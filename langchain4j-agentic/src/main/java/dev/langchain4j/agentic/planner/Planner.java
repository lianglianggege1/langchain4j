package dev.langchain4j.agentic.planner;

import java.util.List;

// 计划
public interface Planner {

    // 初始化
    default void init(InitPlanningContext initPlanningContext) { }

    // 第一个动作
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
