package dev.langchain4j.store.embedding;

/**
 * Utility class for converting between cosine similarity and relevance score.
 */
public class RelevanceScore {
    private RelevanceScore() {}

    /**
     * Converts cosine similarity into relevance score.
     *
     * @param cosineSimilarity Cosine similarity in the range [-1..1] where -1 is not relevant and 1 is relevant.
     * @return Relevance score in the range [0..1] where 0 is not relevant and 1 is relevant.
     */
    /**
     * 将余弦相似度转换为相关性分数。
     *
     * @param cosineSimilarity 余弦相似度，取值范围 [-1..1]，-1 表示不相关，1 表示相关。
     * @return 相关性分数，取值范围 [0..1]，0 表示不相关，1 表示相关。
     */
    public static double fromCosineSimilarity(double cosineSimilarity) {
        return (cosineSimilarity + 1) / 2;
    }
}
