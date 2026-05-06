package dev.langchain4j.mcp.protocol;

import dev.langchain4j.Internal;
import java.util.Map;

/**
 * Corresponds to the {@code GetPromptRequest} type from the MCP schema.
 */
/**
 * 对应 MCP 协议中的 GetPromptRequest 类型。
 */
@Internal
public class McpGetPromptRequest extends McpClientRequest {

    public McpGetPromptRequest(Long id, String promptName, Map<String, Object> arguments) {
        super(id, McpClientMethod.PROMPTS_GET);
        setParams(new McpGetPromptParams(promptName, arguments));
    }
}
