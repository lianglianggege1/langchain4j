package dev.langchain4j.store.embedding.listener;

import dev.langchain4j.Experimental;
import dev.langchain4j.store.embedding.EmbeddingStore;

/**
 * A {@link EmbeddingStore} listener that listens for requests, responses and errors.
 *
 * @since 1.11.0
 */
/**
 * 向量存储 {@link EmbeddingStore} 的监听器，用于监听请求、响应和错误。
 *
 * @since 1.11.0
 */
@Experimental
public interface EmbeddingStoreListener {

    /**
     * This method is called before the request is executed against the embedding store.
     *
     * @param requestContext The request context. It contains operation details and attributes.
     *                       The attributes can be used to pass data between methods of this listener
     *                       or between multiple listeners.
     */
    /**
     * 在向向量存储执行请求之前调用此方法。
     *
     * @param requestContext 请求上下文，包含操作详情和属性。
     *                       这些属性可用于在该监听器的多个方法之间
     *                       或多个监听器之间传递数据。
     */
    default void onRequest(EmbeddingStoreRequestContext<?> requestContext) {}

    /**
     * This method is called after a successful operation completes.
     *
     * @param responseContext The response context. It contains operation details and attributes.
     *                        The attributes can be used to pass data between methods of this listener
     *                        or between multiple listeners.
     */
    /**
     * 操作成功完成后调用此方法。
     *
     * @param responseContext 响应上下文，包含操作详情和属性。
     *                        这些属性可用于在该监听器的多个方法之间
     *                        或多个监听器之间传递数据。
     */
    default void onResponse(EmbeddingStoreResponseContext<?> responseContext) {}

    /**
     * This method is called when an error occurs during interaction with the embedding store.
     *
     * @param errorContext The error context. It contains the error, operation details and attributes.
     *                     The attributes can be used to pass data between methods of this listener
     *                     or between multiple listeners.
     */
    /**
     * 与向量存储交互过程中发生错误时调用此方法。
     *
     * @param errorContext 错误上下文，包含错误信息、操作详情和属性。
     *                     这些属性可用于在该监听器的多个方法之间
     *                     或多个监听器之间传递数据。
     */
    default void onError(EmbeddingStoreErrorContext<?> errorContext) {}
}
