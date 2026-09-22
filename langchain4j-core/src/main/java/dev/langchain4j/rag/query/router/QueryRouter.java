package dev.langchain4j.rag.query.router;

import dev.langchain4j.Experimental;
import dev.langchain4j.exception.AsyncNotSupportedException;
import dev.langchain4j.internal.AsyncNotSupported;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Routes the given {@link Query} to one or multiple {@link ContentRetriever}s.
 * 将给定的｛@link Query｝路由到一个或多个｛@link ContentRetriever｝。
 * <br>
 * The goal is to ensure that {@link Content} is retrieved only from relevant data sources.
 * 目标是确保仅从相关数据源检索｛@link Content｝。
 * <br>
 * Some potential approaches include:
 * 一些潜在的方法包括：
 * <pre>
 * - Using an LLM (see {@link LanguageModelQueryRouter})
 * - 使用LLM（请参阅{@link LanguageModelQueryRouter}）
 * - Using an {@link EmbeddingModel} (aka "semantic routing", see {@code EmbeddingModelTextClassifier} in the {@code langchain4j} module)
 * - 使用｛@link EmbeddedModel｝（也称为“语义路由”，请参阅｛@code langchain4j｝模块中的｛@code EmbeddedModelTextClassifier｝）
 * - Using keyword-based routing
 * - 使用基于关键字的路由
 * - Route depending on the user ({@code query.metadata().chatMemoryId()}) and/or permissions
 * - 路由取决于用户（{@code query.metadata（）.chatMemoryId（）}）和/或权限
 * </pre>
 *
 * @see DefaultQueryRouter
 * @see LanguageModelQueryRouter
 */
public interface QueryRouter {

    /**
     * Routes the given {@link Query} to one or multiple {@link ContentRetriever}s.
     * 将给定的｛@link Query｝路由到一个或多个｛@link ContentRetriever｝。
     *
     * @param query The {@link Query} to be routed.
     * @return A collection of one or more {@link ContentRetriever}s to which the {@link Query} should be routed.
     */
    Collection<ContentRetriever> route(Query query);

    /**
     * Non-blocking counterpart of {@link #route(Query)}, invoked by the asynchronous
     * ({@link java.util.concurrent.CompletableFuture}/{@link java.util.concurrent.CompletionStage}) and reactive
     * ({@link java.util.concurrent.Flow.Publisher}) AI Service modes when RAG is configured.
     * <p>
     * The default implementation returns a failed future carrying {@link AsyncNotSupportedException}: a router backed by a blocking LLM
     * call (e.g. {@link LanguageModelQueryRouter}) must opt in by overriding this method to stay off the calling
     * thread. A router that cannot be made non-blocking is still usable from these modes via
     * {@code DefaultRetrievalAugmentor}, which offloads the blocking {@link #route(Query)} to its executor.
     *
     * @param query The {@link Query} to be routed.
     * @return A {@link CompletableFuture} of one or more {@link ContentRetriever}s to route the {@link Query} to.
     * @since 1.20.0
     */
    @Experimental
    default CompletableFuture<Collection<ContentRetriever>> routeAsync(Query query) {
        return AsyncNotSupported.failedFuture(getClass(), "routeAsync");
    }
}
