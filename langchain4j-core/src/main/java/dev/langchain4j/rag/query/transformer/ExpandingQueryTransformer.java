package dev.langchain4j.rag.query.transformer;

import dev.langchain4j.internal.Utils;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.query.Query;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.internal.ValidationUtils.ensureGreaterThanZero;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * A {@link QueryTransformer} that utilizes a {@link ChatModel} to expand a given {@link Query}.
 * 一个{@link QueryTransformer}，它利用{@link ChatModel}来扩展给定的{@link Query}。
 * <br>
 * Refer to {@link #DEFAULT_PROMPT_TEMPLATE} and implementation for more details.
 * 有关更多详细信息，请参阅{@link#DEFAULT_PROMPT_TEMPLATE}和实现。
 * <br>
 * <br>
 * Configurable parameters (optional):
 * 可配置参数（可选）：
 * <br>
 * - {@link #promptTemplate}: The prompt template used to instruct the LLM to expand the provided {@link Query}.
 * - ｛@link#promptTemplate｝：用于指示LLM展开提供的｛@link Query｝的提示模板。
 * <br>
 * - {@link #n}: The number of {@link Query}s to generate. Default value is 3.
 * - ｛@link #n｝：要生成的｛@link Query｝的数量。默认值为3。
 *
 * @see DefaultQueryTransformer
 * @see CompressingQueryTransformer
 */
public class ExpandingQueryTransformer implements QueryTransformer {

    /**
     生成所提供用户查询的｛｛n｝｝个不同版本。
     每个版本的措辞应该不同，使用同义词或替代句子结构，
     但它们都应该保留原始含义。
     这些版本将用于检索相关文档。
     在单独的行上提供每个查询版本非常重要，
     没有枚举、连字符或任何其他格式！
     用户查询：｛｛query｝｝
     */
    public static final PromptTemplate DEFAULT_PROMPT_TEMPLATE = PromptTemplate.from(
            """
                    Generate {{n}} different versions of a provided user query. \
                    Each version should be worded differently, using synonyms or alternative sentence structures, \
                    but they should all retain the original meaning. \
                    These versions will be used to retrieve relevant documents. \
                    It is very important to provide each query version on a separate line, \
                    without enumerations, hyphens, or any additional formatting!
                    User query: {{query}}"""
    );
    public static final int DEFAULT_N = 3;

    protected final ChatModel chatModel;
    protected final PromptTemplate promptTemplate;
    protected final int n;

    public ExpandingQueryTransformer(ChatModel chatModel) {
        this(chatModel, DEFAULT_PROMPT_TEMPLATE, DEFAULT_N);
    }

    public ExpandingQueryTransformer(ChatModel chatModel, int n) {
        this(chatModel, DEFAULT_PROMPT_TEMPLATE, n);
    }

    public ExpandingQueryTransformer(ChatModel chatModel, PromptTemplate promptTemplate) {
        this(chatModel, ensureNotNull(promptTemplate, "promptTemplate"), DEFAULT_N);
    }

    public ExpandingQueryTransformer(ChatModel chatModel, PromptTemplate promptTemplate, Integer n) {
        this.chatModel = ensureNotNull(chatModel, "chatModel");
        this.promptTemplate = getOrDefault(promptTemplate, DEFAULT_PROMPT_TEMPLATE);
        this.n = ensureGreaterThanZero(getOrDefault(n, DEFAULT_N), "n");
    }

    public static ExpandingQueryTransformerBuilder builder() {
        return new ExpandingQueryTransformerBuilder();
    }

    @Override
    public Collection<Query> transform(Query query) {
        Prompt prompt = createPrompt(query);
        String response = chatModel.chat(prompt.text());
        List<String> queries = parse(response);
        return queries.stream()
                .map(queryText -> query.metadata() == null
                        ? Query.from(queryText)
                        : Query.from(queryText, query.metadata()))
                .collect(toList());
    }

    protected Prompt createPrompt(Query query) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("query", query.text());
        variables.put("n", n);
        return promptTemplate.apply(variables);
    }

    protected List<String> parse(String queries) {
        return stream(queries.split("\n"))
                .filter(Utils::isNotNullOrBlank)
                .collect(toList());
    }

    public static class ExpandingQueryTransformerBuilder {
        private ChatModel chatModel;
        private PromptTemplate promptTemplate;
        private Integer n;

        ExpandingQueryTransformerBuilder() {
        }

        public ExpandingQueryTransformerBuilder chatModel(ChatModel chatModel) {
            this.chatModel = chatModel;
            return this;
        }

        public ExpandingQueryTransformerBuilder promptTemplate(PromptTemplate promptTemplate) {
            this.promptTemplate = promptTemplate;
            return this;
        }

        public ExpandingQueryTransformerBuilder n(Integer n) {
            this.n = n;
            return this;
        }

        public ExpandingQueryTransformer build() {
            return new ExpandingQueryTransformer(this.chatModel, this.promptTemplate, this.n);
        }
    }
}
