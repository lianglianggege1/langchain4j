package dev.langchain4j.store.embedding;

import dev.langchain4j.data.embedding.Embedding;

import static dev.langchain4j.internal.Exceptions.illegalArgument;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

/**
 * Utility class for calculating cosine similarity between two vectors.
 */
/**
 * 用于计算两个向量之间余弦相似度的工具类。
 */
public class CosineSimilarity {
    private CosineSimilarity() {}

    /**
     * A small value to avoid division by zero.
     */
    /**
     * 用于避免除零错误的极小值。
     */
    public static final float EPSILON = 1e-8f;

    /**
     * Calculates cosine similarity between two vectors.
     * <p>
     * Cosine similarity measures the cosine of the angle between two vectors, indicating their directional similarity.
     * It produces a value in the range:
     * <p>
     * -1 indicates vectors are diametrically opposed (opposite directions).
     * <p>
     * 0 indicates vectors are orthogonal (no directional similarity).
     * <p>
     * 1 indicates vectors are pointing in the same direction (but not necessarily of the same magnitude).
     * <p>
     * Not to be confused with cosine distance ([0..2]), which quantifies how different two vectors are.
     * <p>
     * Embeddings of all-zeros vectors are considered orthogonal to all other vectors;
     * including other all-zeros vectors.
     *
     * @param embeddingA first embedding vector
     * @param embeddingB second embedding vector
     * @return cosine similarity in the range [-1..1]
     */
    /**
     * 计算两个向量之间的余弦相似度。
     * <p>
     * 余弦相似度用于衡量两个向量之间夹角的余弦值，表示它们的方向相似性。
     * 计算结果取值范围：
     * <p>
     * -1 表示向量方向完全相反。
     * <p>
     * 0 表示向量正交（无方向相似性）。
     * <p>
     * 1 表示向量方向完全一致（但长度不一定相同）。
     * <p>
     * 不要与余弦距离（[0..2]）混淆，余弦距离用于量化两个向量的差异程度。
     * <p>
     * 全零向量的嵌入被视为与所有其他向量正交；
     * 包括其他全零向量。
     *
     * @param embeddingA 第一个嵌入向量
     * @param embeddingB 第二个嵌入向量
     * @return 余弦相似度，取值范围 [-1..1]
     */
    public static double between(Embedding embeddingA, Embedding embeddingB) {
        ensureNotNull(embeddingA, "embeddingA");
        ensureNotNull(embeddingB, "embeddingB");

        float[] vectorA = embeddingA.vector();
        float[] vectorB = embeddingB.vector();

        if (vectorA.length != vectorB.length) {
            throw illegalArgument("Length of vector a (%s) must be equal to the length of vector b (%s)",
                    vectorA.length, vectorB.length);
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }

        // Avoid division by zero.
        return dotProduct / Math.max(Math.sqrt(normA) * Math.sqrt(normB), EPSILON);
    }

    /**
     * Converts relevance score into cosine similarity.
     *
     * @param relevanceScore Relevance score in the range [0..1] where 0 is not relevant and 1 is relevant.
     * @return Cosine similarity in the range [-1..1] where -1 is not relevant and 1 is relevant.
     */
    /**
     * 将相关性分数转换为余弦相似度。
     *
     * @param relevanceScore 相关性分数，取值范围 [0..1]，0 表示不相关，1 表示相关。
     * @return 余弦相似度，取值范围 [-1..1]，-1 表示不相关，1 表示相关。
     */
    public static double fromRelevanceScore(double relevanceScore) {
        return relevanceScore * 2 - 1;
    }
}
