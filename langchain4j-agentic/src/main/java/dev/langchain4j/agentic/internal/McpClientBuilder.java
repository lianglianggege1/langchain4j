package dev.langchain4j.agentic.internal;

import dev.langchain4j.agentic.observability.AgentListener;

public interface McpClientBuilder<T> {

    // 工具名称
    McpClientBuilder<T> toolName(String toolName);

    // 输入参数
    McpClientBuilder<T> inputKeys(String... inputKeys);

    // 输出参数
    McpClientBuilder<T> outputKey(String outputKey);

    // 异步
    McpClientBuilder<T> async(boolean async);

    // 监听器
    McpClientBuilder<T> listener(AgentListener agentListener);

    // 构建
    T build();
}
