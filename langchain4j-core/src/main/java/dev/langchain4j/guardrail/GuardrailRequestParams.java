package dev.langchain4j.guardrail;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.observability.api.AiServiceListenerRegistrar;
import dev.langchain4j.rag.AugmentationResult;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the common parameters shared across guardrail checks when validating interactions
 * between a user and a language model. This class encapsulates the chat memory, user message
 * template, and additional variables required for guardrail processing.
 */
/**
 * 表示在校验用户与大语言模型之间的交互时，
 * 所有护栏校验过程共享的通用参数。
 * 该类封装了护栏处理所需的对话上下文、用户消息模板
 * 以及其他附加变量。
 */
public final class GuardrailRequestParams {

    private final ChatMemory chatMemory;
    private final AugmentationResult augmentationResult;
    private final String userMessageTemplate;
    private final Map<String, Object> variables;
    private final InvocationContext invocationContext;
    private final AiServiceListenerRegistrar aiServiceListenerRegistrar;

    private GuardrailRequestParams(Builder builder) {
        this.chatMemory = builder.chatMemory;
        this.augmentationResult = builder.augmentationResult;
        this.userMessageTemplate = ensureNotNull(builder.userMessageTemplate, "userMessageTemplate");
        this.variables = ensureNotNull(builder.variables, "variables");
        this.invocationContext = builder.invocationContext;
        this.aiServiceListenerRegistrar = Optional.ofNullable(builder.aiServiceListenerRegistrar)
                .orElseGet(AiServiceListenerRegistrar::newInstance);
    }

    /**
     * Returns the chat memory.
     *
     * @return the chat memory, may be null
     */
    /**
     * 获取对话记忆（聊天上下文）。
     *
     * @return 对话记忆对象，可能为 null
     */
    public ChatMemory chatMemory() {
        return chatMemory;
    }

    /**
     * Returns the augmentation result.
     *
     * @return the augmentation result, may be null
     */
    /**
     * 获取增强处理结果。
     *
     * @return 增强结果对象，可能为 null
     */
    public AugmentationResult augmentationResult() {
        return augmentationResult;
    }

    /**
     * Returns the user message template.
     *
     * @return the user message template, never null
     */
    /**
     * 获取用户消息模板。
     *
     * @return 用户消息模板，永不为 null
     */
    public String userMessageTemplate() {
        return userMessageTemplate;
    }

    /**
     * Returns the variables.
     *
     * @return the variables, never null
     */
    /**
     * 获取变量集合。
     *
     * @return 变量集合，永不为 null
     */
    public Map<String, Object> variables() {
        return variables;
    }

    /**
     * Returns the {@link InvocationContext}, which contains general information about the AI Service invocation.
     *
     * @return the invocation context
     */
    /**
     * 获取调用上下文（InvocationContext），该上下文包含AI服务调用的通用信息。
     *
     * @return AI服务调用上下文对象
     */
    public InvocationContext invocationContext() {
        return invocationContext;
    }

    /**
     * Returns the {@link AiServiceListenerRegistrar}, which provides
     * functionality for registering, unregistering, and notifying listeners of AI
     * service invocation events.
     *
     * @return the {@link AiServiceListenerRegistrar}
     */
    /**
     * 获取 AI 服务监听器注册器（AiServiceListenerRegistrar），
     * 该对象提供注册、注销监听器以及通知 AI 服务调用事件的功能。
     *
     * @return AI 服务监听器注册器实例
     */
    public AiServiceListenerRegistrar aiservicelistenerregistrar() {
        return aiServiceListenerRegistrar;
    }

    /**
     * Converts the current {@link GuardrailRequestParams} instance to a builder,
     * allowing modifications to the current state or creation of a new modified object.
     *
     * @return a {@link Builder} pre-populated with the current state's values
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Creates a new builder for {@link GuardrailRequestParams}.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link GuardrailRequestParams}.
     */
    public static class Builder {
        private ChatMemory chatMemory;
        private AugmentationResult augmentationResult;
        private String userMessageTemplate;
        private Map<String, Object> variables;
        private InvocationContext invocationContext;
        private AiServiceListenerRegistrar aiServiceListenerRegistrar;

        public Builder() {}

        public Builder(GuardrailRequestParams src) {
            this.chatMemory = src.chatMemory;
            this.augmentationResult = src.augmentationResult;
            this.userMessageTemplate = src.userMessageTemplate;
            this.variables = src.variables;
            this.invocationContext = src.invocationContext;
            this.aiServiceListenerRegistrar = src.aiServiceListenerRegistrar;
        }

        /**
         * Sets the chat memory.
         *
         * @param chatMemory the chat memory
         * @return this builder
         */
        public Builder chatMemory(ChatMemory chatMemory) {
            this.chatMemory = chatMemory;
            return this;
        }

        /**
         * Sets the augmentation result.
         *
         * @param augmentationResult the augmentation result
         * @return this builder
         */
        public Builder augmentationResult(AugmentationResult augmentationResult) {
            this.augmentationResult = augmentationResult;
            return this;
        }

        /**
         * Sets the user message template.
         *
         * @param userMessageTemplate the user message template
         * @return this builder
         */
        public Builder userMessageTemplate(String userMessageTemplate) {
            this.userMessageTemplate = userMessageTemplate;
            return this;
        }

        /**
         * Sets the variables.
         *
         * @param variables the variables
         * @return this builder
         */
        public Builder variables(Map<String, Object> variables) {
            this.variables = variables;
            return this;
        }

        /**
         * Sets the invocation context for the builder.
         *
         * @param invocationContext the invocation context, containing details such as the method name,
         *                          interface name, and timestamp of the invocation
         * @return this builder instance, to allow for method chaining
         */
        public Builder invocationContext(InvocationContext invocationContext) {
            this.invocationContext = invocationContext;
            return this;
        }

        /**
         * Sets the AI service invocation event listener registrar.
         *
         * @param aiServiceListenerRegistrar the registrar used to register and manage
         *                                   AI service invocation event listeners
         * @return this builder instance, to allow for method chaining
         */
        public Builder aiServiceListenerRegistrar(AiServiceListenerRegistrar aiServiceListenerRegistrar) {
            this.aiServiceListenerRegistrar = aiServiceListenerRegistrar;
            return this;
        }

        /**
         * Builds a new {@link GuardrailRequestParams}.
         *
         * @return a new {@link GuardrailRequestParams}
         */
        public GuardrailRequestParams build() {
            return new GuardrailRequestParams(this);
        }
    }
}
