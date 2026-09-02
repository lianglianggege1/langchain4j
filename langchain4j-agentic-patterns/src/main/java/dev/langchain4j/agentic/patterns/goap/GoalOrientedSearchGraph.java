package dev.langchain4j.agentic.patterns.goap;

import dev.langchain4j.agentic.patterns.goap.DependencyGraphSearch.Node;
import dev.langchain4j.agentic.planner.AgentArgument;
import dev.langchain4j.agentic.planner.AgentInstance;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GOAP 依赖图：把"动作表"（每个 agent 的 前置条件→效果）翻译成一张可搜索的图。
 * <p>
 * 建模规则（GOAP 落地为图的核心一步）：
 * <ul>
 *   <li><b>节点（Node）</b> = AgenticScope 中的一个 state key（数据依赖的名字），不存值只存名字；</li>
 *   <li><b>边（edge）</b> = 一个 subAgent，从它的每个输入 key 节点指向它的输出 key 节点；</li>
 *   <li>一条 {@code (输入节点, 输出节点)} 二元组唯一确定"产出该输出"的 agent，
 *       存进 {@link #edges}，供搜索完成后把节点路径反查回 agent 路径。</li>
 * </ul>
 * 以 writer 实例为例，三个 subAgent 建出的图：
 * <pre>
 *   topic ──── storyGenerator ───▶ story ──┐
 *   style ────────────────────────────────┴─ styleReviewLoop ──▶ styledStory
 *   styledStory ──┐
 *   audience ─────┴─ audienceEditor ──▶ finalStory（goal）
 * </pre>
 */
public class GoalOrientedSearchGraph {

    private static final Logger LOG = LoggerFactory.getLogger(GoalOrientedSearchGraph.class);

    /** 边的键：一对节点（输入, 输出）。同一对节点只可能由一个 agent 产出。 */
    private record NodePair(Node input, Node output) {}

    /** state key → 图节点 的注册表（建图时懒创建，所有 key 共享节点实例）。 */
    private final Map<String, Node> nodes = new HashMap<>();

    /** (输入节点, 输出节点) → 能产出该输出的 agent。即"边"的实例化。 */
    private final Map<NodePair, AgentInstance> edges = new HashMap<>();

    public GoalOrientedSearchGraph(List<AgentInstance> agents) {
        init(agents);
    }

    /**
     * 建图：遍历每个 subAgent，登记它的 前置条件（@V 参数名）→ 效果（outputKey）。
     * 等价于经典 GOAP 里"声明动作的前置条件与效果"，只是这里的"世界状态"
     * 被降维成了黑板上的 key 依赖关系（agent 的效果就是写一个 key，图因此天然无环）。
     */
    private void init(List<AgentInstance> agents) {
        for (AgentInstance agent : agents) {
            // 前置条件：方法签名上所有 @V("...") 参数名 → 输入节点（图中不存在则懒创建）。
            List<Node> inputs = agent.arguments().stream()
                    .map(AgentArgument::name)
                    .map(arg -> nodes.computeIfAbsent(arg, Node::new))
                    .toList();
            // 效果：agent 执行后写入黑板的 outputKey → 输出节点。
            Node output = nodes.computeIfAbsent(agent.outputKey(), Node::new);

            inputs.forEach(input -> {
                // 建立 输入→输出 的双向邻接关系：
                // input.outputNodes 记录"我喂给谁"，output.inputNodes 记录"激活我之前必须先有谁"，
                // 后者正是底层搜索 canActivate() 判断 AND 依赖的依据。
                input.addOutput(output);
                // 登记边：(输入节点, 输出节点) → 该 agent。
                // 之后拿到节点路径时，用它反查"哪个 agent 能产出这个节点"。
                edges.put(new NodePair(input, output), agent);
            });
        }
    }

    /**
     * 搜索入口：给定"当前黑板上已有的 key"（前置条件）与目标 key，返回达成目标的 agent 序列。
     * <p>
     * 分两步走：
     * <ol>
     *   <li>底层 A* 在<b>节点图</b>上搜出"按激活顺序排列的节点路径"；</li>
     *   <li>本方法再把<b>节点路径翻译回 agent 路径</b>（调用方只认 agent，不认节点）。</li>
     * </ol>
     *
     * @param preconditions 当前 AgenticScope 里已有的全部 state key
     * @param goal 目标 key（根代理的 outputKey）
     * @return 按执行顺序排列的 agent 列表；不可达时返回空列表
     */
    public List<AgentInstance> search(Collection<String> preconditions, String goal) {
        // ── 第一步：节点级 A* 搜索 ──
        // 前置条件翻译：黑板上的 key → 图节点。
        // 注意 filter(Objects::nonNull)：黑板里存在但图中没有的 key（如监听器塞入的
        // additionalKey）会被静默丢弃——"多余的已知状态"不影响规划，这是刻意的容错。
        List<Node> nodesPath = DependencyGraphSearch.search(
                nodes.get(goal),
                preconditions.stream().map(nodes::get).filter(Objects::nonNull).toList());

        if (nodesPath == null) {
            // 底层搜索返回 null：从当前前置条件出发在图上无法抵达 goal。
            return List.of();
        }

        // ── 第二步：节点路径 → agent 路径 ──
        // nodesPath 形如 [topic, story, styledStory, finalStory]（首元素必是某个前置条件节点）。
        // 其中"本来就是前置条件"的节点已在黑板上，无需任何 agent 产出，直接跳过；
        // 其余每个节点都要反查出"能产出它的那个 agent"。
        int preconditionsCounter = 0; // 已跳过的前置条件节点数
        List<AgentInstance> agentsPath = new ArrayList<>();
        for (int i = 1; i < nodesPath.size(); i++) { // i 从 1 起：跳过初始状态里就有的首节点
            Node output = nodesPath.get(i);
            if (preconditions.contains(output.getId())) {
                // 该节点初始状态里已经有了（典型场景：崩溃恢复时黑板已含 styledStory 等中间产物）
                preconditionsCounter++;
                continue;
            }
            // 从当前位置向前回溯，找到第一对 (前驱, 当前) 在 edges 中有登记的 agent，
            // 即"产出当前节点"的动作。多输入节点（如 styledStory 同时吃 story 和 style）
            // 只要路径上存在它的任一前驱，就能定位到同一个 agent（建图时每条输入边都登记了它）。
            for (int j = i - 1; j >= 0; j--) {
                AgentInstance agent = edges.get(new NodePair(nodesPath.get(j), output));
                if (agent != null) {
                    agentsPath.add(agent);
                    break;
                }
            }
            // 不变式校验：到第 i 个节点为止，"需要产出的节点数 = i - 已跳过的前置条件数"
            // 必须恰好等于已收集的 agent 数。不等说明路径上出现了没有任何 agent 能产出的
            // "悬空节点"——正常建图不会发生，这里是对搜索结果完整性的兜底防护。
            if (agentsPath.size() != i - preconditionsCounter) {
                throw new IllegalStateException("No path found for node: " + output.getId());
            }
        }

        // 打日志便于观察规划结果（writer 实例可看到：
        // "Agents path sequence: [storyGenerator, styleReviewLoop, audienceEditor]"）。
        LOG.info(
                "Agents path sequence: {}",
                agentsPath.stream().map(AgentInstance::name).toList());

        return agentsPath;
    }
}
