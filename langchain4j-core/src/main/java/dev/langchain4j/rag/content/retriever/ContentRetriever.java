package dev.langchain4j.rag.content.retriever;

import static dev.langchain4j.internal.Utils.isNullOrEmpty;

import dev.langchain4j.Experimental;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.listener.ContentRetrieverListener;
import dev.langchain4j.rag.query.Query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Retrieves {@link Content}s from an underlying data source using a given {@link Query}.
 * 使用给定的｛@link查询｝从基础数据源检索｛@link内容｝。
 * <br>
 * The goal is to retrieve only relevant {@link Content}s in relation to a given {@link Query}.
 * 目标是只检索与给定的{@link Query}相关的{@link Content}。
 * <br>
 * The underlying data source can be virtually anything:
 * 底层数据源几乎可以是任何东西：
 * <pre>
 * - Embedding (vector) store (see {@link EmbeddingStoreContentRetriever})
 * - 嵌入（矢量）存储（请参阅{@link EmbeddingStoreContentRetriever}）
 * - Full-text search engine (see {@code AzureAiSearchContentRetriever} in the {@code langchain4j-azure-ai-search} module)
 * - 全文搜索引擎（请参阅｛@code langchain4j azure ai search｝模块中的｛@code AzureAiSearchContentRetriever｝）
 * - Hybrid of vector and full-text search (see {@code AzureAiSearchContentRetriever} in the {@code langchain4j-azure-ai-search} module)
 * - 混合向量和全文搜索（请参阅｛@code langchain4j azure ai search｝模块中的｛@code AzureAiSearchContentRetriever｝）
 * - Web Search Engine (see {@link WebSearchContentRetriever})
 * - 网页搜索引擎（请参阅{@link WebSearchContentRetriever}）
 * - Knowledge graph (see {@code Neo4jContentRetriever} in the {@code langchain4j-community-neo4j-retriever} module)
 * - 知识图谱（请参阅｛@code langchain4j-community-neo4j-retriever｝模块中的｛@code Neo4jContentRetriever｝）
 * - SQL database (see {@code SqlDatabaseContentRetriever} in the {@code langchain4j-experimental-sql} module)
 * - SQL数据库（请参阅｛@code langchain4j-experimental-sql｝模块中的｛@code SqlDatabaseContentRetriever｝）
 * - etc.
 * </pre>
 *
 * @see EmbeddingStoreContentRetriever
 * @see WebSearchContentRetriever
 */
public interface ContentRetriever {

    /**
     * Retrieves relevant {@link Content}s using a given {@link Query}.
     * The {@link Content}s are sorted by relevance, with the most relevant {@link Content}s appearing
     * at the beginning of the returned {@code List<Content>}.
     * 使用给定的｛@link Query｝检索相关｛@link Content｝。
     * {@link Content}按相关性排序，最相关的{@link Content}出现在返回的{@code List<Content>}的开头。
     *
     * @param query The {@link Query} to use for retrieval.
     * @return A list of retrieved {@link Content}s.
     */
    List<Content> retrieve(Query query);

    /**
     * Wraps this {@link ContentRetriever} with a listening retriever that dispatches events to the provided listener.
     * 使用侦听检索器将此{@link ContentRetriever}包裹起来，该侦听检索器会将事件分派给提供的侦听器。
     *
     * @param listener The listener to add.
     * @return An observing {@link ContentRetriever} that will dispatch events to the provided listener.
     * @since 1.11.0
     */
    @Experimental
    default ContentRetriever addListener(ContentRetrieverListener listener) {
        return addListeners(listener == null ? null : List.of(listener));
    }

    /**
     * Wraps this {@link ContentRetriever} with a listening retriever that dispatches events to the provided listeners.
     * 使用侦听检索器将此{@link ContentRetriever}包裹起来，该侦听检索器会将事件分派给提供的侦听器。
     * <p>
     * Listeners are called in the order of iteration.
     * 监听器按迭代顺序调用。
     *
     * @param listeners The listeners to add.
     * @return An observing {@link ContentRetriever} that will dispatch events to the provided listeners.
     * @since 1.11.0
     */
    @Experimental
    default ContentRetriever addListeners(Collection<ContentRetrieverListener> listeners) {
        if (isNullOrEmpty(listeners)) {
            return this;
        }
        if (this instanceof ListeningContentRetriever listeningContentRetriever) {
            return listeningContentRetriever.withAdditionalListeners(listeners);
        }
        if (listeners instanceof List<ContentRetrieverListener> listenersList) {
            return new ListeningContentRetriever(this, listenersList);
        } else {
            return new ListeningContentRetriever(this, new ArrayList<>(listeners));
        }
    }
}
