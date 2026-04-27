package dev.langchain4j.rag.query.transformer;

import dev.langchain4j.rag.query.Query;

import java.util.Collection;

/**
 * Transforms the given {@link Query} into one or multiple {@link Query}s.
 * 将给定的｛@link Query｝转换为一个或多个｛@link Query｝。
 * <br>
 * The goal is to enhance retrieval quality by modifying or expanding the original {@link Query}.
 * 这个目标是提升检索质量，通过修改或扩展原始｛@link Query｝。
 * <br>
 * Some known approaches to improve retrieval include:
 * 一些已知的改进检索的方法包括：
 * <pre>
 * - Query compression (see {@link CompressingQueryTransformer})
 * - 查询压缩（请参阅{@link CompressingQueryTransformer}）
 * - Query expansion (see {@link ExpandingQueryTransformer})
 * - 查询扩展（请参阅{@link ExpandingQueryTransformer}）
 * - Query re-writing
 * - 查询重写
 * - Step-back prompting
 * - 步骤后提示
 * - Hypothetical document embeddings (HyDE)
 * - 假设性文件嵌入（HyDE）
 * </pre>
 * Additional details can be found <a href="https://blog.langchain.dev/query-transformations/">here</a>.
 * 更多详细信息可以在<a href="https://blog.langchain.dev/query-transformations/">查看
 *
 * @see DefaultQueryTransformer
 * @see CompressingQueryTransformer
 * @see ExpandingQueryTransformer
 */
public interface QueryTransformer {

    /**
     * Transforms the given {@link Query} into one or multiple {@link Query}s.
     * 将给定的｛@link Query｝转换为一个或多个｛@link Query｝。
     *
     * @param query The {@link Query} to be transformed.
     * @return A collection of one or more {@link Query}s derived from the original {@link Query}.
     */
    Collection<Query> transform(Query query);
}

