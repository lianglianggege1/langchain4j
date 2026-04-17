package dev.langchain4j.agentic.planner;

import dev.langchain4j.agentic.scope.AgentInvocation;
import dev.langchain4j.agentic.scope.AgenticScope;

// 规划上下文
public record PlanningContext(AgenticScope agenticScope, AgentInvocation previousAgentInvocation) {
}
