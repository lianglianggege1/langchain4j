package dev.langchain4j.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.langchain4j.Internal;
import org.jspecify.annotations.NonNull;

/**
 * Corresponds to the {@code params} of the {@code CancelledNotification} type from the MCP schema.
 */
/**
 * 对应 MCP 协议中 CancelledNotification 类型的 params 参数。
 */
@Internal
public class McpCancellationParams extends McpClientParams {

    private Long requestId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String reason;

    public McpCancellationParams() {}

    public McpCancellationParams(@NonNull Long requestId, String reason) {
        this.requestId = requestId;
        this.reason = reason;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
