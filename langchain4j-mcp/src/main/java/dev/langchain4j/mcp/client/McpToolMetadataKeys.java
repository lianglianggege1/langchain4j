package dev.langchain4j.mcp.client;

/**
 * Constants for MCP metadata keys, as defined in the MCP specification.
 * These constants are used as keys in MCP metadata maps, including
 * {@link dev.langchain4j.agent.tool.ToolSpecification#metadata()}.
 */
/**
 * MCP 规范中定义的 MCP 工具注解键常量。
 * 这些常量在 {@link dev.langchain4j.agent.tool.ToolSpecification} 内部的元数据映射中用作键。
 */
public class McpToolMetadataKeys {

    /**
     * A human-readable title for the tool as retrieved from the tool annotations,
     * as opposed to the title that is stored in the Tool definition directly.
     * See <a href="https://github.com/modelcontextprotocol/modelcontextprotocol/blob/2025-06-18/schema/2025-06-18/schema.json#L2457">schema.json</a>
     * Value type: String
     */
    /**
     * 从工具注解中获取的、便于人类阅读的工具标题，
     * 与直接存储在工具定义中的标题不同。
     * 详见 <a href="https://github.com/modelcontextprotocol/modelcontextprotocol/blob/2025-06-18/schema/2025-06-18/schema.json#L2457">schema.json</a>
     * 值类型：字符串
     */
    public static final String TITLE_ANNOTATION = "title-annotation";

    /**
     * A human-readable title for the tool retrieved from the Tool definition directly,
     * as opposed to the title that is stored in the annotations.
     * See <a href="https://github.com/modelcontextprotocol/modelcontextprotocol/blob/2025-06-18/schema/2025-06-18/schema.json#L67">schema.json</a>
     * Value type: String
     */
    /**
     * 直接从工具定义中获取的、便于人类阅读的工具标题，
     * 与存储在注解中的标题相区分。
     * 详见 <a href="https://github.com/modelcontextprotocol/modelcontextprotocol/blob/2025-06-18/schema/2025-06-18/schema.json#L67">schema.json</a>
     * 值类型：字符串
     */
    public static final String TITLE = "title";

    /**
     * Icons associated with an MCP tool when represented as a
     * {@link dev.langchain4j.agent.tool.ToolSpecification}.
     * Value type: {@code List<McpIcon>}
     */
    public static final String ICONS = "icons";

    /**
     * Indicates whether the tool is read-only.
     * Value type: boolean
     */
    /**
     * 标识该工具是否为只读类型。
     * 值类型：布尔值
     */
    public static final String READ_ONLY_HINT = "readOnlyHint";

    /**
     * Indicates whether the tool is destructive.
     * Value type: boolean
     */
    /**
     * 标识该工具是否具有破坏性。
     * 值类型：布尔值
     */
    public static final String DESTRUCTIVE_HINT = "destructiveHint";

    /**
     * Indicates whether the tool is idempotent.
     * Value type: boolean
     */
    /**
     * 标识该工具是否具备幂等性。
     * 值类型：布尔值
     */
    public static final String IDEMPOTENT_HINT = "idempotentHint";

    /**
     * Indicates whether the tool may interact with the open world (like internet resources).
     * Value type: boolean
     */
    /**
     * 标识该工具是否可以与外部开放环境（如网络资源）进行交互。
     * 值类型：布尔值
     */
    public static final String OPEN_WORLD_HINT = "openWorldHint";

    /**
     * The JSON schema describing the structured output of the tool, as declared in the Tool definition.
     * Paired with {@code structuredContent} in tool responses.
     * See <a href="https://github.com/modelcontextprotocol/modelcontextprotocol/blob/2025-06-18/schema/2025-06-18/schema.json">schema.json</a>
     * Value type: {@code Map<String, Object>}
     */
    public static final String OUTPUT_SCHEMA = "outputSchema";
}
