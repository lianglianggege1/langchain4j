package dev.langchain4j.store.embedding.listener;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

import dev.langchain4j.Experimental;
import dev.langchain4j.store.embedding.EmbeddingStore;
import java.util.Map;

/**
 * The embedding store error context.
 * It contains the error, corresponding request details, and attributes.
 * The attributes can be used to pass data between methods of an {@link EmbeddingStoreListener}
 * or between multiple {@link EmbeddingStoreListener}s.
 *
 * @since 1.11.0
 */
/**
 * 向量存储的错误上下文。
 * 包含错误信息、对应的请求详情和属性。
 * 这些属性可用于在 {@link EmbeddingStoreListener} 的多个方法之间
 * 或多个 {@link EmbeddingStoreListener} 之间传递数据。
 *
 * @since 1.11.0
 */
@Experimental
public class EmbeddingStoreErrorContext<Embedded> {

    private final Throwable error;
    private final EmbeddingStoreRequestContext<Embedded> requestContext;
    private final Map<Object, Object> attributes;

    public EmbeddingStoreErrorContext(
            Throwable error, EmbeddingStoreRequestContext<Embedded> requestContext, Map<Object, Object> attributes) {
        this.error = ensureNotNull(error, "error");
        this.requestContext = ensureNotNull(requestContext, "requestContext");
        this.attributes = ensureNotNull(attributes, "attributes");
    }

    /**
     * @return The error that occurred.
     */
    public Throwable error() {
        return error;
    }

    public EmbeddingStore<Embedded> embeddingStore() {
        return requestContext.embeddingStore();
    }

    /**
     * @return The corresponding request context.
     */
    public EmbeddingStoreRequestContext<Embedded> requestContext() {
        return requestContext;
    }

    /**
     * @return The attributes map. It can be used to pass data between methods of an {@link EmbeddingStoreListener}
     * or between multiple {@link EmbeddingStoreListener}s.
     */
    public Map<Object, Object> attributes() {
        return attributes;
    }
}
