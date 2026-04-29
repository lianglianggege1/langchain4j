package dev.langchain4j.model.chat;

import dev.langchain4j.model.ModelDisabledException;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;

/**
 * A {@link ChatModel} which throws a {@link ModelDisabledException} for all of its methods
 * 一个所有方法均抛出 {@link ModelDisabledException} 异常的 {@link ChatModel}
 * <p>
 * This could be used in tests, or in libraries that extend this one to conditionally enable or disable functionality.
 * 这可用于测试场景，或在扩展本库的依赖库中按条件启用、停用相关功能。
 * </p>
 */
public class DisabledChatModel implements ChatModel {

    @Override
    public ChatResponse doChat(final ChatRequest chatRequest) {
        throw new ModelDisabledException("ChatModel is disabled");
    }
}
