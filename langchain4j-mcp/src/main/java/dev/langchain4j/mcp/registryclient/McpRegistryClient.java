package dev.langchain4j.mcp.registryclient;

import dev.langchain4j.mcp.registryclient.model.McpGetServerResponse;
import dev.langchain4j.mcp.registryclient.model.McpRegistryHealth;
import dev.langchain4j.mcp.registryclient.model.McpRegistryPong;
import dev.langchain4j.mcp.registryclient.model.McpServerList;
import dev.langchain4j.mcp.registryclient.model.McpServerListRequest;

/**
 * The interface for talking to a MCP (sub)registry.
 * See <a href="https://registry.modelcontextprotocol.io/docs#/">official reference documentation</a> for more details about the API.
 * This interface closely mirrors the official API.
 */
/**
 * 用于与 MCP（子）注册表交互的接口。
 * 有关 API 的更多细节，请参阅 <a href="https://registry.modelcontextprotocol.io/docs#/">官方参考文档</a>。
 * 该接口与官方 API 保持严格镜像对齐。
 */
public interface McpRegistryClient {

    /**
     * Obtains a list of MCP servers from the registry.
     */
    /**
     * 从注册表中获取 MCP 服务器列表。
     */
    McpServerList listServers(McpServerListRequest request);

    /**
     * Obtains the details for a single MCP server denoted by its ID.
     * @deprecated This method is not supported on the official MCP registry anymore.
     */
    /**
     * 根据服务器 ID 获取单个 MCP 服务器的详细信息。
     * @deprecated 该方法已不再被官方 MCP 注册表支持。
     */
    @Deprecated(forRemoval = true)
    McpGetServerResponse getServerDetails(String id);

    /**
     * Get detailed information about a specific version of an MCP server.
     * Use the special version 'latest' to get the latest version.
     */
    /**
     * 获取MCP服务器指定版本的详细信息。
     * 使用特殊版本号'latest'可获取最新版本。
     */
    McpGetServerResponse getSpecificServerVersion(String serverName, String version);

    /**
     * Get all available versions for a specific MCP server
     * 获取指定MCP服务器的所有可用版本。
     */
    McpServerList getAllVersionsOfServer(String serverName);

    /**
     * Runs a health check against the MCP registry. If the registry is healthy,
     * the returned object's "status" field will be "ok".
     */
    /**
     * 对 MCP 注册表执行健康检查。
     * 如果注册表状态正常，返回对象的 "status" 字段值为 "ok"。
     */
    McpRegistryHealth healthCheck();

    /**
     * Sends a ping message to the MCP registry. If the ping is successful,
     * the 'pong' field in the response object will contain 'true'.
     */
    /**
     * 向 MCP 注册表发送 ping 消息。
     * 如果 ping 成功，响应对象中的 'pong' 字段将包含 'true'。
     */
    McpRegistryPong ping();
}
