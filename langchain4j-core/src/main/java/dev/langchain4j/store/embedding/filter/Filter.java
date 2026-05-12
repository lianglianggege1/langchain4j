package dev.langchain4j.store.embedding.filter;

import dev.langchain4j.internal.JacocoIgnoreCoverageGenerated;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.comparison.ContainsString;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import dev.langchain4j.store.embedding.filter.comparison.IsGreaterThan;
import dev.langchain4j.store.embedding.filter.comparison.IsGreaterThanOrEqualTo;
import dev.langchain4j.store.embedding.filter.comparison.IsIn;
import dev.langchain4j.store.embedding.filter.comparison.IsLessThan;
import dev.langchain4j.store.embedding.filter.comparison.IsLessThanOrEqualTo;
import dev.langchain4j.store.embedding.filter.comparison.IsNotEqualTo;
import dev.langchain4j.store.embedding.filter.comparison.IsNotIn;
import dev.langchain4j.store.embedding.filter.logical.And;
import dev.langchain4j.store.embedding.filter.logical.Not;
import dev.langchain4j.store.embedding.filter.logical.Or;

/**
 * This class represents a filter that can be applied during search in an {@link EmbeddingStore}.
 * <br>
 * Many {@link EmbeddingStore}s support a feature called metadata filtering. A {@code Filter} can be used for this.
 * <br>
 * A {@code Filter} object can represent simple (e.g. {@code type = 'documentation'})
 * and composite (e.g. {@code type = 'documentation' AND year > 2020}) filter expressions in
 * an {@link EmbeddingStore}-agnostic way.
 * <br>
 * Each {@link EmbeddingStore} implementation that supports metadata filtering is mapping {@link Filter}
 * into it's native filter expression.
 *
 * @see IsEqualTo
 * @see IsNotEqualTo
 * @see IsGreaterThan
 * @see IsGreaterThanOrEqualTo
 * @see IsLessThan
 * @see IsLessThanOrEqualTo
 * @see IsIn
 * @see IsNotIn
 * @see ContainsString
 * @see And
 * @see Not
 * @see Or
 */
/**
 * 该类表示一种可在{@link EmbeddingStore}搜索过程中应用的过滤器。
 * <br>
 * 多数{@link EmbeddingStore}均支持元数据过滤功能，{@code Filter}类可用于实现该功能。
 * <br>
 * {@code Filter}对象能够以与{@link EmbeddingStore}无关的通用方式，表示简单过滤表达式（如{@code 类型 = '文档'}）
 * 以及组合过滤表达式（如{@code 类型 = '文档' 且 年份 > 2020}）。
 * <br>
 * 所有支持元数据过滤的{@link EmbeddingStore}实现类，都会将{@link Filter}转换为自身原生的过滤表达式。
 *
 * @see 等于条件
 * @see 不等于条件
 * @see 大于条件
 * @see 大于等于条件
 * @see 小于条件
 * @see 小于等于条件
 * @see 包含于条件
 * @see 不包含于条件
 * @see 包含字符串条件
 * @see 逻辑与组合
 * @see 逻辑非
 * @see 逻辑或组合
 */
@JacocoIgnoreCoverageGenerated
public interface Filter {

    /**
     * Tests if a given object satisfies this {@link Filter}.
     *
     * @param object An object to test.
     * @return {@code true} if a given object satisfies this {@link Filter}, {@code false} otherwise.
     */
    /**
     * 校验指定对象是否满足当前{@link Filter}过滤器的条件。
     *
     * @param object 待校验的对象。
     * @return 若指定对象满足该{@link Filter}过滤器条件则返回{@code true}，否则返回{@code false}。
     */
    boolean test(Object object);

    default Filter and(Filter filter) {
        return and(this, filter);
    }

    static Filter and(Filter left, Filter right) {
        return new And(left, right);
    }

    default Filter or(Filter filter) {
        return or(this, filter);
    }

    static Filter or(Filter left, Filter right) {
        return new Or(left, right);
    }

    static Filter not(Filter expression) {
        return new Not(expression);
    }
}
