package dev.langchain4j.agentic.workflow;

import dev.langchain4j.agentic.planner.AgentInstance;
import java.util.List;

// 条件agent实例
public interface ConditionalAgentInstance extends AgentInstance {
    List<ConditionalAgent> conditionalSubagents();
}
