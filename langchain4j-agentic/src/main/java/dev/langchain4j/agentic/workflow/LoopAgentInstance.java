package dev.langchain4j.agentic.workflow;

import dev.langchain4j.agentic.planner.AgentInstance;

// 循环agent实例
public interface LoopAgentInstance extends AgentInstance {

    // 最大迭代次数
    int maxIterations();

    // 测试循环结束
    boolean testExitAtLoopEnd();

    // 退出条件
    String exitCondition();
}
