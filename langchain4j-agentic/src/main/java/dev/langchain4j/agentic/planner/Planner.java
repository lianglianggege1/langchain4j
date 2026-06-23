package dev.langchain4j.agentic.planner;

import java.util.List;
import java.util.Map;

// 计划
public interface Planner {

    // 初始化
    default void init(InitPlanningContext initPlanningContext) { }

    // 第一个动作
    /**
     * Returns the planner's current execution state as a map of serializable values.
     * This state is persisted to the {@link dev.langchain4j.agentic.scope.AgenticScope} after each
     * agent invocation, enabling the planner to resume from the correct position after a crash.
     * <p>
     * The returned state must be such that, when passed to {@link #restoreExecutionState(Map)} and
     * {@link #firstAction(PlanningContext)} is called, the planner produces the correct resume action.
     * <p>
     * Stateless planners (e.g., parallel, conditional) can use the default empty implementation.
     *
     * @return a map of state entries to persist, or an empty map if no state needs saving
     */
    /**
     * 以可序列化值的映射形式返回规划器的当前执行状态。
     * 该状态会在每次代理调用后持久化到 {@link dev.langchain4j.agentic.scope.AgenticScope}，
     * 使规划器在崩溃后能够从正确的位置恢复执行。
     * <p>
     * 返回的状态需满足：当传入 {@link #restoreExecutionState(Map)} 并调用 {@link #firstAction(PlanningContext)} 时，
     * 规划器能生成正确的恢复执行动作。
     * <p>
     * 无状态规划器（如并行、条件执行型）可使用默认的空实现。
     *
     * @return 待持久化的状态条目映射；若无状态需要保存，则返回空映射
     */
    default Map<String, Object> executionState() {
        return Map.of();
    }

    /**
     * Restores the planner's execution state from a previously saved map.
     * Called by the execution loop before {@link #firstAction(PlanningContext)} when recovering
     * from a persisted scope.
     *
     * @param state the previously saved execution state
     */
    /**
     * 从之前保存的状态映射中恢复规划器的执行状态。
     * 当从持久化作用域恢复执行时，执行循环会在调用 {@link #firstAction(PlanningContext)} 之前调用此方法。
     *
     * @param state 之前保存的执行状态
     */
    default void restoreExecutionState(Map<String, Object> state) { }

    default Action firstAction(PlanningContext planningContext) {
        return nextAction(planningContext);
    }

    // 拓扑
    default AgenticSystemTopology topology() {
        return AgenticSystemTopology.SEQUENCE;
    }

    // 下一个动作
    Action nextAction(PlanningContext planningContext);

    // 终止
    default boolean terminated() {
        return false;
    }

    // 无操作
    default Action noOp() {
        return Action.NoOpAction.INSTANCE;
    }

    // 调用
    default Action call(AgentInstance... agents) {
        return new Action.AgentCallAction(agents);
    }

    // 调用
    default Action call(List<AgentInstance> agents) {
        return call(agents.toArray(new AgentInstance[0]));
    }

    // 完成
    default Action done() {
        return Action.DoneAction.INSTANCE;
    }

    // 完成
    default Action done(Object result) {
        return new Action.DoneWithResultAction(result);
    }

    default <T extends AgentInstance> T as(Class<T> agentInstanceClass, AgentInstance agentInstance) {
        throw new ClassCastException("Cannot cast to " + agentInstanceClass.getName() + ": incompatible type");
    }
}
