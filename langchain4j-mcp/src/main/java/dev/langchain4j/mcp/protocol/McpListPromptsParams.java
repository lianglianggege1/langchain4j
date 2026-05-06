package dev.langchain4j.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.langchain4j.Internal;

/**
 * Corresponds to the {@code params} of the {@code ListPromptsRequest} type from the MCP schema.
 */
/**
 * 对应 MCP 协议中 ListPromptsRequest 类型的 params 参数。
 */
@Internal
public class McpListPromptsParams extends McpClientParams {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String cursor;

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }
}
