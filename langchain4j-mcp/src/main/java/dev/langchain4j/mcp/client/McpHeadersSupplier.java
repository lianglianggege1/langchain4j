package dev.langchain4j.mcp.client;

import java.util.Map;
import java.util.function.Function;

/**
 * A functional interface that supplies HTTP headers for MCP client requests
 * based on the given {@link McpCallContext}.
 */
/**
 * 函数式接口：基于传入的 {@link McpCallContext}，
 * 为 MCP 客户端请求提供 HTTP 请求头。
 */
@FunctionalInterface
public interface McpHeadersSupplier extends Function<McpCallContext, Map<String, String>> {}
