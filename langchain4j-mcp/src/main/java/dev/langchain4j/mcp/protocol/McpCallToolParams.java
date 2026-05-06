package dev.langchain4j.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.langchain4j.Internal;

/**
 * Corresponds to the {@code params} of the {@code CallToolRequest} type from the MCP schema.
 */
/**
 * 对应 MCP 协议中 CallToolRequest 类型的 params 参数。
 */
@Internal
public class McpCallToolParams extends McpClientParams {

    private String name;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    private ObjectNode arguments;

    public McpCallToolParams() {}

    public McpCallToolParams(String name, ObjectNode arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ObjectNode getArguments() {
        return arguments;
    }

    public void setArguments(ObjectNode arguments) {
        this.arguments = arguments;
    }
}
