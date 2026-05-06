package dev.langchain4j.mcp.client.logging;

/**
 * A handler that decides what to do with received log messages from an MCP
 * server.
 */
/**
 * 处理器接口：用于处理、决定如何消费从 MCP 服务器接收到的日志消息。
 */
public interface McpLogMessageHandler {

    void handleLogMessage(McpLogMessage message);
}
