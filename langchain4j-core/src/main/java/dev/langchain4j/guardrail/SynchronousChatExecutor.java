package dev.langchain4j.guardrail;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

import dev.langchain4j.Internal;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;

/**
 * A concrete implementation of the {@link ChatExecutor} interface that executes
 * chat requests using a specified {@link ChatModel}.
 *
 * This class utilizes a {@link ChatRequest} to encapsulate the input messages
 * and parameters and delegates the execution of the chat to the provided
 * {@link ChatModel}.
 *
 * Instances of this class are immutable and are typically instantiated using
 * the {@link SynchronousBuilder}.
 */
/**
 * {@link ChatExecutor} 接口的具体实现类，用于执行聊天请求。
 * <p>
 * 该实现通过指定的 {@link ChatModel}（同步聊天模型）处理聊天请求。
 * </p>
 * <p>
 * 本类使用 {@link ChatRequest} 封装输入消息与参数，
 * 并将聊天执行逻辑委托给传入的 {@link ChatModel} 完成。
 * </p>
 * <p>
 * 该类的实例是不可变的，通常通过 {@link SynchronousBuilder} 构建实例。
 * </p>
 */
@Internal
final class SynchronousChatExecutor extends AbstractChatExecutor {
    private final ChatModel chatModel;

    protected SynchronousChatExecutor(SynchronousBuilder builder) {
        super(builder);
        this.chatModel = ensureNotNull(builder.chatModel, "chatModel");
    }

    @Override
    protected ChatResponse execute(ChatRequest chatRequest) {
        return this.chatModel.chat(chatRequest);
    }
}
