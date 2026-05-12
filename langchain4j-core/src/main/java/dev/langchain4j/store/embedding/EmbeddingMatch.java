package dev.langchain4j.store.embedding;

import dev.langchain4j.data.embedding.Embedding;

import java.util.Objects;

import static dev.langchain4j.internal.ValidationUtils.ensureNotBlank;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

/**
 * Represents a matched embedding along with its relevance score (derivative of cosine distance), ID, and original embedded content.
 *
 * @param <Embedded> The class of the object that has been embedded. Typically, it is {@link dev.langchain4j.data.segment.TextSegment}.
 */
/**
 * 表示匹配到的嵌入向量，及其相关性分数（由余弦距离推导而来）、ID 和原始嵌入内容。
 *
 * @param <Embedded> 已嵌入的对象类型。通常为 {@link dev.langchain4j.data.segment.TextSegment}。
 */
public class EmbeddingMatch<Embedded> {

    private final Double score;
    private final String embeddingId;
    private final Embedding embedding;
    private final Embedded embedded;

    /**
     * Creates a new instance.
     * @param score The relevance score (derivative of cosine distance) of this embedding compared to
     *              a reference embedding during a search.
     * @param embeddingId The ID of the embedding assigned when adding this embedding to the store.
     * @param embedding The embedding that has been matched.
     * @param embedded The original content that was embedded. Typically, this is a {@link dev.langchain4j.data.segment.TextSegment}.
     */
    /**
     * 创建新实例。
     * @param score 搜索过程中，该嵌入向量与参考嵌入向量相比的相关性分数（由余弦距离推导而来）。
     * @param embeddingId 将该嵌入向量添加到存储时分配的 ID。
     * @param embedding 匹配到的嵌入向量。
     * @param embedded 被嵌入的原始内容。通常为 {@link dev.langchain4j.data.segment.TextSegment}。
     */
    public EmbeddingMatch(Double score, String embeddingId, Embedding embedding, Embedded embedded) {
        this.score = ensureNotNull(score, "score");
        this.embeddingId = ensureNotBlank(embeddingId, "embeddingId");
        this.embedding = embedding;
        this.embedded = embedded;
    }

    /**
     * Returns the relevance score (derivative of cosine distance) of this embedding compared to
     * a reference embedding during a search.
     * The current implementation assumes that the embedding store uses cosine distance when comparing embeddings.
     *
     * @return Relevance score, ranging from 0 (not relevant) to 1 (highly relevant).
     */
    public Double score() {
        return score;
    }

    /**
     * The ID of the embedding assigned when adding this embedding to the store.
     * @return The ID of the embedding assigned when adding this embedding to the store.
     */
    public String embeddingId() {
        return embeddingId;
    }

    /**
     * Returns the embedding that has been matched.
     * @return The embedding that has been matched.
     */
    public Embedding embedding() {
        return embedding;
    }

    /**
     * Returns the original content that was embedded.
     * @return The original content that was embedded. Typically, this is a {@link dev.langchain4j.data.segment.TextSegment}.
     */
    public Embedded embedded() {
        return embedded;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmbeddingMatch<?> that = (EmbeddingMatch<?>) o;
        return Objects.equals(this.score, that.score)
                && Objects.equals(this.embeddingId, that.embeddingId)
                && Objects.equals(this.embedding, that.embedding)
                && Objects.equals(this.embedded, that.embedded);
    }

    @Override
    public int hashCode() {
        return Objects.hash(score, embeddingId, embedding, embedded);
    }

    @Override
    public String toString() {
        return "EmbeddingMatch {" +
                " score = " + score +
                ", embedded = " + embedded +
                ", embeddingId = " + embeddingId +
                ", embedding = " + embedding +
                " }";
    }
}
