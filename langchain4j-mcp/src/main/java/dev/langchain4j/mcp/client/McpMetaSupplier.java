package dev.langchain4j.mcp.client;

import java.util.Map;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

/**
 * A functional interface that supplies {@code _meta} fields for MCP client
 * requests and notifications based on the given {@link McpCallContext}.
 * Unlike HTTP headers, this applies to all transports.
 */
/**
 * 函数式接口：基于传入的 {@link McpCallContext}，
 * 为 MCP 客户端的**请求和通知**提供 `_meta` 元数据字段。
 * 与 HTTP 请求头不同，该元数据适用于**所有传输协议**。
 */
@FunctionalInterface
public interface McpMetaSupplier extends Function<@Nullable McpCallContext, Map<String, Object>> {}
