package dev.langchain4j.rag;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.rag.query.Metadata;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

/**
 * Represents a request for {@link ChatMessage} augmentation.
 * 表示一个{@link ChatMessage}  增强请求
 */
public class AugmentationRequest {

    /**
     * The chat message to be augmented.
     * 要增强 的聊天消息
     * Currently, only {@link UserMessage} is supported.
     * 目前只支持{@link UserMessage}
     */
    private final ChatMessage chatMessage;

    /**
     * Additional metadata related to the augmentation request.
     * 增强请求相关的附加元数据
     */
    private final Metadata metadata;

    public AugmentationRequest(ChatMessage chatMessage, Metadata metadata) {
        this.chatMessage = ensureNotNull(chatMessage, "chatMessage");
        this.metadata = ensureNotNull(metadata, "metadata");
    }

    public ChatMessage chatMessage() {
        return chatMessage;
    }

    public Metadata metadata() {
        return metadata;
    }
}
