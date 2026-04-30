package dev.langchain4j.guardrail;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.observability.api.AiServiceListenerRegistrar;
import dev.langchain4j.observability.api.event.AiServiceEvent;
import dev.langchain4j.observability.api.listener.AiServiceListener;
import java.util.List;
import java.util.function.Consumer;

/**
 * Generic executor interface that defines a chat interaction
 */
/**
 * 定义聊天交互行为的通用执行器接口。
 */
public interface ChatExecutor {

    /**
     * Execute a chat request
     * @return The response
     */
    /**
     * 执行聊天请求
     * @return 聊天响应
     */
    ChatResponse execute();

    /**
     * Executes a chat request using the provided chat messages
     * @param chatMessages The chat messages containing the context of the conversation.
     *                     It provides the history of messages required for proper interaction with the chat model
     * @return A response object containing the AI's response and additional metadata.
     */
    /**
     * 使用提供的聊天消息执行聊天请求
     * @param chatMessages 包含对话上下文的聊天消息
     *                     提供与聊天模型正常交互所需的消息历史记录
     * @return 包含AI响应和额外元数据的响应对象
     */
    ChatResponse execute(List<ChatMessage> chatMessages);

    /**
     * Creates a new {@link SynchronousBuilder} instance for constructing {@link ChatExecutor} objects
     * that perform synchronous chat requests.
     *
     * @return A new {@link SynchronousBuilder} instance to configure and build a {@link ChatExecutor}.
     */
    /**
     * 创建一个新的 {@link SynchronousBuilder} 实例，用于构建执行**同步**聊天请求的 {@link ChatExecutor} 对象。
     *
     * @return 用于配置并构建 {@link ChatExecutor} 的全新 {@link SynchronousBuilder} 实例。
     */
    static SynchronousBuilder builder(ChatModel chatModel) {
        return new SynchronousBuilder(chatModel);
    }

    /**
     * Creates a new {@link StreamingToSynchronousBuilder} instance for constructing {@link ChatExecutor} objects
     * that perform streaming chat requests.
     *
     * @return A new {@link StreamingToSynchronousBuilder} instance to configure and build a {@link ChatExecutor}.
     */
    /**
     * 创建一个新的 {@link StreamingToSynchronousBuilder} 实例，用于构建执行**流式**聊天请求的 {@link ChatExecutor} 对象。
     *
     * @return 用于配置并构建 {@link ChatExecutor} 的全新 {@link StreamingToSynchronousBuilder} 实例。
     */
    static StreamingToSynchronousBuilder builder(StreamingChatModel streamingChatModel) {
        return new StreamingToSynchronousBuilder(streamingChatModel);
    }

    /**
     * An abstract base-builder class for constructing instances of {@link ChatExecutor}.
     *
     * This class provides a fluent API for setting required components, such as
     * {@link ChatRequest}, and defines a contract for building {@link ChatExecutor}
     * instances. Subclasses should implement the {@code build()} method to ensure
     * proper construction of the target chat executor object.
     *
     * @param <T> the type of the builder subclass for enabling fluent method chaining
     */
    /**
     * 用于构建 {@link ChatExecutor} 实例的抽象基础构建器类。
     *
     * 此类提供流式 API 用于设置必需组件（例如 {@link ChatRequest}），
     * 并定义了创建 {@link ChatExecutor} 实例的约定。
     * 子类应实现 {@code build()} 方法，以确保目标聊天执行器对象被正确构建。
     *
     * @param <T> 构建器子类类型，用于支持流式方法链式调用
     */
    abstract class AbstractBuilder<T extends AbstractBuilder<T>> {
        protected ChatRequest chatRequest;
        protected InvocationContext invocationContext;
        protected AiServiceListenerRegistrar eventListenerRegistrar;

        protected AbstractBuilder() {}

        /**
         * Sets the {@link ChatRequest} instance for the synchronousBuilder.
         * The {@link ChatRequest} encapsulates the input messages and parameters required
         * to generate a response from the chat model.
         *
         * @param chatRequest the {@link ChatRequest} containing the input messages and parameters
         * @return the updated SynchronousBuilder instance
         */
        /**
         * 为同步构建器设置 {@link ChatRequest} 实例。
         * {@link ChatRequest} 封装了从聊天模型生成响应所需的输入消息和参数。
         *
         * @param chatRequest 包含输入消息和参数的 {@link ChatRequest}
         * @return 更新后的同步构建器实例
         */
        public AbstractBuilder<T> chatRequest(ChatRequest chatRequest) {
            this.chatRequest = chatRequest;
            return this;
        }

        /**
         * Sets the {@link InvocationContext} instance for the builder.
         * The {@link InvocationContext} provides contextual information
         * that can be used during the execution of the chat request.
         *
         * @param invocationContext the {@link InvocationContext} containing contextual information
         * @return the updated builder instance of type {@code T} for method chaining
         */
        /**
         * 为构建器设置 {@link InvocationContext} 实例。
         * {@link InvocationContext} 提供聊天请求执行过程中可使用的上下文信息。
         *
         * @param invocationContext 包含上下文信息的 {@link InvocationContext}
         * @return 更新后的构建器实例（类型为 {@code T}），用于方法链式调用
         */
        public AbstractBuilder<T> invocationContext(InvocationContext invocationContext) {
            this.invocationContext = invocationContext;
            return this;
        }

        /**
         * Sets the {@link AiServiceListenerRegistrar} instance for the builder.
         * The {@link AiServiceListenerRegistrar} facilitates the registration and
         * management of {@link AiServiceListener}s, allowing the builder to
         * configure event listeners for handling {@link AiServiceEvent}s.
         *
         * @param eventListenerRegistrar the {@link AiServiceListenerRegistrar} to use for managing event listeners
         * @return the updated builder instance of type {@code T} for method chaining
         */
        /**
         * 为构建器设置 {@link AiServiceListenerRegistrar} 实例。
         * {@link AiServiceListenerRegistrar} 用于简化 {@link AiServiceListener} 的注册与管理，
         * 使构建器能够配置事件监听器，用于处理 {@link AiServiceEvent} 事件。
         *
         * @param eventListenerRegistrar 用于管理事件监听器的 {@link AiServiceListenerRegistrar}
         * @return 更新后的构建器实例（类型为 {@code T}），用于方法链式调用
         */
        public AbstractBuilder<T> eventListenerRegistrar(AiServiceListenerRegistrar eventListenerRegistrar) {
            this.eventListenerRegistrar = eventListenerRegistrar;
            return this;
        }

        /**
         * Constructs and returns an instance of {@link ChatExecutor}.
         * Ensures that all required parameters have been appropriately set
         * before building the {@link ChatExecutor}.
         *
         * @return a fully constructed {@link ChatExecutor} instance
         */
        public abstract ChatExecutor build();
    }

    /**
     * SynchronousBuilder for constructing instances of {@link ChatExecutor}.
     *
     * This synchronousBuilder provides a fluent API for setting required components
     * like {@link ChatRequest}, and for building an instance of the {@link ChatExecutor}.
     */
    class SynchronousBuilder extends AbstractBuilder<SynchronousBuilder> {
        protected final ChatModel chatModel;

        protected SynchronousBuilder(ChatModel chatModel) {
            this.chatModel = ensureNotNull(chatModel, "chatModel");
        }

        /**
         * Constructs and returns an instance of {@link ChatExecutor}.
         * Ensures that all required parameters have been appropriately set
         * before building the {@link ChatExecutor}.
         *
         * @return a fully constructed {@link ChatExecutor} instance
         */
        public ChatExecutor build() {
            return new SynchronousChatExecutor(this);
        }
    }

    /**
     * StreamingToSynchronousBuilder for constructing instances of {@link ChatExecutor}.
     *
     * This streaming build provides a fluent API for setting required components
     * like {@link ChatRequest}, and for building an instance of the {@link ChatExecutor}
     * that simulates streaming.
     */
    /**
     * 用于构建 {@link ChatExecutor} 实例的流式转同步构建器。
     *
     * 该流式构建器提供流式 API，用于设置 {@link ChatRequest} 等必需组件，
     * 并用于构建**模拟流式响应**的 {@link ChatExecutor} 实例。
     */
    class StreamingToSynchronousBuilder extends AbstractBuilder<StreamingToSynchronousBuilder> {
        protected final StreamingChatModel streamingChatModel;
        protected Consumer<Throwable> errorHandler;

        protected StreamingToSynchronousBuilder(StreamingChatModel streamingChatModel) {
            this.streamingChatModel = ensureNotNull(streamingChatModel, "streamingChatModel");
        }

        /**
         * Sets a custom error handler to manage exceptions or errors that occur during the execution.
         *
         * @param errorHandler a {@link Consumer} of {@link Throwable} that processes the error
         * @return the current {@link StreamingToSynchronousBuilder} instance for method chaining
         */
        /**
         * 设置自定义错误处理器，用于处理执行过程中发生的异常或错误。
         *
         * @param errorHandler 处理错误的 {@link Throwable} 类型 {@link Consumer}
         * @return 当前 {@link StreamingToSynchronousBuilder} 实例，用于方法链式调用
         */
        public StreamingToSynchronousBuilder errorHandler(Consumer<Throwable> errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        /**
         * Constructs and returns an instance of {@link ChatExecutor}.
         * Ensures that all required parameters have been appropriately set
         * before building the {@link ChatExecutor}.
         *
         * @return a fully constructed {@link ChatExecutor} instance
         */
        /**
         * 构建并返回 {@link ChatExecutor} 实例。
         * 在构建之前确保所有必需参数都已正确设置。
         *
         * @return 构建完成的 {@link ChatExecutor} 完整实例
         */
        public ChatExecutor build() {
            return new StreamingToSynchronousChatExecutor(this);
        }
    }
}
