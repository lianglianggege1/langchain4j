package dev.langchain4j.model.output;

/**
 * The reason why a model call finished.
 */
public enum FinishReason {
    /**
     * The model call finished because the model decided the request was done.
     * 模型调用已结束，因模型判定请求已处理完成。
     */
    STOP,

    /**
     * The call finished because the token length was reached.
     * 会话已结束，已达到令牌长度上限。
     */
    LENGTH,

    /**
     * The call finished signalling a need for tool execution.
     * 调用已结束，需执行工具操作。
     */
    TOOL_EXECUTION,

    /**
     * The call finished signalling a need for content filtering.
     * 调用结束，触发内容过滤需求。
     */
    CONTENT_FILTER,

    /**
     * The call finished for some other reason.
     * 调用因其他原因结束。
     */
    OTHER
}
