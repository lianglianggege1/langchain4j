package dev.langchain4j.mcp.client.transport;

import com.fasterxml.jackson.databind.JsonNode;
import dev.langchain4j.mcp.client.McpCallContext;
import dev.langchain4j.mcp.protocol.McpClientMessage;
import dev.langchain4j.mcp.protocol.McpInitializeRequest;
import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

public interface McpTransport extends Closeable {

    /**
     * Creates a connection to the MCP server (runs the server as a subprocess if needed).
     * This does NOT yet send the "initialize" message to negotiate capabilities.
     */
    /**
     * 建立到 MCP 服务器的连接（如有需要，会将服务器作为子进程启动）。
     * 此方法**不会**发送 "initialize" 初始化消息来协商能力。
     */
    void start(McpOperationHandler messageHandler);

    /**
     * Sends the "initialize" message to the MCP server to negotiate
     * capabilities, supported protocol version etc. When this method
     * returns successfully, the transport is fully initialized and ready to
     * be used. This has to be called AFTER the "start" method.
     * Only used with the legacy MCP protocol (versions up to 2025-11-25).
     * Modern protocol uses {@code server/discover} instead.
     */
    /**
     * 向 MCP 服务器发送 "initialize" 初始化消息，用于协商双方能力、支持的协议版本等。
     * 当此方法成功返回时，传输通道已**完全初始化**，可以正常使用。
     * 必须在 "start" 方法**之后**调用此方法。
     */
    CompletableFuture<JsonNode> initialize(McpInitializeRequest request);

    /**
     * Executes an operation that expects a response from the server.
     */
    /**
     * 执行一个**需要等待服务器返回响应**的操作。
     */
    CompletableFuture<JsonNode> executeOperationWithResponse(McpClientMessage request);

    /**
     * Executes an operation that expects a response from the server.
     */
    /**
     * 执行一个**需要服务器返回响应**的操作。
     */
    CompletableFuture<JsonNode> executeOperationWithResponse(McpCallContext context);

    /**
     * Sends a message that does not expect a response from the server - either a
     * client-initiated notification or a response to a server-initiated request.
     */
    /**
     * 发送一条**不需要服务器响应**的消息 ——
     * 可以是客户端主动发起的通知，也可以是对服务端请求的响应。
     */
    void executeOperationWithoutResponse(McpClientMessage request);

    /**
     * Sends a message that does not expect a response from the server - either a
     * client-initiated notification or a response to a server-initiated request.
     */
    /**
     * 发送一条**无需等待服务器响应**的消息：
     * 既可以是客户端主动发送的通知，也可以是对服务端发起请求的响应。
     */
    void executeOperationWithoutResponse(McpCallContext context);

    /**
     * Performs transport-specific health checks, if applicable. This is called
     * by `McpClient.checkHealth()` as the first check before performing a check
     * by sending a 'ping' over the MCP protocol. The purpose is that the
     * transport may have some specific and faster ways to detect that it is broken,
     * like for example, the STDIO transport can fail the check if it detects
     * that the server subprocess isn't alive anymore.
     */
    /**
     * 执行与传输层相关的健康检查（如适用）。
     * 该方法会被 `McpClient.checkHealth()` 调用，作为通过 MCP 协议发送 "ping" 检查之前的**第一项检查**。
     * 其设计目的是：传输层可以通过更专用、更快速的方式判断连接已断开，
     * 例如 STDIO 传输层如果检测到服务器子进程已不再存活，可直接判定检查失败。
     */
    void checkHealth();

    void onFailure(Runnable actionOnFailure);

    /**
     * Informs the transport whether the modern MCP protocol (2026-07-28 or later) is in use.
     * HTTP-based transports use this to switch between modern headers (MCP-Protocol-Version,
     * Mcp-Method, Mcp-Name) and legacy session management (Mcp-Session-Id).
     * Default implementation is a no-op (for transports like stdio/WebSocket that don't need it).
     */
    default void setModernProtocol(boolean modernProtocol) {}

    /**
     * Sets the protocol version string to be sent in the MCP-Protocol-Version HTTP header.
     * Only relevant for HTTP-based transports in modern protocol mode.
     * Default implementation is a no-op.
     */
    default void setProtocolVersion(String protocolVersion) {}

    /**
     * Returns whether this transport requires an explicit {@code notifications/cancelled}
     * message to cancel an in-progress request. Transports that use per-request SSE
     * streams (like Streamable HTTP) cancel by closing the stream and should return
     * {@code false}. Transports that share a single channel (like stdio) must return
     * {@code true} so the server knows which request to cancel.
     * <p>
     * Note: when using the legacy protocol (2025-11-25), the client always sends
     * {@code notifications/cancelled} regardless of this value, because the legacy
     * spec states that disconnection should not be interpreted as cancellation.
     * <p>
     * Defaults to {@code true}, which is the safe answer for a transport that shares a
     * single channel; per-request-stream transports override it.
     */
    default boolean requiresCancellationNotification() {
        return true;
    }
}
