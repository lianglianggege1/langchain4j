package dev.langchain4j.store.embedding;

import java.util.List;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

/**
 * Represents a result of a search in an {@link EmbeddingStore}.
 */
/**
 * 表示在 {@link EmbeddingStore}（嵌入存储）中执行搜索的结果。
 */
public class EmbeddingSearchResult<Embedded> {

    private final List<EmbeddingMatch<Embedded>> matches;

    public EmbeddingSearchResult(List<EmbeddingMatch<Embedded>> matches) {
        this.matches = ensureNotNull(matches, "matches");
    }

    public List<EmbeddingMatch<Embedded>> matches() {
        return matches;
    }
}
