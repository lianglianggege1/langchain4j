package dev.langchain4j.rag.query.router;

import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;

import java.util.Collection;

import static dev.langchain4j.internal.ValidationUtils.ensureNotEmpty;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;

/**
 * Default implementation of {@link QueryRouter} intended to be suitable for the majority of use cases.
 * {@link QueryRouter}的默认实现旨在适用于大多数用例。
 * <br>
 * <br>
 * It's important to note that while efforts will be made to avoid breaking changes,
 * the default behavior of this class may be updated in the future if it's found
 * that the current behavior does not adequately serve the majority of use cases.
 * Such changes would be made to benefit both current and future users.
 * 值得注意的是，虽然将努力避免破坏性更改，但如果发现当前行为不能充分服务于大多数用例，
 * 则该类的默认行为可能会在未来进行更新。这些变化将使当前和未来的用户受益。
 * <br>
 * <br>
 * This implementation always routes all {@link Query}s
 * to one or multiple {@link ContentRetriever}s provided in the constructor.
 * 此实现始终路由所有｛@link Query｝s
 * 指向构造函数中提供的一个或多个{@link ContentRetriever}。
 *
 * @see LanguageModelQueryRouter
 */
public class DefaultQueryRouter implements QueryRouter {

    // 内容检索器
    private final Collection<ContentRetriever> contentRetrievers;

    public DefaultQueryRouter(ContentRetriever... contentRetrievers) {
        this(asList(contentRetrievers));
    }

    public DefaultQueryRouter(Collection<ContentRetriever> contentRetrievers) {
        this.contentRetrievers = unmodifiableCollection(ensureNotEmpty(contentRetrievers, "contentRetrievers"));
    }

    @Override
    public Collection<ContentRetriever> route(Query query) {
        return contentRetrievers;
    }
}
