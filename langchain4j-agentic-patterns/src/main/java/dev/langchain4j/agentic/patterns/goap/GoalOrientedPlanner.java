package dev.langchain4j.agentic.patterns.goap;

import java.util.List;
import java.util.Map;
import dev.langchain4j.agentic.planner.Action;
import dev.langchain4j.agentic.planner.AgentInstance;
import dev.langchain4j.agentic.planner.AgenticSystemTopology;
import dev.langchain4j.agentic.planner.InitPlanningContext;
import dev.langchain4j.agentic.planner.PlanningContext;
import dev.langchain4j.agentic.planner.Planner;

//**GOAP**：目标导向行为规划（Goal-Oriented Action Planning）
/**
 * GOAP 规划器：整个模式的"决策者"（只决定下一步调用谁，不负责真正执行）。
 * <p>
 * GOAP 建模三要素在本框架中的落地方式：
 * <ul>
 *   <li><b>前置条件（precondition）</b> = agent 方法签名上 {@code @V("...")} 的参数名，
 *       即该 agent 运行前黑板上必须已存在的 state key；</li>
 *   <li><b>效果（effect）</b> = agent 的 {@code outputKey}，
 *       即 agent 执行后写入黑板的那个 key；</li>
 *   <li><b>目标（goal）</b> = 规划根代理（如 writer 实例的 Writer 接口）的 {@code outputKey}。</li>
 * </ul>
 * 你不声明执行顺序，只声明"每个动作需要什么、产出什么"，
 * 规划器在运行时用图搜索自动算出一条通往目标的最短 agent 序列。
 * <p>
 * 与运行时的分工（见 PlannerBasedInvocationHandler 内的 PlannerLoop）：
 * 运行时持有黑板（AgenticScope），反复问本规划器"下一步干什么"（firstAction/nextAction），
 * 规划器以 {@link Action} 作答，运行时负责真正调用 agent 并把输出写回黑板。
 * <p>
 * 以 writer 测试实例（goap/writer/GoapWriterIT）为例：
 * <pre>
 *   storyGenerator : topic                    → story        （前置条件 → 效果）
 *   styleReviewLoop: story + style             → styledStory  （循环对外只是一个动作）
 *   audienceEditor : styledStory + audience    → finalStory   （= goal）
 *   搜索结果：[storyGenerator, styleReviewLoop, audienceEditor]
 * </pre>
 */
public class GoalOrientedPlanner implements Planner {

    /** 目标 state key：取自规划根代理的 outputKey（如 writer 实例的 "finalStory"）。 */
    private String goal;

    /** 依赖图：节点 = state key，边 = subAgent（输入 key → 输出 key），由 subAgents 建模而来。 */
    private GoalOrientedSearchGraph graph;

    /** 图搜索算出的 agent 执行序列（一次规划、顺序执行，中途不重规划）。 */
    private List<AgentInstance> path;

    /** 当前执行到 path 的第几个 agent。 */
    private int agentCursor = 0;

    @Override
    public void init(InitPlanningContext initPlanningContext) {
        // 目标 = 根代理的 outputKey。GOAP 只关心"黑板上最终要出现哪个 key"，
        // 不关心路径怎么走——路径交给图搜索决定。
        this.goal = initPlanningContext.plannerAgent().outputKey();
        // 用"直接注册"的 subAgents 建图。注意：循环等复合代理内部的子 agent 不在此列，
        // 复合代理对外只是一个普通的动作（接口方法参数=前置条件，builder 的 outputKey=效果），
        // 其内部由自己的规划器（如 LoopPlanner）驱动——"规划器套规划器"的递归结构。
        this.graph = new GoalOrientedSearchGraph(initPlanningContext.subagents());
    }

    @Override
    public Action firstAction(PlanningContext planningContext) {
        // 以当前黑板上已有的全部 state key 作为搜索前置条件（初始已激活集合），
        // 向 goal 做一次 A* 搜索，得到最短 agent 激活序列。
        // 崩溃/挂起恢复时会重新走到这里：已完成 agent 的输出已在黑板上，
        // 重搜出的路径天然只含"还没做的"agent——这就是 GOAP 无需持久化状态的原因。
        path = graph.search(planningContext.agenticScope().state().keySet(), goal);
        if (path.isEmpty()) {
            // 图上不存在从当前状态到目标的通路。
            // 常见根因：某个 @V 参数名 / outputKey 拼错，导致依赖链断裂（图是靠这些字符串连通的）。
            throw new IllegalStateException("No path found for goal: " + goal);
        }
        // 返回第一个 Action：调用路径上的第一个 agent，游标前移。
        // 真正的执行（LLM 调用、结果写回黑板）由运行时完成。
        return call(path.get(agentCursor++));
    }

    @Override
    public Action nextAction(PlanningContext planningContext) {
        // 游标走完路径上所有 agent → done()，运行时随即退出 while 循环并读取黑板上的最终结果。
        // 注意：GOAP 是"一次搜完、顺序执行"，不根据中间结果重新规划；
        // 重新规划只发生在恢复执行重新进入 firstAction() 时。
        return agentCursor >= path.size() ? done() : call(path.get(agentCursor++));
    }

    /**
     * GoalOrientedPlanner does not persist execution state because {@link #firstAction(PlanningContext)}
     * recomputes the path from the current scope state via graph search. On recovery, completed agents'
     * outputs are already in scope, so the search produces a shorter path containing only the remaining
     * agents. The cursor resets to 0 naturally, making state persistence unnecessary and potentially
     * harmful (a stale cursor could point beyond the bounds of the recomputed path).
     */
    /**
     * 目标导向规划器不会持久化执行状态，因为{@link #firstAction(PlanningContext)}
     * 会基于当前作用域状态重新执行图搜索以计算路径。任务恢复时，已完成执行的代理节点输出内容已存在于作用域中，
     * 因此重新搜索得到的路径仅包含剩余待执行的代理节点。执行游标会自动重置为0，
     * 故无需持久化状态，且持久化操作反而可能引发问题（失效游标可能超出重算后路径的边界）。
     */
    @Override
    public void restoreExecutionState(Map<String, Object> state) {
        // No-op: path recomputation in firstAction() handles recovery
        // 空实现是故意的："重搜"本身就是恢复策略，无状态即自愈。
    }

    @Override
    public AgenticSystemTopology topology() {
        // A* 搜出的是线性激活序列，因此拓扑为 SEQUENCE。
        return AgenticSystemTopology.SEQUENCE;
    }
}
