package dev.langchain4j.agentic.observability;

import dev.langchain4j.agentic.planner.AgentInstance;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.service.tool.BeforeToolExecution;

//在agent 工具执行之前，记录工具执行信息
public record BeforeAgentToolExecution(AgenticScope agenticScope, AgentInstance agentInstance, BeforeToolExecution toolExecution) {
}
