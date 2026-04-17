package dev.langchain4j.agentic.workflow.impl;

import java.util.List;
import java.util.function.BiPredicate;
import dev.langchain4j.agentic.planner.Action;
import dev.langchain4j.agentic.planner.AgentInstance;
import dev.langchain4j.agentic.planner.AgenticSystemTopology;
import dev.langchain4j.agentic.planner.InitPlanningContext;
import dev.langchain4j.agentic.planner.PlanningContext;
import dev.langchain4j.agentic.planner.Planner;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.agentic.workflow.LoopAgentInstance;

public class LoopPlanner implements Planner {

    // 最大迭代次数
    private final int maxIterations;
    // 当前迭代次数
    private int iterationsCounter = 1;

    // 测试退出条件
    private final boolean testExitAtLoopEnd;

    // 退出条件
    private final BiPredicate<AgenticScope, Integer> exitCondition;
    // 退出条件描述
    private final String exitConditionDescription;

    // 循环的子代理
    private List<AgentInstance> agents;
    // 当前代理索引
    private int agentCursor = 0;

    public LoopPlanner(int maxIterations, boolean testExitAtLoopEnd, BiPredicate<AgenticScope, Integer> exitCondition, String exitConditionDescription) {
        this.maxIterations = maxIterations;
        this.testExitAtLoopEnd = testExitAtLoopEnd;
        this.exitCondition = exitCondition;
        this.exitConditionDescription = exitConditionDescription;
    }

    @Override
    public void init(InitPlanningContext initPlanningContext) {
        this.agents = initPlanningContext.subagents();
    }

    // 第一个动作
    @Override
    public Action firstAction(PlanningContext planningContext) {
        // 调用子代理
        return call(agents.get(agentCursor));
    }

    // 下一个动作
    @Override
    public Action nextAction(PlanningContext planningContext) {
        // agent光标
        agentCursor = (agentCursor+1) % agents.size();
        if (agentCursor == 0) {
            // 当agent光标大于最大迭代次数后，该task就已经完成了
            if (iterationsCounter > maxIterations || exitCondition.test(planningContext.agenticScope(), iterationsCounter)) {
                return done();
            }
            iterationsCounter++;
        } else if (!testExitAtLoopEnd && exitCondition.test(planningContext.agenticScope(), iterationsCounter)) {
            return done();
        }
        return call(agents.get(agentCursor));
    }

    @Override
    public AgenticSystemTopology topology() {
        return AgenticSystemTopology.LOOP;
    }

    @Override
    public <T extends AgentInstance> T as(Class<T> agentInstanceClass, AgentInstance agentInstance) {
        if (agentInstanceClass != LoopAgentInstance.class) {
            throw new ClassCastException("Cannot cast to " + agentInstanceClass.getName() + ": incompatible type");
        }
        return (T) new DefaultLoopAgentInstance(agentInstance, this);
    }

    // 最大迭代次数
    public int maxIterations() {
        return maxIterations;
    }

    // 测试退出条件
    public boolean testExitAtLoopEnd() {
        return testExitAtLoopEnd;
    }

    // 退出条件
    public String exitCondition() {
        return exitConditionDescription;
    }
}
