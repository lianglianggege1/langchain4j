package dev.langchain4j.mcp.client;

import com.fasterxml.jackson.databind.JsonNode;
import dev.langchain4j.service.tool.ToolExecutionResult;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Default extractor for MCP tool responses backed by {@code content[]}.
 * <p>
 * This implementation preserves the existing client behavior: it only supports
 * {@code content} items of type {@code text}, joins multiple text fragments with
 * newline characters, and stores the result in {@link ToolExecutionResult#resultText()}.
 * It does not apply to responses that contain {@code structuredContent}.
 */
/**
 * 基于 {@code content[]} 的 MCP 工具响应默认提取器。
 * <p>
 * 此实现保留现有客户端行为：仅支持
 * {@code text} 类型的 {@code content} 项，使用换行符
 * 拼接多个文本片段，并将结果存入 {@link ToolExecutionResult#resultText()}。
 * 不适用于包含 {@code structuredContent} 的响应。
 */
public class DefaultMcpToolResultExtractor implements McpToolResultExtractor {

    @Override
    public ToolExecutionResult extract(JsonNode content, boolean isError) {
        String resultText = StreamSupport.stream(content.spliterator(), false)
                .map(this::extractText)
                .collect(Collectors.joining("\n"));

        return ToolExecutionResult.builder()
                .isError(isError)
                .resultText(resultText)
                .build();
    }

    private String extractText(JsonNode contentItem) {
        if (!contentItem.get("type").asText().equals("text")) {
            // Preserve the historical error message format from ToolExecutionHelper,
            // where the JSON string value is rendered with quotes.
            throw new RuntimeException("Unsupported content type: " + contentItem.get("type"));
        }
        return contentItem.get("text").asText();
    }
}
