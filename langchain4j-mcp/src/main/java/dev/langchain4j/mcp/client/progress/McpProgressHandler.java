package dev.langchain4j.mcp.client.progress;

/**
 * Handler for MCP progress notifications.
 * Implement this interface to receive progress updates from the MCP server
 * during long-running tool executions.
 */
/**
 * MCP 进度通知处理器。
 * 实现该接口可在长时间运行的工具执行过程中，
 * 接收来自 MCP 服务器的进度更新通知。
 */
public interface McpProgressHandler {

    /**
     * Called when a progress notification is received from the MCP server.
     * 接收到来自 MCP 服务器的进度通知时调用。
     *
     * @param notification the progress notification
     */
    void onProgress(McpProgressNotification notification);
}
