package dev.langchain4j.mcp.protocol;

import dev.langchain4j.Internal;

/**
 * Corresponds to the {@code JSONRPCResponse} type from the MCP schema.
 */
/**
 * 对应 MCP 协议中的 JSONRPCResponse 类型。
 */
@Internal
public class McpClientResponse extends McpClientMessage {

    public McpClientResponse(Long id) {
        super(id, null);
    }
}
