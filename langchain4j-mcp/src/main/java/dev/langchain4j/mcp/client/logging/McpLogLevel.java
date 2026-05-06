package dev.langchain4j.mcp.client.logging;

/**
 * Log level of an MCP log message.
 */
/**
 * MCP 日志消息的日志级别。
 */
public enum McpLogLevel {
    DEBUG,
    INFO,
    NOTICE,
//    通知级别
    WARNING,
    ERROR,
//    严重级别（最高优先级）
    CRITICAL,
//    警报级别
    ALERT,
//    紧急级别（最高级）
    EMERGENCY;

    public static McpLogLevel from(String val) {
        if (val == null || val.isBlank()) {
            return null;
        }
        try {
            return valueOf(val.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}
