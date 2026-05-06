package dev.langchain4j.mcp.client;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.service.tool.ToolExecutionResult;
import java.util.List;
import java.util.Map;

/**
 * Represents a client that can communicate with an MCP server over a given transport protocol,
 * retrieve and execute tools using the server.
 */
/**
 * 表示一个客户端，该客户端能够通过指定的传输协议与 MCP 服务器通信，
 * 并通过服务器获取和执行工具。
 */
public interface McpClient extends AutoCloseable {

    /**
     * Returns the unique key of this client.
     * 返回此客户端的唯一标识键。
     */
    String key();

    /**
     * Obtains a list of tools from the MCP server.
     * 从 MCP 服务器获取工具列表。
     */
    List<ToolSpecification> listTools();

    /**
     * Obtains a list of tools from the MCP server.
     * 从MCP服务器获取工具列表。
     */
    List<ToolSpecification> listTools(InvocationContext invocationContext);

    /**
     * Executes a tool on the MCP server and returns the result.
     * Currently, this expects a tool execution to only contain text-based results or JSON structured content.
     */
    /**
     * 在 MCP 服务器上执行工具并返回结果。
     * 目前，该方法仅支持工具执行返回纯文本结果或 JSON 结构化内容。
     */
    ToolExecutionResult executeTool(ToolExecutionRequest executionRequest);

    /**
     * Executes a tool on the MCP server and returns the result.
     * Currently, this expects a tool execution to only contain text-based results or JSON structured content.
     */
    /**
     * 在 MCP 服务器上执行工具并返回结果。
     * 目前，此方法仅支持工具执行返回**纯文本结果**或 **JSON 结构化内容**。
     */
    ToolExecutionResult executeTool(ToolExecutionRequest executionRequest, InvocationContext invocationContext);

    /**
     * Obtains the current list of resources available on the MCP server.
     */
    /**
     * 获取 MCP 服务器上当前可用的资源列表。
     */
    List<McpResource> listResources();

    /**
     * Obtains the current list of resources available on the MCP server.
     * 获取 MCP 服务器上当前可用的资源列表。
     */
    List<McpResource> listResources(InvocationContext invocationContext);

    /**
     * Obtains the current list of resource templates (dynamic resources) available on the MCP server.
     * 获取 MCP 服务器上当前可用的资源模板（动态资源）列表。
     */
    List<McpResourceTemplate> listResourceTemplates();

    /**
     * Obtains the current list of resource templates (dynamic resources) available on the MCP server.
     * 获取 MCP 服务器上当前可用的资源模板（动态资源）列表。
     */
    List<McpResourceTemplate> listResourceTemplates(InvocationContext invocationContext);

    /**
     * Retrieves the contents of the resource with the specified URI. This also
     * works for dynamic resources (templates).
     */
    /**
     * 获取指定统一资源标识符（URI）对应的资源内容。
     * 该方法同样适用于动态资源（资源模板）。
     */
    McpReadResourceResult readResource(String uri);

    /**
     * Retrieves the contents of the resource with the specified URI. This also
     * works for dynamic resources (templates).
     */
    /**
     * 获取具有指定URI的资源的内容。
     * 此方法同样适用于动态资源（模板）。
     */
    McpReadResourceResult readResource(String uri, InvocationContext invocationContext);

    /**
     * Subscribes to updates for the resource with the specified URI.
     * When the resource changes, the server will send a {@code notifications/resources/updated} notification.
     * The client will invoke the {@code onResourceUpdated} callback (if configured) with the URI of the updated resource.
     */
    /**
     * 订阅指定URI资源的更新通知。
     * 当资源发生变更时，服务器会发送一条 {@code notifications/resources/updated} 通知。
     * 客户端会调用已配置的 {@code onResourceUpdated} 回调函数，并传入已更新资源的URI。
     */
    void subscribeToResource(String uri);

    /**
     * Unsubscribes from updates for the resource with the specified URI.
     * 取消订阅指定URI资源的更新通知。
     */
    void unsubscribeFromResource(String uri);

    /**
     * Obtain a list of prompts available on the MCP server.
     * 获取 MCP 服务器上可用的提示（prompts）列表。
     */
    List<McpPrompt> listPrompts();

    /**
     * Render the contents of a prompt.
     * 渲染提示词内容。
     */
    McpGetPromptResult getPrompt(String name, Map<String, Object> arguments);

    /**
     * Performs a health check that returns normally if the MCP server is reachable and
     * properly responding to ping requests. If this method throws an exception,
     * the health of this MCP client is considered degraded.
     */
    /**
     * 执行健康检查：如果 MCP 服务器可访问且能正常响应心跳请求，则正常返回。
     * 如果此方法抛出异常，则视为该 MCP 客户端的健康状态异常/降级。
     */
    void checkHealth();

    /**
     * Sets the roots that are made available to the server upon its request.
     * After calling this method, the client also sends a `notifications/roots/list_changed` message to the server.
     */
    /**
     * 设置服务器请求时可访问的根路径。
     * 调用此方法后，客户端还会向服务器发送 `notifications/roots/list_changed` 消息。
     */
    void setRoots(List<McpRoot> roots);
}
