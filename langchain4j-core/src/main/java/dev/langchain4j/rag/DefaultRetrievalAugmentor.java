package dev.langchain4j.rag;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.aggregator.ContentAggregator;
import dev.langchain4j.rag.content.aggregator.DefaultContentAggregator;
import dev.langchain4j.rag.content.injector.ContentInjector;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.router.DefaultQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.rag.query.transformer.DefaultQueryTransformer;
import dev.langchain4j.rag.query.transformer.QueryTransformer;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

/**
 * The default implementation of {@link RetrievalAugmentor} intended to be suitable for the majority of use cases.
 * 一个默认的实现的{@link RetrievalAugmentor}，适用于大多数用例。
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
 * This implementation is inspired by <a href="https://blog.langchain.dev/deconstructing-rag">this article</a>
 * and <a href="https://arxiv.org/abs/2312.10997">this paper</a>.
 * 此实现的灵感来自<a href=“https://blog.langchain.dev/deconstructing-rag“>
 *  和<a href=”https://arxiv.org/abs/2312.10997“>
 * It is recommended to review these resources for a better understanding of the concept.
 * 建议查阅这些资源，以便更好地理解这一概念。
 * <br>
 * <br>
 * This implementation orchestrates the flow between the following base components:
 * 此实现协调了以下基本组件之间的流程：
 * <pre>
 * - {@link QueryTransformer}
 * - {@link QueryRouter}
 * - {@link ContentRetriever}
 * - {@link ContentAggregator}
 * - {@link ContentInjector}
 * </pre>
 * Visual representation of this flow can be found
 * 可以找到此流的视觉表示
 * <a href="https://docs.langchain4j.dev/img/advanced-rag.png">here</a>.
 * <br>
 * For each base component listed above, we offer several ready-to-use implementations,
 * each based on a recognized approach.
 * We intend to introduce more such implementations over time and welcome your contributions.
 * 对于上面列出的每个基本组件，我们提供了几个现成的实现，每个实现都基于一种公认的方法。
 * 我们打算随着时间的推移引入更多这样的实现，并欢迎您的贡献。
 * <br>
 * <br>
 * The flow is as follows:
 * 流程如下：
 * <br>
 * 1. A {@link Query} (derived from an original {@link UserMessage}) is transformed
 * using a {@link QueryTransformer} into one or multiple {@link Query}s.
 * 1.使用{@link QueryTransformer}将{@link Query}（从原始{@link UserMessage}派生）转换为一个或多个{@link Query}。
 * <br>
 * 2. Each {@link Query} is routed to the appropriate {@link ContentRetriever} using a {@link QueryRouter}.
 * Each {@link ContentRetriever} retrieves one or multiple {@link Content}s using a {@link Query}.
 * 2.使用{@link QueryRouter}将每个{@link Query}路由到相应的{@link ContentRetriever}。
 * 每个｛@link ContentRetriever｝使用｛@link Query｝检索一个或多个｛@link Content｝。
 * <br>
 * 3. All {@link Content}s retrieved by all {@link ContentRetriever}s using all {@link Query}s are
 * aggregated (fused/re-ranked/filtered/etc.) into a final list of {@link Content}s using a {@link ContentAggregator}.
 * 3.使用所有{@link Content}的所有{@link ContentRetriever}检索到的所有{@link Query}都使用{@link Content-Aggregator}聚合（融合/重新排名/过滤/等）到最终的{@link Content}列表中。
 * <br>
 * 4. Lastly, a final list of {@link Content}s is injected into the original {@link UserMessage}
 * using a {@link ContentInjector}.
 * 4.最后，使用{@link ContentInjector}将{@link Content}的最终列表注入到原始的{@link UserMessage}中。
 * <br>
 * <br>
 * By default, each base component (except for {@link ContentRetriever}, which needs to be provided by you)
 * is initialized with a sensible default implementation:
 * 默认情况下，每个基本组件（需要您提供的{@link ContentRetriever} 除外）都使用合理的默认实现进行初始化：
 * <pre>
 * - {@link DefaultQueryTransformer}
 * - {@link DefaultQueryRouter}
 * - {@link DefaultContentAggregator}
 * - {@link DefaultContentInjector}
 * </pre>
 * Nonetheless, you are encouraged to use one of the advanced ready-to-use implementations or create a custom one.
 * 尽管如此，我们仍鼓励您使用高级即用型实现之一或创建自定义实现。
 * <br>
 * <br>
 * When there is only a single {@link Query} and a single {@link ContentRetriever},
 * query routing and content retrieval are performed in the same thread.
 * Otherwise, an {@link Executor} is used to parallelize the processing.
 * By default, a modified (keepAliveTime is 1 second instead of 60 seconds) {@link Executors#newCachedThreadPool()}
 * is used, but you can provide a custom {@link Executor} instance.
 * 当只有一个｛@link Query｝和一个｛@link ContentRetriever｝时，查询路由和内容检索将在同一线程中执行。
 * 否则，将使用{@link Executor}来并行处理。默认情况下，使用修改后的（keepAliveTime为1秒而不是60秒）{@link Executors#newCachedThreadPool（）}，但您可以提供自定义的{@link Executioner}实例。
 *
 * @see DefaultQueryTransformer
 * @see DefaultQueryRouter
 * @see DefaultContentAggregator
 * @see DefaultContentInjector
 */
public class DefaultRetrievalAugmentor implements RetrievalAugmentor {

    private final QueryTransformer queryTransformer;
    private final QueryRouter queryRouter;
    private final ContentAggregator contentAggregator;
    private final ContentInjector contentInjector;
    private final Executor executor;

    public DefaultRetrievalAugmentor(QueryTransformer queryTransformer,
                                     QueryRouter queryRouter,
                                     ContentAggregator contentAggregator,
                                     ContentInjector contentInjector,
                                     Executor executor) {
        this.queryTransformer = getOrDefault(queryTransformer, DefaultQueryTransformer::new);
        this.queryRouter = ensureNotNull(queryRouter, "queryRouter");
        this.contentAggregator = getOrDefault(contentAggregator, DefaultContentAggregator::new);
        this.contentInjector = getOrDefault(contentInjector, DefaultContentInjector::new);
        this.executor = getOrDefault(executor, DefaultRetrievalAugmentor::createDefaultExecutor);
    }

    private static ExecutorService createDefaultExecutor() {
        return new ThreadPoolExecutor(
            0, Integer.MAX_VALUE,
            1, SECONDS,
            new SynchronousQueue<>()
        );
    }

    @Override
    public AugmentationResult augment(AugmentationRequest augmentationRequest) {

        ChatMessage chatMessage = augmentationRequest.chatMessage();
        String queryText;
        if (chatMessage instanceof UserMessage userMessage) {
            queryText = userMessage.singleText();
        } else {
            throw new IllegalArgumentException("Unsupported message type: " + chatMessage.type());
        }
        Query originalQuery = Query.from(queryText, augmentationRequest.metadata());

        // 转换成多个查询
        Collection<Query> queries = queryTransformer.transform(originalQuery);

        // 查询到的内容
        Map<Query, Collection<List<Content>>> queryToContents = process(queries);

        // 聚合查询到的内容
        List<Content> contents = contentAggregator.aggregate(queryToContents);

        // chatMessage补充增强
        ChatMessage augmentedChatMessage = contentInjector.inject(contents, chatMessage);

        // 增强后的结果，聚合后的内容
        return AugmentationResult.builder()
            .chatMessage(augmentedChatMessage)
            .contents(contents)
            .build();
    }

    private Map<Query, Collection<List<Content>>> process(Collection<Query> queries) {
        if (queries.size() == 1) {
            Query query = queries.iterator().next();
            Collection<ContentRetriever> retrievers = queryRouter.route(query);
            if (retrievers.size() == 1) {
                ContentRetriever contentRetriever = retrievers.iterator().next();
                List<Content> contents = contentRetriever.retrieve(query);
                return singletonMap(query, singletonList(contents));
            } else if (retrievers.size() > 1) {
                Collection<List<Content>> contents = retrieveFromAll(retrievers, query).join();
                return singletonMap(query, contents);
            } else {
                return emptyMap();
            }
        } else if (queries.size() > 1) {
            Map<Query, CompletableFuture<Collection<List<Content>>>> queryToFutureContents = new ConcurrentHashMap<>();
            queries.forEach(query -> {
                CompletableFuture<Collection<List<Content>>> futureContents =
                        supplyAsync(() -> queryRouter.route(query), executor)
                                .thenCompose(retrievers -> retrieveFromAll(retrievers, query));
                queryToFutureContents.put(query, futureContents);
            });
            return join(queryToFutureContents);
        } else {
            return emptyMap();
        }
    }

    private CompletableFuture<Collection<List<Content>>> retrieveFromAll(Collection<ContentRetriever> retrievers,
                                                                         Query query) {
        List<CompletableFuture<List<Content>>> futureContents = retrievers.stream()
            .map(retriever -> supplyAsync(() -> retriever.retrieve(query), executor))
            .collect(Collectors.toList());

        return allOf(futureContents.toArray(new CompletableFuture[0]))
            .thenApply(ignored ->
                futureContents.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList()));
    }

    private static Map<Query, Collection<List<Content>>> join(
        Map<Query, CompletableFuture<Collection<List<Content>>>> queryToFutureContents) {
        return allOf(queryToFutureContents.values().toArray(new CompletableFuture[0]))
            .thenApply(ignored ->
                queryToFutureContents.entrySet().stream()
                    .collect(toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().join()
                    ))
            ).join();
    }

    public static DefaultRetrievalAugmentorBuilder builder() {
        return new DefaultRetrievalAugmentorBuilder();
    }

    public static class DefaultRetrievalAugmentorBuilder {

        private QueryTransformer queryTransformer;
        private QueryRouter queryRouter;
        private ContentAggregator contentAggregator;
        private ContentInjector contentInjector;
        private Executor executor;

        DefaultRetrievalAugmentorBuilder() {
        }

        public DefaultRetrievalAugmentorBuilder contentRetriever(ContentRetriever contentRetriever) {
            this.queryRouter = new DefaultQueryRouter(ensureNotNull(contentRetriever, "contentRetriever"));
            return this;
        }

        public DefaultRetrievalAugmentorBuilder queryTransformer(QueryTransformer queryTransformer) {
            this.queryTransformer = queryTransformer;
            return this;
        }

        public DefaultRetrievalAugmentorBuilder queryRouter(QueryRouter queryRouter) {
            this.queryRouter = queryRouter;
            return this;
        }

        public DefaultRetrievalAugmentorBuilder contentAggregator(ContentAggregator contentAggregator) {
            this.contentAggregator = contentAggregator;
            return this;
        }

        public DefaultRetrievalAugmentorBuilder contentInjector(ContentInjector contentInjector) {
            this.contentInjector = contentInjector;
            return this;
        }

        public DefaultRetrievalAugmentorBuilder executor(Executor executor) {
            this.executor = executor;
            return this;
        }

        public DefaultRetrievalAugmentor build() {
            return new DefaultRetrievalAugmentor(this.queryTransformer, this.queryRouter, this.contentAggregator, this.contentInjector, this.executor);
        }
    }
}
