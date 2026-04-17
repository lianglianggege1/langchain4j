package dev.langchain4j.agentic.observability;

import dev.langchain4j.agentic.scope.AgenticScope;

/**
 * Listener interface for monitoring agent invocations.
 * 用于监视代理调用的侦听器接口。
 */
public interface AgentListener {

    // 在代理调用之前
    default void beforeAgentInvocation(AgentRequest agentRequest) { }
    // 在代理调用之后
    default void afterAgentInvocation(AgentResponse agentResponse) { }
    // 在代理调用期间发生错误时
    default void onAgentInvocationError(AgentInvocationError agentInvocationError) { }

    // 在代理调用期间创建代理之后
    default void afterAgenticScopeCreated(AgenticScope agenticScope) { }

    // 在代理调用期间销毁代理之前
    default void beforeAgenticScopeDestroyed(AgenticScope agenticScope) { }

    // 在代理调用期间执行工具之前
    default void beforeAgentToolExecution(BeforeAgentToolExecution beforeAgentToolExecution) { }

    // 在代理调用期间执行工具之后
    default void afterAgentToolExecution(AfterAgentToolExecution afterAgentToolExecution) { }

    /**
     * Indicates whether this listener should be used only to the agent where it is registered (default)
     * or also inherited by its subagents.
     * 指示此侦听器是应仅用于注册它的代理（默认），还是也应由其子代理继承。
     *
     * @return true if the listener should be inherited by sub-agents, false otherwise
     * 如果侦听器应由子代理继承，则为true，否则为false
     */
    default boolean inheritedBySubagents() {
        return false;
    }
}
