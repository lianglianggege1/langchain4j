package dev.langchain4j.agentic.internal;

import dev.langchain4j.agentic.observability.AgentListener;
import dev.langchain4j.agentic.planner.AgentInstance;

// 内部agent
public interface InternalAgent extends AgentInstance {

    // 设置父级
    void setParent(InternalAgent parent);

    // 注册继承的父侦听器
    void registerInheritedParentListener(AgentListener parentListener);

    // 添加id
    void appendId(String idSuffix);

    // 获取侦听器
    AgentListener listener();

    // 允许流式输出
    default boolean allowStreamingOutput() {
        throw new UnsupportedOperationException();
    }

    // 允许聊天历史
    default boolean allowChatMemory() {
        return true;
    }
}
