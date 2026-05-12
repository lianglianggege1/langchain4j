package dev.langchain4j.store.embedding.filter;

/**
 * Parses a filter expression string into a {@link Filter} object.
 * <br>
 * Currently, there is only one implementation: {@code SqlFilterParser}
 * in the {@code langchain4j-embedding-store-filter-parser-sql} module.
 */
/**
 * 将过滤表达式字符串解析为 {@link Filter} 对象。
 * <br>
 * 目前仅有一种实现：位于 {@code langchain4j-embedding-store-filter-parser-sql} 模块中的 {@code SqlFilterParser}。
 */
public interface FilterParser {

    /**
     * Parses a filter expression string into a {@link Filter} object.
     *
     * @param filter The filter expression as a string.
     * @return A {@link Filter} object.
     */
    /**
     * 将过滤表达式字符串解析为 {@link Filter} 对象。
     *
     * @param filter 字符串形式的过滤表达式
     * @return {@link Filter} 对象
     */
    Filter parse(String filter);
}
