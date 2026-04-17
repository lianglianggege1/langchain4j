package dev.langchain4j.agentic.workflow;

import dev.langchain4j.agentic.planner.AgenticService;

// 串行agent服务
public interface SequentialAgentService<T> extends AgenticService<SequentialAgentService<T>, T> {

}
