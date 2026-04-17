package dev.langchain4j.agentic.internal;

import dev.langchain4j.agentic.scope.AgentInvocation;

// 计划执行人
public interface PlannerExecutor {

    // 响应子代理调用
    void onSubagentInvoked(AgentInvocation agentInvocation);

    // 是否传递流式响应
    boolean propagateStreaming();
}
