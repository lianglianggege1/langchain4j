package dev.langchain4j.mcp.client;

import com.fasterxml.jackson.databind.JsonNode;
import dev.langchain4j.service.tool.ToolExecutionResult;

/**
 * Extracts a {@link ToolExecutionResult} from a tool response {@code content[]} array.
 * <p>
 * This extension point is only used for ordinary MCP tool responses that return
 * {@code CallToolResult.result.content[]}. It is not invoked when the MCP server
 * returns {@code structuredContent}, which is handled separately.
 * <p>
 * This interface is not a general-purpose MCP content parsing framework. The default
 * client only supports {@code structuredContent} and text content out of the box.
 * More specialized extraction strategies can be provided through
 * {@link DefaultMcpClient.Builder#toolResultExtractor(McpToolResultExtractor)}.
 */
/**
 * 从工具响应的 {@code content[]} 数组中提取 {@link ToolExecutionResult}。
 * <p>
 * 此扩展点仅用于返回 {@code CallToolResult.result.content[]} 的普通 MCP 工具响应。
 * 当 MCP 服务器返回 {@code structuredContent} 时不会触发该方法，结构化内容会单独处理。
 * <p>
 * 该接口并非通用的 MCP 内容解析框架。默认客户端开箱即用，仅支持 {@code structuredContent}
 * 和文本内容。
 * 可通过 {@link DefaultMcpClient.Builder#toolResultExtractor(McpToolResultExtractor)}
 * 提供更专业的提取策略。
 */
public interface McpToolResultExtractor {

    /**
     * Extracts a {@link ToolExecutionResult} from {@code CallToolResult.result.content[]}.
     *
     * @param content the MCP tool result content array.
     * @param isError whether the tool response is marked as an application-level error.
     * @return the extracted {@link ToolExecutionResult}.
     */
    /**
     * 从 {@code CallToolResult.result.content[]} 中提取 {@link ToolExecutionResult}。
     *
     * @param content MCP 工具返回的内容数组
     * @param isError 工具响应是否被标记为**应用级错误**
     * @return 提取完成的 {@link ToolExecutionResult} 结果
     */
    ToolExecutionResult extract(JsonNode content, boolean isError);
}
