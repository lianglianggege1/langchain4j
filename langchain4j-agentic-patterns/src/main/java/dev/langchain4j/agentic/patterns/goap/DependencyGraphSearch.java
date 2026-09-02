package dev.langchain4j.agentic.patterns.goap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Modified A* Search for Dependency Graphs
 * <p>
 * Handles nodes with multiple input dependencies where ALL inputs must be
 * satisfied before a node can be activated/traversed.
 */
/**
 * 适用于依赖图的改进版A*搜索
 * <p>
 * 处理存在多个输入依赖的节点，节点必须满足**全部输入依赖**后，才能被激活或遍历。
 */
/**
 * GOAP 的搜索内核：在依赖图上做 A*，求"从初始已激活集合到目标节点"的最短激活序列。
 * <p>
 * 与经典 A*（如 LeetCode 网格最短路）的两点差异：
 * <ol>
 *   <li><b>状态是集合而非单点</b>：经典 A* 的状态 = 当前所在节点；这里的状态 =
 *       {@code 已激活节点集合 + 当前节点}。因为激活一个节点可能同时满足多条下游依赖（AND 依赖），
 *       搜索空间实际是"集合的演化过程"。</li>
 *   <li><b>邻居受 AND 约束</b>：一个节点不是"与当前节点相邻"就能访问，而是
 *       <b>它的全部输入依赖都已在激活集合中</b>才可访问——语义等价于拓扑排序里"入度归零"。</li>
 * </ol>
 * A* 记号对照：g = 已激活节点数（每步 +1）；h = 启发函数估算的剩余节点数；f = g + h。
 */
public class DependencyGraphSearch {

    /**
     * Represents a node with multiple inputs and a single output
     */
    /**
     * 表示多输入、单输出的节点
     */
    /**
     * 依赖图节点，建模含义 = 一个 state key（只存名字，不存值）。
     * <ul>
     *   <li>{@link #inputNodes}：上游依赖——激活本节点之前必须先具备的 key 集合（AND 语义）；</li>
     *   <li>{@link #outputNodes}：下游——本 key 解锁（喂给）的 key 列表。</li>
     * </ul>
     */
    public static class Node {
        private final String id;
        private final Set<Node> inputNodes = new HashSet<>();      // Required input nodes
        private final List<Node> outputNodes = new ArrayList<>();  // Nodes this feeds into

        public Node(String id) {
            this.id = id;
        }

        private void addInput(Node input) {
            inputNodes.add(input);
        }

        /**
         * 由建图方调用：登记"本节点 → output 节点"的一条边，并同时反向登记
         * output 对本节点的输入依赖——一次调用建立双向邻接，保证两张邻接表永远一致。
         */
        public void addOutput(Node output) {
            outputNodes.add(output);
            // 自动建立双向关联关系
            output.addInput(this);  // Automatically set up bidirectional relationship
        }

        public Set<Node> getInputNodes() {
            return inputNodes;
        }

        public List<Node> getOutputNodes() {
            return outputNodes;
        }

        public String getId() {
            return id;
        }

        // 节点身份由 id（即 state key）唯一决定：同一 key 在全图只对应一个节点实例。

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof final Node node)) return false;
            return id.equals(node.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public String toString() {
            return id;
        }
    }

    /**
     * Represents the state of the search: which nodes have been activated
     */
    /**
     * 表示搜索状态：记录已激活的节点
     */
    /**
     * A* 的"搜索状态"：已激活节点集合 + 当前节点 + 深度。
     * record 不可变——每次状态转移都生成新实例（见 {@link #activateNode}），
     * 这样状态才能安全地作为 visited / gScore / cameFrom 等 Map 的键。
     */
    record SearchState(Set<Node> activatedNodes, Node currentNode, int depth) {

        /**
         * 状态转移：激活一个新节点 = 复制已激活集合并加入该节点，深度 +1。
         * 深度就是 A* 的 g 值雏形（每激活一个节点代价恒为 1，见主循环）。
         */
        SearchState activateNode(Node node) {
            Set<Node> newActivated = new HashSet<>(activatedNodes);
            newActivated.add(node);
            return new SearchState(newActivated, node, depth + 1);
        }

        /**
         * AND 依赖判定：节点可被激活，当且仅当它的<b>全部</b>输入依赖都已激活。
         * 这正是"依赖图搜索"区别于普通图搜索的地方。
         */
        boolean canActivate(Node node) {
            // A node can be activated if all its input dependencies are satisfied
            return activatedNodes.containsAll(node.getInputNodes());
        }
    }

    /**
     * Wrapper for priority queue with f-score
     */
    /**
     * 带估价分数的优先队列包装类
     */
    /**
     * openSet 的队列元素：(状态, f = g + h)。PriorityQueue 按此比较，
     * 保证每次弹出"看起来离目标最近"的状态——A* 的贪心本质就在这里。
     */
    record StateScore(SearchState state, double fScore) implements Comparable<StateScore> {

        @Override
        public int compareTo(StateScore other) {
            return Double.compare(this.fScore, other.fScore);
        }
    }

    /**
     * Heuristic function for estimating remaining cost
     */
    /**
     * 用于估算剩余代价的启发式函数
     */
    interface Heuristic {
        double estimate(SearchState state, Node goal);
    }

    public static List<Node> search(Node goal, Node... preconditions) {
        return search(goal, Stream.of(preconditions).collect(Collectors.toSet()));
    }

    /**
     * 对外搜索入口：使用默认启发函数（见下方实现）。
     *
     * @param goal 目标节点
     * @param preconditions 初始已激活（黑板上已存在）的节点集合
     * @return 按激活顺序排列的节点路径；不可达返回 null
     */
    public static List<Node> search(Node goal, Collection<Node> preconditions) {
        // Simple heuristic: number of unsatisfied dependencies
        // 默认启发函数：从 goal 出发沿 inputNodes 反向遍历，收集所有"尚未激活"的节点数。
        Heuristic heuristic = (state, goalNode) -> {
            if (state.activatedNodes.contains(goalNode)) {
                // 目标已激活：剩余代价为 0
                return 0.0;
            }
            // Estimate remaining nodes to activate
            // 反向 BFS：只有"自己还没激活"的节点才需要继续看它的依赖
            //（已激活节点的依赖必然也已激活或本就无关）。
            // 该估计是可采纳(admissible)的：不高估——每个未激活节点至少要花 1 步去激活，
            // 因此 A* 能保证搜出的激活序列是最短的。
            Set<Node> remaining = new HashSet<>();
            Queue<Node> toCheck = new LinkedList<>();
            toCheck.add(goalNode);

            while (!toCheck.isEmpty()) {
                Node node = toCheck.poll();
                if (!state.activatedNodes.contains(node)) {
                    remaining.add(node);
                    toCheck.addAll(node.getInputNodes());
                }
            }

            return remaining.size();
        };
        return search(preconditions, goal, heuristic);
    }

    /**
     * Finds shortest path considering dependency constraints
     *
     * @param startNodes Set of nodes that are already active (preconditions)
     * @param goal The goal node
     * @param heuristic Heuristic function for A*
     * @return List of nodes in activation order, or null if no path exists
     */
    /**
     * 结合依赖约束查找最短路径
     *
     * @param startNodes 已激活的节点集合（前置条件）
     * @param goal 目标节点
     * @param heuristic A*算法启发式函数
     * @return 按激活顺序排列的节点列表，若无路径则返回null
     */
    /**
     * A* 主循环。以 writer 实例走一遍：
     * 初始激活集 {topic, style, audience}；可激活 story（其输入 topic 已满足）；
     * 激活 story 后 styledStory 两个输入（story、style）齐全，可激活；
     * 最后 finalStory 的输入（styledStory、audience）齐全，可激活并命中 goal。
     * 产出节点路径 [topic(初始), story, styledStory, finalStory]。
     */
    private static List<Node> search(Collection<Node> startNodes, Node goal, Heuristic heuristic) {
        if (startNodes == null || startNodes.isEmpty()) {
            throw new IllegalArgumentException("Must provide at least one start node");
        }

        // Initial state: all start nodes are already activated
        // 初始状态：前置条件节点全部视为已激活（黑板上已有这些 key）。
        Set<Node> initialActivated = new HashSet<>(startNodes);
        // Use first start node as current (arbitrary choice since all are active)
        // "当前节点"任取一个前置节点即可——它们都已激活，不影响后续扩展（扩展看的是整个集合）。
        SearchState initialState = new SearchState(initialActivated, startNodes.iterator().next(), 0);

        // A* 标准四件套：
        PriorityQueue<StateScore> openSet = new PriorityQueue<>(); // 待扩展状态，按 f=g+h 升序
        Set<SearchState> visited = new HashSet<>();                  // 已扩展过的状态（防重复扩展）
        Map<SearchState, SearchState> cameFrom = new HashMap<>();    // 状态 → 前驱状态（重构路径用）
        Map<SearchState, Double> gScore = new HashMap<>();           // 到达该状态的实际代价 g

        gScore.put(initialState, 0.0);
        openSet.add(new StateScore(initialState, heuristic.estimate(initialState, goal)));

        while (!openSet.isEmpty()) {
            // 弹出 f 最小的状态（g+h 最优）进行扩展——A* 的核心选择策略。
            SearchState current = openSet.poll().state;

            if (visited.contains(current)) {
                // 队列里可能残留同一状态的旧记录（松弛更新时重复入队），跳过。
                continue;
            }
            visited.add(current);

            // Check if goal is reached
            // 目标判定：当前节点即 goal → 沿 cameFrom 回溯出激活序列。
            if (current.currentNode.equals(goal)) {
                return reconstructPath(cameFrom, current);
            }

            double currentGScore = gScore.get(current);

            // Explore all nodes that can now be activated
            // 邻居扩展：不是"图上相邻"而是"依赖已满足"——取当前激活集合能解锁的全部节点。
            for (Node nextNode : findActivatableNodes(current)) {
                SearchState nextState = current.activateNode(nextNode);

                if (visited.contains(nextState)) {
                    continue;
                }

                double tentativeGScore = currentGScore + 1.0; // Cost of activating one node
                // 每激活一个节点的代价恒为 1：最短路径 = 激活节点数最少 = 执行的 agent 数最少。

                if (tentativeGScore < gScore.getOrDefault(nextState, Double.POSITIVE_INFINITY)) {
                    // 经典 A* 松弛：找到更优到达方式才更新前驱、g 值，并把新 f 压入队列。
                    cameFrom.put(nextState, current);
                    gScore.put(nextState, tentativeGScore);
                    double fScore = tentativeGScore + heuristic.estimate(nextState, goal);
                    openSet.add(new StateScore(nextState, fScore));
                }
            }
        }

        return null; // No path found
        // 队列耗尽仍未命中 goal：从前置条件出发在图上不可达（依赖链断裂）。
    }

    /**
     * Finds all nodes that can be activated given the current state
     */
    /**
     * 根据当前状态查找所有可激活的节点
     */
    /**
     * 候选邻居 = 已激活节点的输出中，"尚未激活 且 全部输入依赖已满足"者。
     * 语义上等价于拓扑排序中"入度已归零"的节点集合，是 AND 依赖约束在扩展步的体现。
     */
    private static Set<Node> findActivatableNodes(SearchState state) {
        Set<Node> activatable = new HashSet<>();

        // Check all output nodes of already activated nodes
        // 检查已激活节点的所有输出节点
        for (Node activatedNode : state.activatedNodes) {
            for (Node outputNode : activatedNode.getOutputNodes()) {
                if (!state.activatedNodes.contains(outputNode) && state.canActivate(outputNode)) {
                    activatable.add(outputNode);
                }
            }
        }

        return activatable;
    }

    /**
     * Reconstructs the path by following cameFrom references
     */
    /**
     * 通过前驱引用回溯重构路径
     */
    /**
     * 路径重构，两步：
     * <ol>
     *   <li>cameFrom 是"状态 → 前驱状态"的链，先倒序收集整条状态链（头插法正回来）；</li>
     *   <li>每个状态相对前驱"新激活"的节点 = 该状态激活集合 − 前驱激活集合，
     *       逐状态做差集，即得按激活顺序排列的节点路径。
     *       （注意：初始状态的新增节点也会被算进来，所以路径首元素是某个前置条件节点，
     *       上层 GoalOrientedSearchGraph 翻译时会把它跳过。）</li>
     * </ol>
     */
    private static List<Node> reconstructPath(Map<SearchState, SearchState> cameFrom, SearchState current) {
        List<Node> path = new ArrayList<>();

        // Collect all states in reverse order
        List<SearchState> states = new ArrayList<>();
        states.add(current);

        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            states.add(0, current);
        }

        // Extract the sequence of activated nodes
        // 对每个状态做差集：本次"新激活"的节点就是路径上该步的产出。
        Set<Node> previouslyActivated = new HashSet<>();
        for (SearchState state : states) {
            Set<Node> newNodes = new HashSet<>(state.activatedNodes);
            newNodes.removeAll(previouslyActivated);
            path.addAll(newNodes);
            previouslyActivated = state.activatedNodes;
        }

        return path;
    }
}
