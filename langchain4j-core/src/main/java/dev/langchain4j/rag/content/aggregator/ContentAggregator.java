package dev.langchain4j.rag.content.aggregator;

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Aggregates all {@link Content}s retrieved from all {@link ContentRetriever}s using all {@link Query}s.
 * 使用所有{@link Query}聚合从所有{@link ContentRetriever}检索到的所有{@link Content}。
 * <br>
 * The goal is to ensure that only the most relevant and non-redundant {@link Content}s are presented to the LLM.
 * 目标是确保只向LLM呈现最相关和非冗余的{@link Content}。
 * <br>
 * Some effective approaches include:
 * 一些有效的方法包括：
 * <pre>
 * - Re-ranking (see {@link ReRankingContentAggregator})
 * - 重新排名（请参阅{@link ReRankingContentAggregator}）
 * - Reciprocal Rank Fusion (see {@link ReciprocalRankFuser}, utilized in both {@link DefaultContentAggregator} and {@link ReRankingContentAggregator})
 * - 互惠排名融合（见{@link ReciprocalRankFuser}，在{@link DefaultContentAggregator}和{@link ReRankingContentAggregator}中都有使用）
 * </pre>
 *
 * @see DefaultContentAggregator
 * @see ReRankingContentAggregator
 */
public interface ContentAggregator {

    /**
     * Aggregates all {@link Content}s retrieved by all {@link ContentRetriever}s using all {@link Query}s.
     * The {@link Content}s, both on input and output, are sorted by relevance,
     * with the most relevant {@link Content}s appearing at the beginning of {@code List<Content>}.
     * 聚合所有{@link ContentRetriever｝使用所有{@link Query｝s检索到的所有{@link Content｝。
     * 输入和输出的{@link Content｝都按相关性排序，最相关的｛@linkContent｝出现在 {@code List<Content>}的开头。
     *
     * @param queryToContents A map from a {@link Query} to all {@code List<Content>} retrieved with that {@link Query}.
     *                        Given that each {@link Query} can be routed to multiple {@link ContentRetriever}s, the
     *                        value of this map is a {@code Collection<List<Content>>}
     *                        rather than a simple {@code List<Content>}.
     *                        从查询到使用该查询检索到的所有列表<Content>的映射。
     *                        鉴于每个查询都可以路由到多个ContentRetriever，
     *                        此映射的值是一个集合<List<Content>>，
     *                        而不是一个简单的列表<Content>。
     * @return A list of aggregated {@link Content}s. 聚合的｛@link内容｝列表。
     */
    List<Content> aggregate(Map<Query, Collection<List<Content>>> queryToContents);
}
