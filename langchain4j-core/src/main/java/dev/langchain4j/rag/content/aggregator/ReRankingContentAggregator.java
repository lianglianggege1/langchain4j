package dev.langchain4j.rag.content.aggregator;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.scoring.ScoringModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.transformer.ExpandingQueryTransformer;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static dev.langchain4j.internal.Exceptions.illegalArgument;
import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;
import static dev.langchain4j.rag.content.ContentMetadata.RERANKED_SCORE;
import static java.util.Collections.emptyList;

/**
 * A {@link ContentAggregator} that performs re-ranking using a {@link ScoringModel}, such as Cohere.
 * 一个{@link ContentAggregator}，使用{@link ScoringModel}（如Cohere）执行重新排名。
 * <br>
 * The {@link ScoringModel} scores {@link Content}s against a (single) {@link Query}.
 * {@link ScoringModel}根据（单个）{@link Query}对{@link Content}进行评分。
 * If multiple {@link Query}s are input to this aggregator
 * (for example, when using {@link ExpandingQueryTransformer}),
 * a {@link #querySelector} must be provided to select a {@link Query} for ranking all {@link Content}s.
 * Alternatively, a custom implementation can be created to score {@link Content}s against the {@link Query}s
 * that were used for their retrieval (instead of a single {@link Query}), and then re-rank based on those scores.
 * Although potentially more costly, this method may yield better results
 * when the {@link Query}s are significantly different.
 * <br>
 * <br>
 * Before the use of a {@link ScoringModel}, all {@link Content}s are fused in the same way
 * as by the {@link DefaultContentAggregator}. For detailed information, please refer to its Javadoc.
 * <br>
 * <br>
 * Configurable parameters (optional):
 * <br>
 * - {@link #minScore}: the minimum score for {@link Content}s to be returned.
 * {@link Content}s scoring below this threshold (as determined by the {@link ScoringModel})
 * are excluded from the results.
 *
 * @see DefaultContentAggregator
 */
/**
 * 一种{@link ContentAggregator}内容聚合器，通过{@link ScoringModel}评分模型（例如Cohere）执行内容重排序。
 * <br>
 * 该{@link ScoringModel}评分模型会依据**单个**{@link Query}查询对{@link Content}内容进行打分。
 * 若向此聚合器输入多个{@link Query}查询
 * （例如使用{@link ExpandingQueryTransformer}扩展查询转换器时），
 * 则必须配置{@link #querySelector}查询选择器，用于选定一个{@link Query}查询对所有{@link Content}内容进行排序。
 * <br>
 * 作为替代方案，可自定义实现逻辑：基于**检索对应内容的原始查询**（而非单一查询）
 * 为{@link Content}内容打分，再依据分数完成重排序。
 * 尽管该方案计算成本更高，但在{@link Query}查询差异显著时，
 * 往往能获得更优的排序效果。
 * <br>
 * <br>
 * 在启用{@link ScoringModel}评分模型之前，所有{@link Content}内容
 * 会按照{@link DefaultContentAggregator}默认内容聚合器的规则完成内容融合。
 * 详细说明请参考其Javadoc文档。
 * <br>
 * <br>
 * 可配置参数（可选）：
 * <br>
 * - {@link #minScore}最低分数阈值：仅返回分数达到该值的{@link Content}内容。
 * 经{@link ScoringModel}评分后，低于该阈值的内容将被过滤，不纳入最终结果。
 *
 * @see DefaultContentAggregator
 */
public class ReRankingContentAggregator implements ContentAggregator {

    public static final Function<Map<Query, Collection<List<Content>>>, Query> DEFAULT_QUERY_SELECTOR =
            (queryToContents) -> {
                if (queryToContents.size() > 1) {
                    throw illegalArgument(
                            "The 'queryToContents' contains %s queries, making the re-ranking ambiguous. " +
                                    "Because there are multiple queries, it is unclear which one should be " +
                                    "used for re-ranking. Please provide a 'querySelector' in the constructor/builder.",
                            queryToContents.size()
                    );
                }
                return queryToContents.keySet().iterator().next();
            };

    private final ScoringModel scoringModel;
    private final Function<Map<Query, Collection<List<Content>>>, Query> querySelector;
    private final Double minScore;
    private final Integer maxResults;

    public ReRankingContentAggregator(ScoringModel scoringModel) {
        this(scoringModel, DEFAULT_QUERY_SELECTOR, null);
    }

    public ReRankingContentAggregator(ScoringModel scoringModel,
                                      Function<Map<Query, Collection<List<Content>>>, Query> querySelector,
                                      Double minScore) {
        this(scoringModel, querySelector, minScore, null);
    }

    public ReRankingContentAggregator(ScoringModel scoringModel,
                                      Function<Map<Query, Collection<List<Content>>>, Query> querySelector,
                                      Double minScore,
                                      Integer maxResults) {
        this.scoringModel = ensureNotNull(scoringModel, "scoringModel");
        this.querySelector = getOrDefault(querySelector, DEFAULT_QUERY_SELECTOR);
        this.minScore = minScore;
        this.maxResults = getOrDefault(maxResults, Integer.MAX_VALUE);
    }

    public static ReRankingContentAggregatorBuilder builder() {
        return new ReRankingContentAggregatorBuilder();
    }

    @Override
    public List<Content> aggregate(Map<Query, Collection<List<Content>>> queryToContents) {

        if (queryToContents.isEmpty()) {
            return emptyList();
        }

        // Select a query against which all contents will be re-ranked
        Query query = querySelector.apply(queryToContents);

        // For each query, fuse all contents retrieved from different sources using that query
        Map<Query, List<Content>> queryToFusedContents = fuse(queryToContents);

        // Fuse all contents retrieved using all queries
        List<Content> fusedContents = ReciprocalRankFuser.fuse(queryToFusedContents.values());

        if (fusedContents.isEmpty()) {
            return fusedContents;
        }

        // Re-rank all the fused contents against the query selected by the query selector
        return reRankAndFilter(fusedContents, query);
    }

    protected Map<Query, List<Content>> fuse(Map<Query, Collection<List<Content>>> queryToContents) {
        Map<Query, List<Content>> fused = new LinkedHashMap<>();
        for (Query query : queryToContents.keySet()) {
            Collection<List<Content>> contents = queryToContents.get(query);
            fused.put(query, ReciprocalRankFuser.fuse(contents));
        }
        return fused;
    }

    protected List<Content> reRankAndFilter(List<Content> contents, Query query) {

        List<TextSegment> segments = contents.stream()
                .map(Content::textSegment)
                .collect(Collectors.toList());

        // 分数
        List<Double> scores = scoringModel.scoreAll(segments, query.text()).content();

        Map<TextSegment, Double> segmentToScore = new HashMap<>();
        for (int i = 0; i < segments.size(); i++) {
            segmentToScore.put(segments.get(i), scores.get(i));
        }

        return segmentToScore.entrySet().stream()
                .filter(entry -> minScore == null || entry.getValue() >= minScore)
                .sorted(Map.Entry.<TextSegment, Double>comparingByValue().reversed())
                .map(entry ->  Content.from(entry.getKey(), Map.of(RERANKED_SCORE, entry.getValue())))
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    public static class ReRankingContentAggregatorBuilder {
        private ScoringModel scoringModel;
        private Function<Map<Query, Collection<List<Content>>>, Query> querySelector;
        private Double minScore;
        private Integer maxResults;

        ReRankingContentAggregatorBuilder() {
        }

        public ReRankingContentAggregatorBuilder scoringModel(ScoringModel scoringModel) {
            this.scoringModel = scoringModel;
            return this;
        }

        public ReRankingContentAggregatorBuilder querySelector(Function<Map<Query, Collection<List<Content>>>, Query> querySelector) {
            this.querySelector = querySelector;
            return this;
        }

        public ReRankingContentAggregatorBuilder minScore(Double minScore) {
            this.minScore = minScore;
            return this;
        }

        public ReRankingContentAggregatorBuilder maxResults(Integer maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        public ReRankingContentAggregator build() {
            return new ReRankingContentAggregator(this.scoringModel, this.querySelector, this.minScore, this.maxResults);
        }
    }
}
