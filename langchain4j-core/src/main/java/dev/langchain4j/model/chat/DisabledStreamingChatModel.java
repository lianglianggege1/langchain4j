package dev.langchain4j.model.chat;

import dev.langchain4j.model.ModelDisabledException;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;

/**
 * A {@link StreamingChatModel} which throws a {@link ModelDisabledException} for all of its methods
 * 一个所有方法均抛出 {@link ModelDisabledException} 异常的 {@link StreamingChatModel}
 * <p>
 * This could be used in tests, or in libraries that extend this one to conditionally enable or disable functionality.
 * 这可用于测试，或在扩展该库的各类库中按条件启用或禁用功能。
 * </p>
 */
public class DisabledStreamingChatModel implements StreamingChatModel {

    @Override
    public void doChat(ChatRequest chatRequest, StreamingChatResponseHandler handler) {
        throw new ModelDisabledException("StreamingChatModel is disabled");
    }
}
