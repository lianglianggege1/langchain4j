package dev.langchain4j.mcp.client;

import dev.langchain4j.service.tool.ToolExecutionResult;
import java.util.Map;

/**
 * Listener interface for monitoring MCP client operations.
 * 用于监听 MCP 客户端操作的监听器接口。
 */
public interface McpClientListener {

    /**
     * Called before executing a tool.
     * 在执行工具之前调用。
     */
    default void beforeExecuteTool(McpCallContext context) {}

    /**
     * Called after executing a tool if the execution was successful, or if it resulted in an application-level error
     * (but not a protocol-level or communication error).
     */
    /**
     * 在工具执行成功后调用，或在执行触发应用级错误（非协议级/通信级错误）时调用。
     */
    default void afterExecuteTool(McpCallContext context, ToolExecutionResult result, Map<String, Object> rawResult) {}

    /**
     * Called when a tool execution fails due to a protocol-level or communication error.
     */
    /**
     * 当工具执行因**协议级错误**或**通信级错误**而失败时调用。
     */
    default void onExecuteToolError(McpCallContext context, Throwable error) {}

    /**
     * Called before getting a resource.
     * 在获取资源之前调用。
     */
    default void beforeResourceGet(McpCallContext context) {}

    /**
     * Called after getting a resource.
     * 在获取资源之后调用。
     */
    default void afterResourceGet(
            McpCallContext context, McpReadResourceResult result, Map<String, Object> rawResult) {}

    /**
     * Called when getting a resource fails.
     * 获取资源失败时调用。
     */
    default void onResourceGetError(McpCallContext context, Throwable error) {}

    /**
     * Called before getting a prompt.
     * 在获取提示词之前调用。
     */
    default void beforePromptGet(McpCallContext context) {}

    /**
     * Called after getting a prompt.
     * 在获取提示词之后调用。
     */
    default void afterPromptGet(McpCallContext context, McpGetPromptResult result, Map<String, Object> rawResult) {}

    /**
     * Called when getting a prompt fails.
     * 获取提示词失败时调用。
     */
    default void onPromptGetError(McpCallContext context, Throwable error) {}
}
