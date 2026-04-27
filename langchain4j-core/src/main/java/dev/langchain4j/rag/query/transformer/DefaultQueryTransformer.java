package dev.langchain4j.rag.query.transformer;

import dev.langchain4j.rag.query.Query;

import java.util.Collection;

import static java.util.Collections.singletonList;

/**
 * Default implementation of {@link QueryTransformer} intended to be suitable for the majority of use cases.
 * {@link QueryTransformer}的默认实现旨在适用于大多数用例。
 * <br>
 * <br>
 * It's important to note that while efforts will be made to avoid breaking changes,
 * the default behavior of this class may be updated in the future if it's found
 * that the current behavior does not adequately serve the majority of use cases.
 * Such changes would be made to benefit both current and future users.
 * 值得注意的是，虽然将努力避免破坏性更改，但如果发现当前行为不能充分服务于大多数用例，
 * 则该类的默认行为可能会在未来进行更新。这些变化将使当前和未来的用户受益
 * <br>
 * <br>
 * This implementation simply returns the provided {@link Query} without any transformation.
 *
 * @see CompressingQueryTransformer
 * @see ExpandingQueryTransformer
 */
public class DefaultQueryTransformer implements QueryTransformer {

    @Override
    public Collection<Query> transform(Query query) {
        return singletonList(query);
    }
}
