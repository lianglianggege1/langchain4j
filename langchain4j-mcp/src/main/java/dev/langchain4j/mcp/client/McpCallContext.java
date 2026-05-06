package dev.langchain4j.mcp.client;

import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.mcp.protocol.McpClientMessage;
import org.jspecify.annotations.Nullable;

/**
 * Context information for any invocation made towards an MCP server.
 *
 * It contains the AI service invocation context when this is during
 * an AI service invocation (in other cases, the invocation context is null).
 */
/**
 * 面向 MCP 服务器发起的任意调用的上下文信息。
 *
 * 当处于 AI 服务调用期间时，包含 AI 服务调用上下文
 *（其他情况下，调用上下文为 null）。
 */
public record McpCallContext(@Nullable InvocationContext invocationContext, McpClientMessage message) {}
