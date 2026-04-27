package dev.langchain4j.rag.content.aggregator;

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.query.Query;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link ContentAggregator} intended to be suitable for the majority of use cases.
 * {@link ContentAggregator}的默认实现旨在适用于大多数用例。
 * <br>
 * <br>
 * It's important to note that while efforts will be made to avoid breaking changes,
 * the default behavior of this class may be updated in the future if it's found
 * that the current behavior does not adequately serve the majority of use cases.
 * Such changes would be made to benefit both current and future users.
 * 值得注意的是，虽然将努力避免破坏性更改，但如果发现当前行为不能充分服务于大多数用例，
 * 则该类的默认行为可能会在未来进行更新。这些变化将使当前和未来的用户受益。
 * <br>
 * <br>
 * This implementation employs Reciprocal Rank Fusion (see {@link ReciprocalRankFuser}) in two stages
 * to aggregate all {@code Collection<List<Content>>} into a single {@code List<Content>}.
 * The {@link Content}s in both the input and output lists are expected to be sorted by relevance,
 * with the most relevant {@link Content}s at the beginning of the {@code List<Content>}.
 * 此实现分两个阶段采用互易秩融合（参见{@link ReciprocalRankFuser}）
 * 将所有｛@code Collection＜List＜Content＞｝聚合为一个｛@code List＜Content｝。
 * 输入和输出列表中的{@link Content}都应按相关性排序，
 * 最相关的{@link Content}位于{@code List<Content>}的开头。
 * <br>
 * Stage 1: For each {@link Query}, all {@code List<Content>} retrieved with that {@link Query}
 * are merged into a single {@code List<Content>}.
 * 第一阶段：对于每个{@link Query}，使用该{@link Query}检索到的所有{@code List<Content>}都被合并到一个{@code List<Content>｝中。
 * <br>
 * Stage 2: All {@code List<Content>} (results from stage 1) are merged into a single {@code List<Content>}.
 * <br>
 * <br>
 * <b>Example:</b>
 * <br>
 * Input (query -&gt; multiple lists with ranked contents):
 * <pre>
 * home animals -&gt; [cat, dog, hamster], [cat, parrot]
 * domestic animals -&gt; [dog, horse], [cat]
 * </pre>
 * After stage 1 (query -&gt; single list with ranked contents):
 * <br>
 * <pre>
 * home animals -&gt; [cat, dog, parrot, hamster]
 * domestic animals -&gt; [dog, cat, horse]
 * </pre>
 * After stage 2 (single list with ranked contents):
 * <br>
 * <pre>
 * [cat, dog, parrot, horse, hamster]
 * </pre>
 *
 * @see ReciprocalRankFuser
 * @see ReRankingContentAggregator
 */
public class DefaultContentAggregator implements ContentAggregator {

    @Override
    public List<Content> aggregate(Map<Query, Collection<List<Content>>> queryToContents) {

        // First, for each query, fuse all contents retrieved from different sources using that query.
        // 首先，对于每个查询，融合使用该查询从不同来源检索到的所有内容。
        Map<Query, List<Content>> fused = fuse(queryToContents);

        // Then, fuse all contents retrieved using all queries
        // 然后，融合使用所有查询检索到的所有内容
        return ReciprocalRankFuser.fuse(fused.values());
    }

    protected Map<Query, List<Content>> fuse(Map<Query, Collection<List<Content>>> queryToContents) {
        Map<Query, List<Content>> fused = new LinkedHashMap<>();
        for (Query query : queryToContents.keySet()) {
            Collection<List<Content>> contents = queryToContents.get(query);
            fused.put(query, ReciprocalRankFuser.fuse(contents));
        }
        return fused;
    }
}
