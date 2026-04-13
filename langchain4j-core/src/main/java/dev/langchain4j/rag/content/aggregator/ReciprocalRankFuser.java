package dev.langchain4j.rag.content.aggregator;

import dev.langchain4j.rag.content.Content;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dev.langchain4j.internal.ValidationUtils.ensureBetween;

/**
 * Implementation of Reciprocal Rank Fusion.
 * 互惠秩融合的实现。
 * <br>
 * A comprehensive explanation can be found
 * <a href="https://learn.microsoft.com/en-us/azure/search/hybrid-search-ranking">here</a>.
 */
public class ReciprocalRankFuser {

    /**
     * Fuses multiple {@code List<Content>} into a single {@code List<Content>}
     * using the Reciprocal Rank Fusion (RRF) algorithm with k=60.
     * 使用k=60的互易秩融合（RRF）算法将多个List<Content>融合为一个List<Content->。
     *
     * @param listsOfContents A {@link Collection} of {@code List<Content>} to be fused together.
     * @return A single {@code List<Content>}, the result of the fusion.
     */
    public static List<Content> fuse(Collection<List<Content>> listsOfContents) {
        return fuse(listsOfContents, 60);
    }

    /**
     * Fuses multiple {@code List<Content>} into a single {@code List<Content>}
     * using the Reciprocal Rank Fusion (RRF) algorithm.
     * 使用互易秩融合（RRF）算法将多个List<Content>融合为单个List<Content->。
     *
     * @param listsOfContents A {@link Collection} of {@code List<Content>} to be fused together.
     *                        要融合在一起的{@code List<Content>}的{@link Collection}。
     * @param k               A ranking constant used to control the influence of individual ranks
     *                        from different ranked lists being combined. A common value used is 60,
     *                        based on empirical studies. However, the optimal value may vary depending
     *                        on the specific application and the characteristics of the data.
     *                        A larger value diminishes the differences between the ranks,
     *                        leading to a more uniform distribution of fusion scores.
     *                        A smaller value amplifies the importance of the top-ranked items in each list.
     *                        K must be greater than or equal to 1.
     *                        一种排名常数，用于控制来自不同排名列表的单个排名组合的影响。
     *                        根据实证研究，常用的值是60。
     *                        然而，最佳值可能因具体应用和数据特性而异。
     *                        较大的值会减少排名之间的差异，从而使融合分数的分布更加均匀。
     *                        较小的值会放大每个列表中排名靠前的项目的重要性。K必须大于或等于1。
     * @return A single {@code List<Content>}, the result of the fusion.
     */
    public static List<Content> fuse(Collection<List<Content>> listsOfContents, int k) {
        ensureBetween(k, 1, Integer.MAX_VALUE, "k");

        Map<Content, Double> scores = new LinkedHashMap<>();
        for (List<Content> singleListOfContent : listsOfContents) {
            for (int i = 0; i < singleListOfContent.size(); i++) {
                Content content = singleListOfContent.get(i);
                double currentScore = scores.getOrDefault(content, 0.0);
                int rank = i + 1;
                double newScore = currentScore + 1.0 / (k + rank);
                scores.put(content, newScore);
            }
        }

        List<Content> fused = new ArrayList<>(scores.keySet());
        fused.sort(Comparator.comparingDouble(scores::get).reversed());
        return fused;
    }
}
