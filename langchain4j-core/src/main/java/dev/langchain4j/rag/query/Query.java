package dev.langchain4j.rag.query;

import dev.langchain4j.rag.content.Content;

import java.util.Objects;

import static dev.langchain4j.internal.Utils.quoted;
import static dev.langchain4j.internal.ValidationUtils.ensureNotBlank;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

/**
 * Represents a query from the user intended for retrieving relevant {@link Content}s.
 * 表示来自用户的用于检索相关｛@link Content｝s的查询。
 * <br>
 * Currently, it is limited to text,
 * but future extensions may include support for other modalities (e.g., images, audio, video, etc.).
 * 目前，它仅限于文本，但未来的扩展可能包括对其他模态的支持（例如，图像、音频、视频等）。
 * <br>
 * Includes {@link Metadata} that may be useful or necessary for retrieval or augmentation.
 * 包括可能对检索或增强有用或必要的{@link Metadata}。
 */
public class Query {

    // query text
    private final String text;
    private final Metadata metadata;

    public Query(String text) {
        this.text = ensureNotBlank(text, "text");
        this.metadata = null;
    }

    public Query(String text, Metadata metadata) {
        this.text = ensureNotBlank(text, "text");
        this.metadata = ensureNotNull(metadata, "metadata");
    }

    public String text() {
        return text;
    }

    public Metadata metadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Query that = (Query) o;
        return Objects.equals(this.text, that.text)
                && Objects.equals(this.metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, metadata);
    }

    @Override
    public String toString() {
        return "Query {" +
                " text = " + quoted(text) +
                ", metadata = " + metadata +
                " }";
    }

    public static Query from(String text) {
        return new Query(text);
    }

    public static Query from(String text, Metadata metadata) {
        return new Query(text, metadata);
    }
}
