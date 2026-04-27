package dev.langchain4j.rag.content.retriever.listener;

import dev.langchain4j.Experimental;
import dev.langchain4j.rag.content.retriever.ContentRetriever;

/**
 * A {@link ContentRetriever} listener that listens for requests, responses and errors.
 * 一个{@link ContentRetriever}侦听器，用于侦听请求、响应和错误。
 *
 * @since 1.11.0
 */
@Experimental
public interface ContentRetrieverListener {

    /**
     * This method is called before the request is executed against the retriever.
     * 在对检索器执行请求之前调用此方法。
     *
     * @param requestContext The request context. It contains the {@link dev.langchain4j.rag.query.Query} and attributes.
     *                       The attributes can be used to pass data between methods of this listener
     *                       or between multiple listeners.
     */
    default void onRequest(ContentRetrieverRequestContext requestContext) {}

    /**
     * This method is called after a successful retrieval.
     * 此方法在成功检索后调用。
     *
     * @param responseContext The response context. It contains retrieved content, corresponding query and attributes.
     *                        The attributes can be used to pass data between methods of this listener
     *                        or between multiple listeners.
     */
    default void onResponse(ContentRetrieverResponseContext responseContext) {}

    /**
     * This method is called when an error occurs during retrieval.
     * 当检索过程中发生错误时，会调用此方法。
     *
     * @param errorContext The error context. It contains the error, corresponding query and attributes.
     *                     The attributes can be used to pass data between methods of this listener
     *                     or between multiple listeners.
     */
    default void onError(ContentRetrieverErrorContext errorContext) {}
}
