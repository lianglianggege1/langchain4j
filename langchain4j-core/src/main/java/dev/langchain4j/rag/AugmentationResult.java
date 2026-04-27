package dev.langchain4j.rag;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.rag.content.Content;

import java.util.List;

import static dev.langchain4j.internal.Utils.copy;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

/**
 * Represents the result of a {@link ChatMessage} augmentation.
 * 代表一个 {@link ChatMessage} 的增强结果。
 */
public class AugmentationResult {

    /**
     * The augmented chat message.
     * 增强的聊天消息
     */
    private final ChatMessage chatMessage;

    /**
     * A list of content used to augment the original chat message.
     * 用于增强原始聊天消息的内容列表。
     */
    private final List<Content> contents;

    public AugmentationResult(ChatMessage chatMessage, List<Content> contents) {
        this.chatMessage = ensureNotNull(chatMessage, "chatMessage");
        this.contents = copy(contents);
    }

    public static AugmentationResultBuilder builder() {
        return new AugmentationResultBuilder();
    }

    public ChatMessage chatMessage() {
        return chatMessage;
    }

    public List<Content> contents() {
        return contents;
    }

    public static class AugmentationResultBuilder {

        private ChatMessage chatMessage;
        private List<Content> contents;

        AugmentationResultBuilder() {
        }

        public AugmentationResultBuilder chatMessage(ChatMessage chatMessage) {
            this.chatMessage = chatMessage;
            return this;
        }

        public AugmentationResultBuilder contents(List<Content> contents) {
            this.contents = contents;
            return this;
        }

        public AugmentationResult build() {
            return new AugmentationResult(this.chatMessage, this.contents);
        }
    }
}
