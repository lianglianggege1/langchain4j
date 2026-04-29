package dev.langchain4j.model.chat.response;

import dev.langchain4j.internal.JacocoIgnoreCoverageGenerated;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;

import java.util.Objects;

/**
 * Represents common chat response metadata supported by most LLM providers.
 * 表示大多数大语言模型服务商所支持的通用聊天响应元数据。
 * Specific LLM provider integrations can extend this interface to add provider-specific metadata.
 * 特定大语言模型提供商的集成可扩展此接口，以添加提供商专属元数据。
 */
@JacocoIgnoreCoverageGenerated
public class ChatResponseMetadata {

    private final String id;
    private final String modelName;
    private final TokenUsage tokenUsage;
    private final FinishReason finishReason;

    protected ChatResponseMetadata(Builder<?> builder) {
        this.id = builder.id;
        this.modelName = builder.modelName;
        this.tokenUsage = builder.tokenUsage;
        this.finishReason = builder.finishReason;
    }

    public String id() {
        return id;
    }

    public String modelName() {
        return modelName;
    }

    public TokenUsage tokenUsage() {
        return tokenUsage;
    }

    public FinishReason finishReason() {
        return finishReason;
    }

    public Builder<?> toBuilder() {
        return toBuilder(builder());
    }

    protected Builder<?> toBuilder(Builder<?> builder) {
        return builder
                .id(id)
                .modelName(modelName)
                .tokenUsage(tokenUsage)
                .finishReason(finishReason);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatResponseMetadata that = (ChatResponseMetadata) o;
        return Objects.equals(id, that.id)
                && Objects.equals(modelName, that.modelName)
                && Objects.equals(tokenUsage, that.tokenUsage)
                && Objects.equals(finishReason, that.finishReason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, modelName, tokenUsage, finishReason);
    }

    @Override
    public String toString() {
        return "ChatResponseMetadata{" +
                "id='" + id + '\'' +
                ", modelName='" + modelName + '\'' +
                ", tokenUsage=" + tokenUsage +
                ", finishReason=" + finishReason +
                '}';
    }

    public static Builder<?> builder() {
        return new Builder<>();
    }

    public static class Builder<T extends Builder<T>> {

        private String id;
        private String modelName;
        private TokenUsage tokenUsage;
        private FinishReason finishReason;

        public T id(String id) {
            this.id = id;
            return (T) this;
        }

        public T modelName(String modelName) {
            this.modelName = modelName;
            return (T) this;
        }

        public T tokenUsage(TokenUsage tokenUsage) {
            this.tokenUsage = tokenUsage;
            return (T) this;
        }

        public T finishReason(FinishReason finishReason) {
            this.finishReason = finishReason;
            return (T) this;
        }

        public ChatResponseMetadata build() {
            return new ChatResponseMetadata(this);
        }
    }
}
