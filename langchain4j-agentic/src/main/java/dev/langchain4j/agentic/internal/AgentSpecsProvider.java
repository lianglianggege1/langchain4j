package dev.langchain4j.agentic.internal;

import dev.langchain4j.agentic.observability.AgentListener;
import dev.langchain4j.agentic.planner.AgentArgument;

import java.util.List;

public interface AgentSpecsProvider {

    // 输出 key
    String outputKey();

    // 描述
    String description();

    // 异步
    boolean async();

    // 监听器
    AgentListener listener();

    default List<AgentArgument> arguments() {
        return null;
    }
}
