package dev.langchain4j.model.chat.response;

import dev.langchain4j.Experimental;

/**
 * Handle that can be used to cancel the streaming done via {@link StreamingChatResponseHandler}.
 *
 * @since 1.8.0
 */
@Experimental
public interface StreamingHandle {

    /**
     * Cancels the streaming.
     * 退出流
     */
    void cancel();

    /**
     * Returns {@code true} if streaming was cancelled by calling {@link #cancel()}.
     * 若通过调用{@link #cancel()}取消流式传输，则返回{@code true}。
     */
    boolean isCancelled();
}
