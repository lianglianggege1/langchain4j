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
public class GoalOrientedPlanner implements Planner {

    private String goal;

    private GoalOrientedSearchGraph graph;
    private List<AgentInstance> path;

    private int agentCursor = 0;

    @Override
    public void init(InitPlanningContext initPlanningContext) {
        this.goal = initPlanningContext.plannerAgent().outputKey();
        this.graph = new GoalOrientedSearchGraph(initPlanningContext.subagents());
    }

    @Override
    public Action firstAction(PlanningContext planningContext) {
        path = graph.search(planningContext.agenticScope().state().keySet(), goal);
        if (path.isEmpty()) {
            throw new IllegalStateException("No path found for goal: " + goal);
        }
        return call(path.get(agentCursor++));
    }

    @Override
    public Action nextAction(PlanningContext planningContext) {
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
    }

    @Override
    public AgenticSystemTopology topology() {
        return AgenticSystemTopology.SEQUENCE;
    }
}
