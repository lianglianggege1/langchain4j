package dev.langchain4j.agentic.supervisor;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.scope.AgenticScopeAccess;
import dev.langchain4j.agentic.scope.ResultWithAgenticScope;
import dev.langchain4j.service.V;

// 代理监控者
public interface SupervisorAgent extends AgenticScopeAccess {
    // 调用
    @Agent
    String invoke(@V("request") String request);

    // 调用AgenticScope
    ResultWithAgenticScope<String> invokeWithAgenticScope(@V("request") String request);
}
