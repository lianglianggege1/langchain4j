package dev.langchain4j.agentic.internal;

import dev.langchain4j.agentic.observability.AgentListener;

public interface AgentSpecsProvider {

    // 输出 key
    String outputKey();

    // 描述
    String description();

    // 异步
    boolean async();

    // 监听器
    AgentListener listener();
}
