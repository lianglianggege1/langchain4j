package dev.langchain4j.agentic.declarative;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a static method that returns the McpClient instance for declarative MCP client agents.
 * 标记一个静态方法，该方法返回声明性MCP客户端代理的McpClient实例。
 */
@Retention(RUNTIME)
@Target({METHOD})
public @interface McpClientSupplier {
}
