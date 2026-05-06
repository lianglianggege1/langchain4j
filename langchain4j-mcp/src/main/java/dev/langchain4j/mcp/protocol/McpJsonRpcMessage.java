package dev.langchain4j.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.langchain4j.Internal;

/**
 * Corresponds to the {@code JSONRPCMessage} type from the MCP schema.
 */
/**
 * 对应 MCP 协议中的 JSONRPCMessage 类型。
 */
@Internal
public class McpJsonRpcMessage {

    @JsonInclude
    public final String jsonrpc = "2.0";

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long id;

    public McpJsonRpcMessage(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }
}
