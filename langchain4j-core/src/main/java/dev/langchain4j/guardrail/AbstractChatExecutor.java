package dev.langchain4j.guardrail;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

import dev.langchain4j.Internal;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.observability.api.AiServiceListenerRegistrar;
import dev.langchain4j.observability.api.event.AiServiceRequestIssuedEvent;
import java.util.List;

/**
 * Abstract base class for chat executors that provides a common structure and shared functionality
 * for implementing the {@link ChatExecutor} interface.
 *
 * This class encapsulates a {@link ChatRequest} and allows subclasses to define how
 * the request should be processed by implementing the {@code execute(ChatRequest)} method.
 *
 * Subclasses are expected to be immutable and should provide specific implementations for
 * executing chat requests, typically using particular chat models or processing strategies.
 *
 * Responsibilities:
 * - Stores a {@link ChatRequest} object which can be used to build specific chat requests.
 * - Provides standard implementations for executing a chat request with a list of messages
 *   or without any additional input.
 * - Defines an abstract method {@code execute(ChatRequest)} for subclasses to implement
 *   specific execution logic.
 */
/**
 * 聊天执行器的抽象基类，为实现 {@link ChatExecutor} 接口提供通用结构与共享功能。
 *
 * 此类封装了 {@link ChatRequest}，并允许子类通过实现 {@code execute(ChatRequest)} 方法
 * 定义请求的具体处理方式。
 *
 * 子类应设计为不可变类，并提供执行聊天请求的特定实现，通常使用特定的聊天模型或处理策略。
 *
 * 职责：
 * - 存储可用于构建特定聊天请求的 {@link ChatRequest} 对象
 * - 为「携带消息列表执行聊天请求」和「无额外输入执行聊天请求」提供标准实现
 * - 定义抽象方法 {@code execute(ChatRequest)}，供子类实现具体的执行逻辑
 */
@Internal
abstract class AbstractChatExecutor implements ChatExecutor {
    protected final ChatRequest chatRequest;
    protected final InvocationContext invocationContext;
    protected final AiServiceListenerRegistrar eventListenerRegistrar;

    protected AbstractChatExecutor(AbstractBuilder<?> builder) {
        this.chatRequest = ensureNotNull(builder.chatRequest, "chatRequest");
        this.invocationContext = ensureNotNull(builder.invocationContext, "invocationContext");
        this.eventListenerRegistrar = builder.eventListenerRegistrar;
    }

    @Override
    public ChatResponse execute(List<ChatMessage> chatMessages) {
        var newChatRequest = this.chatRequest.toBuilder().messages(chatMessages).build();

        return executeInternal(newChatRequest);
    }

    @Override
    public ChatResponse execute() {
        return executeInternal(this.chatRequest);
    }

    protected void fireRequestIssuedEvent(ChatRequest chatRequest) {
        if (this.eventListenerRegistrar != null) {
            this.eventListenerRegistrar.fireEvent(AiServiceRequestIssuedEvent.builder()
                    .invocationContext(this.invocationContext)
                    .request(chatRequest)
                    .build());
        }
    }

    private ChatResponse executeInternal(ChatRequest chatRequest) {
        fireRequestIssuedEvent(chatRequest);
        return execute(chatRequest);
    }

    /**
     * Executes a given chat request and returns the corresponding chat response.
     *
     * @param chatRequest the chat request to process, containing the input messages and any necessary configurations
     * @return the chat response generated as a result of processing the given chat request
     */
    /**
     * 执行指定的聊天请求，并返回对应的聊天响应。
     *
     * @param chatRequest 待处理的聊天请求，包含输入消息与所有必要配置
     * @return 处理指定聊天请求后生成的聊天响应
     */
    protected abstract ChatResponse execute(ChatRequest chatRequest);
}
