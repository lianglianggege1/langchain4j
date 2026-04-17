package dev.langchain4j.agentic.planner;

import dev.langchain4j.agentic.scope.AgenticScope;

import java.util.List;

// 初始规划上下文
public record InitPlanningContext(AgenticScope agenticScope, AgentInstance plannerAgent,
                                  List<AgentInstance> subagents) {
}
