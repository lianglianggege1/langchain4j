package dev.langchain4j.model.chat;

import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.json.JsonSchema;

/**
 * Represents a capability of a {@link ChatModel} or {@link StreamingChatModel}.
 * This is required for the low-level {@link ChatModel} or {@link StreamingChatModel} API
 * to communicate to the high-level APIs (like AI Service) what capabilities are supported and can be utilized.
 */
/**
 * 表示 {@link ChatModel} 或 {@link StreamingChatModel} 具备的一项能力。
 * 底层的 {@link ChatModel} 与 {@link StreamingChatModel} API 需要通过该标识，
 * 向高层 API（如 AI 服务）告知自身支持哪些可用能力。
 */
public enum Capability {

    /**
     * Indicates whether {@link ChatModel} or {@link StreamingChatModel}
     * supports responding in JSON format according to the specified JSON schema.
     * 指示 {@link ChatModel} 或 {@link StreamingChatModel} 是否支持**按照指定的 JSON 模式以 JSON 格式返回响应**。
     *
     * @see ResponseFormat
     * @see JsonSchema
     */
    RESPONSE_FORMAT_JSON_SCHEMA
}
