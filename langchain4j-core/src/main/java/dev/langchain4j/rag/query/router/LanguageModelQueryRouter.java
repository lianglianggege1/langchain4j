package dev.langchain4j.rag.query.router;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.internal.ValidationUtils.ensureNotBlank;
import static dev.langchain4j.internal.ValidationUtils.ensureNotEmpty;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;
import static dev.langchain4j.rag.query.router.LanguageModelQueryRouter.FallbackStrategy.DO_NOT_ROUTE;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * A {@link QueryRouter} that utilizes a {@link ChatModel} to make a routing decision.
 * 一个利用{@link ChatModel}进行路由决策的{@link QueryRouter}。
 * <br>
 * Each {@link ContentRetriever} provided in the constructor should be accompanied by a description which
 * should help the LLM to decide where to route a {@link Query}.
 * 构造函数中提供的每个{@link ContentRetriever}都应附有一个描述，该描述应有助于LLM决定将{@link Query}路由到何处。
 * <br>
 * Refer to {@link #DEFAULT_PROMPT_TEMPLATE} and implementation for more details.
 * 有关更多详细信息，请参阅{@link#DEFAULT_PROMPT_TEMPLATE}和实现。
 * <br>
 * <br>
 * Configurable parameters (optional):
 * 配置参数（可选）：
 * <br>
 * - {@link #promptTemplate}: The prompt template used to ask the LLM for routing decisions.
 * - {@link#promptTemplate}：用于向LLM询问路由决策的提示模板。
 * <br>
 * - {@link #fallbackStrategy}: The strategy applied if the call to the LLM fails of if LLM does not return a valid response.
 * Please check {@link FallbackStrategy} for more details. Default value: {@link FallbackStrategy#DO_NOT_ROUTE}
 * - ｛@link #fallbackStrategy｝：如果对LLM的调用失败或LLM没有返回有效响应，则应用的策略。
 * 请查看｛@link FallbackStrategy｝了解更多详细信息。默认值：{@link回退策略#DO_NOT_ROUTE}
 *
 * @see DefaultQueryRouter
 */
public class LanguageModelQueryRouter implements QueryRouter {

    /**
     根据用户查询，确定最合适的数据源
     从以下选项中检索相关信息：
     ｛｛options｝｝
     你的答案必须由一个数字组成，这一点非常重要
     或者用逗号分隔的多个数字，没有别的！
     用户查询：｛｛query｝｝
     */
    public static final PromptTemplate DEFAULT_PROMPT_TEMPLATE = PromptTemplate.from(
            """
                    Based on the user query, determine the most suitable data source(s) \
                    to retrieve relevant information from the following options:
                    {{options}}
                    It is very important that your answer consists of either a single number \
                    or multiple numbers separated by commas and nothing else!
                    User query: {{query}}"""
    );

    protected final ChatModel chatModel;
    protected final PromptTemplate promptTemplate;
    protected final String options;
    protected final Map<Integer, ContentRetriever> idToRetriever;
    protected final FallbackStrategy fallbackStrategy;

    public LanguageModelQueryRouter(ChatModel chatModel,
                                    Map<ContentRetriever, String> retrieverToDescription) {
        this(chatModel, retrieverToDescription, DEFAULT_PROMPT_TEMPLATE, DO_NOT_ROUTE);
    }

    public LanguageModelQueryRouter(ChatModel chatModel,
                                    Map<ContentRetriever, String> retrieverToDescription,
                                    PromptTemplate promptTemplate,
                                    FallbackStrategy fallbackStrategy) {
        this.chatModel = ensureNotNull(chatModel, "chatModel");
        ensureNotEmpty(retrieverToDescription, "retrieverToDescription");
        this.promptTemplate = getOrDefault(promptTemplate, DEFAULT_PROMPT_TEMPLATE);

        Map<Integer, ContentRetriever> idToRetriever = new HashMap<>();
        StringBuilder optionsBuilder = new StringBuilder();
        int id = 1;
        for (Map.Entry<ContentRetriever, String> entry : retrieverToDescription.entrySet()) {
            idToRetriever.put(id, ensureNotNull(entry.getKey(), "ContentRetriever"));

            if (id > 1) {
                optionsBuilder.append("\n");
            }
            optionsBuilder.append(id);
            optionsBuilder.append(": ");
            optionsBuilder.append(ensureNotBlank(entry.getValue(), "ContentRetriever description"));

            id++;
        }
        this.idToRetriever = idToRetriever;
        this.options = optionsBuilder.toString();
        this.fallbackStrategy = getOrDefault(fallbackStrategy, DO_NOT_ROUTE);
    }

    public static LanguageModelQueryRouterBuilder builder() {
        return new LanguageModelQueryRouterBuilder();
    }

    @Override
    public Collection<ContentRetriever> route(Query query) {
        Prompt prompt = createPrompt(query);
        try {
            String response = chatModel.chat(prompt.text());
            return parse(response);
        } catch (Exception e) {
            return fallback(query, e);
        }
    }

    protected Collection<ContentRetriever> fallback(Query query, Exception e) {
        return switch (fallbackStrategy) {
            case DO_NOT_ROUTE -> {
                yield emptyList();
            }
            case ROUTE_TO_ALL -> {
                yield new ArrayList<>(idToRetriever.values());
            }
            default -> throw new RuntimeException(e);
        };
    }

    protected Prompt createPrompt(Query query) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("query", query.text());
        variables.put("options", options);
        return promptTemplate.apply(variables);
    }

    protected Collection<ContentRetriever> parse(String choices) {
        return stream(choices.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .map(idToRetriever::get)
                .collect(toList());
    }

    /**
     * Strategy applied if the call to the LLM fails of if LLM does not return a valid response.
     * It could be because it was formatted improperly, or it is unclear where to route.
     * 如果对LLM的调用失败或LLM未返回有效响应，则应用策略。这可能是因为它的格式不正确，或者不清楚在哪里路由。
     */
    public enum FallbackStrategy {

        /**
         * In this case, the {@link Query} will not be routed to any {@link ContentRetriever},
         * thus skipping the RAG flow. No content will be appended to the original {@link UserMessage}.
         * 在这种情况下，｛@link Query｝将不会被路由到任何｛@link ContentRetriever｝，
         * 从而跳过RAG流。原始｛@link UserMessage｝将不会附加任何内容。
         */
        DO_NOT_ROUTE,

        /**
         * In this case, the {@link Query} will be routed to all {@link ContentRetriever}s.
         * 在这种情况下，｛@link查询｝将被路由到所有｛@link ContentRetriever｝。
         */
        ROUTE_TO_ALL,

        /**
         * In this case, an original exception will be re-thrown, and the RAG flow will fail.
         * 在这种情况下，原始异常将被重新抛出，RAG流将失败。
         */
        FAIL
    }

    public static class LanguageModelQueryRouterBuilder {
        // 语言模型
        private ChatModel chatModel;
        //  检索器描述
        private Map<ContentRetriever, String> retrieverToDescription;
        private PromptTemplate promptTemplate;
        private FallbackStrategy fallbackStrategy;

        LanguageModelQueryRouterBuilder() {
        }

        public LanguageModelQueryRouterBuilder chatModel(ChatModel chatModel) {
            this.chatModel = chatModel;
            return this;
        }

        public LanguageModelQueryRouterBuilder retrieverToDescription(Map<ContentRetriever, String> retrieverToDescription) {
            this.retrieverToDescription = retrieverToDescription;
            return this;
        }

        public LanguageModelQueryRouterBuilder promptTemplate(PromptTemplate promptTemplate) {
            this.promptTemplate = promptTemplate;
            return this;
        }

        public LanguageModelQueryRouterBuilder fallbackStrategy(FallbackStrategy fallbackStrategy) {
            this.fallbackStrategy = fallbackStrategy;
            return this;
        }

        public LanguageModelQueryRouter build() {
            return new LanguageModelQueryRouter(this.chatModel, this.retrieverToDescription, this.promptTemplate, this.fallbackStrategy);
        }
    }
}
