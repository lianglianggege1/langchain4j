package dev.langchain4j.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.langchain4j.Internal;
import java.util.Map;

/**
 * Corresponds to the {@code params} of the {@code JSONRPCRequest} type from the MCP schema.
 */
/**
 * 对应 MCP 协议中 JSONRPCRequest 类型的 params 参数。
 */
@Internal
public class McpClientParams {

    @JsonProperty("_meta")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Object> meta;

    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }
}
