package dev.langchain4j.rag.content.retriever.listener;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

import dev.langchain4j.Experimental;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import java.util.Map;

/**
 * The content retriever error context.
 * 内容检索器错误上下文。
 * It contains the error, corresponding {@link Query}, {@link ContentRetriever} and attributes.
 * 它包含错误、相应的｛@link Query｝、｛@link ContentRetriever｝和属性。
 * The attributes can be used to pass data between methods of a {@link ContentRetrieverListener}
 * or between multiple {@link ContentRetrieverListener}s.
 * 这些属性可用于在{@link ContentRetrieverListener}的方法之间或在多个{@link ContentRetrieverListener}之间传递数据。
 *
 * @since 1.11.0
 */
@Experimental
public class ContentRetrieverErrorContext {

    private final Throwable error;
    private final Query query;
    private final ContentRetriever contentRetriever;
    private final Map<Object, Object> attributes;

    public ContentRetrieverErrorContext(Builder builder) {
        this.error = ensureNotNull(builder.error, "error");
        this.query = ensureNotNull(builder.query, "query");
        this.contentRetriever = ensureNotNull(builder.contentRetriever, "contentRetriever");
        this.attributes = ensureNotNull(builder.attributes, "attributes");
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link ContentRetrieverErrorContext}.
     *
     * @since 1.11.0
     */
    @Experimental
    public static class Builder {

        private Throwable error;
        private Query query;
        private ContentRetriever contentRetriever;
        private Map<Object, Object> attributes;

        Builder() {}

        public Builder error(Throwable error) {
            this.error = error;
            return this;
        }

        public Builder query(Query query) {
            this.query = query;
            return this;
        }

        public Builder contentRetriever(ContentRetriever contentRetriever) {
            this.contentRetriever = contentRetriever;
            return this;
        }

        public Builder attributes(Map<Object, Object> attributes) {
            this.attributes = attributes;
            return this;
        }

        public ContentRetrieverErrorContext build() {
            return new ContentRetrieverErrorContext(this);
        }
    }

    /**
     * @return The error that occurred.
     */
    public Throwable error() {
        return error;
    }

    public Query query() {
        return query;
    }

    public ContentRetriever contentRetriever() {
        return contentRetriever;
    }

    /**
     * @return The attributes map. It can be used to pass data between methods of a {@link ContentRetrieverListener}
     * or between multiple {@link ContentRetrieverListener}s.
     */
    public Map<Object, Object> attributes() {
        return attributes;
    }
}
