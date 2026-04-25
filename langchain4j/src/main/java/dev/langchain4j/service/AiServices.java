package dev.langchain4j.service;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;
import static dev.langchain4j.service.IllegalConfigurationException.illegalConfiguration;
import static dev.langchain4j.spi.ServiceHelper.loadFactory;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import dev.langchain4j.Internal;
import dev.langchain4j.agent.tool.ReturnBehavior;
import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.exception.ToolArgumentsException;
import dev.langchain4j.exception.ToolExecutionException;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.config.InputGuardrailsConfig;
import dev.langchain4j.guardrail.config.OutputGuardrailsConfig;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.CompleteToolCall;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.input.structured.StructuredPrompt;
import dev.langchain4j.model.moderation.Moderation;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.observability.api.event.AiServiceEvent;
import dev.langchain4j.observability.api.listener.AiServiceListener;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.tool.AiServiceTool;
import dev.langchain4j.service.tool.BeforeToolExecution;
import dev.langchain4j.service.tool.DefaultToolExecutor;
import dev.langchain4j.service.tool.ToolArgumentsErrorHandler;
import dev.langchain4j.service.tool.ToolExecution;
import dev.langchain4j.service.tool.ToolExecutionErrorHandler;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.search.ToolSearchStrategy;
import dev.langchain4j.spi.services.AiServicesFactory;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * AI Services is a high-level API of LangChain4j to interact with {@link ChatModel} and {@link StreamingChatModel}.
 * AI Services 是 LangChain4j 的高级 API，用于与 {@link ChatModel} 和 {@link StreamingChatModel} 进行交互。
 * <p>
 * You can define your own API (a Java interface with one or more methods),
 * and {@code AiServices} will provide an implementation for it, hiding all the complexity from you.
 * 您可以定义自己的 API（一个包含一个或多个方法的 Java 接口），{@code AiServices} 将为其提供实现，从而隐藏所有复杂性。
 * <p>
 * You can find more details <a href="https://docs.langchain4j.dev/tutorials/ai-services">here</a>.
 * 您可以在这里找到更多详细信息<a href="https://docs.langchain4j.dev/tutorials/ai-services"></a>。
 * <p>
 * Please note that AI Service should not be called concurrently for the same @{@link MemoryId},
 * as it can lead to corrupted {@link ChatMemory}. Currently, AI Service does not implement any mechanism
 * to prevent concurrent calls for the same @{@link MemoryId}.
 * 请注意，不应针对同一个 @{@link MemoryId} 并发调用 AI 服务，否则可能导致 {@link ChatMemory} 损坏。
 * 目前，AI 服务尚未实现任何机制来阻止针对同一个 @{@link MemoryId} 的并发调用。
 * <p>
 * Currently, AI Services support:
 * 当前，AI Services支持：
 * <pre>
 * - Static system message templates, configured via @{@link SystemMessage} annotation on top of the method
 * - 静态系统消息模板，通过方法顶部的 @{@link SystemMessage} 注解进行配置
 * - Dynamic system message templates, configured via {@link #systemMessageProvider(Function)}
 * - 动态系统消息模板，通过 {@link #systemMessageProvider(Function)} 配置
 * - Static user message templates, configured via @{@link UserMessage} annotation on top of the method
 * - 静态用户消息模板，通过方法顶部的 @{@link UserMessage} 注解进行配置
 * - Dynamic user message templates, configured via method parameter annotated with @{@link UserMessage}
 * - 动态用户消息模板，通过带有 @{@link UserMessage} 注解的方法参数进行配置
 * - Single (shared) {@link ChatMemory}, configured via {@link #chatMemory(ChatMemory)}
 * - 单个（共享）{@link ChatMemory}，通过 {@link #chatMemory(ChatMemory)} 配置
 * - Separate (per-user) {@code ChatMemory}, configured via {@link #chatMemoryProvider(ChatMemoryProvider)} and a method parameter annotated with @{@link MemoryId}
 * - 独立的（每个用户）{@code ChatMemory}，通过 {@link #chatMemoryProvider(ChatMemoryProvider)} 和带有 @{@link MemoryId} 注解的方法参数进行配置。
 * - RAG, configured via {@link #contentRetriever(ContentRetriever)} or {@link #retrievalAugmentor(RetrievalAugmentor)}
 * - RAG，可通过 {@link #contentRetriever(ContentRetriever)} 或 {@link #retrievalAugmentor(RetrievalAugmentor)} 进行配置。
 * - Tools, configured via {@link #tools(Collection)}, {@link #tools(Object...)}, {@link #tools(Map)} or {@link #toolProvider(ToolProvider)} and methods annotated with @{@link Tool}
 * - 工具可通过 {@link #tools(Collection)}、{@link #tools(Object...)}、{@link #tools(Map)} 或 {@link #toolProvider(ToolProvider)} 进行配置，方法则使用 @{@link Tool} 注解。
 * - Various method return types (output parsers), see more details below
 * - 方法返回类型（输出解析器）多种多样，详情请见下文。
 * - Streaming (use {@link TokenStream} as a return type)
 * - 流式传输（使用 {@link TokenStream} 作为返回类型）
 * - Structured prompts as method arguments (see @{@link StructuredPrompt})
 * - 将结构化提示作为方法参数（参见 @{@link StructuredPrompt}）
 * - Auto-moderation, configured via @{@link Moderate} annotation
 * - 自动审核，通过 @{@link Moderate} 注解进行配置
 * </pre>
 * <p>
 * Here is the simplest example of an AI Service:
 *  简单的 AI Service 的例子：
 *
 * <pre>
 * interface Assistant {
 *
 *     String chat(String userMessage);
 * }
 *
 * Assistant assistant = AiServices.create(Assistant.class, model);
 *
 * String answer = assistant.chat("hello");
 * System.out.println(answer); // Hello, how can I help you today?
 * </pre>
 *
 * <pre>
 * The return type of methods in your AI Service can be any of the following:
 * AI 服务中方法的返回类型可以是以下任何一种：
 * - a {@link String} or an {@link AiMessage}, if you want to get the answer from the LLM as-is
 * - 如果您想直接从 LLM 获取答案，请使用 {@link String} 或 {@link AiMessage}。
 * - a {@code List<String>} or {@code Set<String>}, if you want to receive the answer as a collection of items or bullet points
 * - 如果您希望以项目或要点集合的形式接收答案，请使用 {@code List<String>} 或 {@code Set<String>}。
 * - any {@link Enum} or a {@code boolean}, if you want to use the LLM for classification
 * - 如果要使用 LLM 进行分类，则可以使用任何 {@link Enum} 或 {@code boolean}。
 * - a primitive or boxed Java type: {@code int}, {@code Double}, etc., if you want to use the LLM for data extraction
 * - 如果您想使用 LLM 进行数据提取，则需要使用 Java 基本类型或装箱类型：{@code int}、{@code Double} 等。
 * - many default Java types: {@code Date}, {@code LocalDateTime}, {@code BigDecimal}, etc., if you want to use the LLM for data extraction
 * - 许多默认的 Java 数据类型：{@code Date}、{@code LocalDateTime}、{@code BigDecimal} 等，如果您想使用 LLM 进行数据提取。
 * - any custom POJO, if you want to use the LLM for data extraction.
 * - 任何自定义 POJO，如果要使用 LLM 进行数据提取。
 * - Result&lt;T&gt; if you want to access {@link TokenUsage} or sources ({@link Content}s retrieved during RAG), aside from T, which can be of any type listed above. For example: Result&lt;String&gt;, Result&lt;MyCustomPojo&gt;
 * - Result<T> 如果您想访问 {@link TokenUsage} 或源（在 RAG 期间检索的 {@link Content}），除了 T 之外，T 可以是上面列出的任何类型。例如：Result<String>、Result<MyCustomPojo>
 * For POJOs, it is advisable to use the "json mode" feature if the LLM provider supports it. For OpenAI, this can be enabled by calling {@code responseFormat("json_object")} during model construction.
 * 对于 POJO，如果 LLM 提供程序支持，建议使用“json 模式”功能。对于 OpenAI，可以在模型构建期间调用 {@code responseFormat("json_object")} 来启用此功能。
 *
 * </pre>
 * <p>
 * Let's see how we can classify the sentiment of a text:
 * 让我们看看如何对文本进行分类：
 * <pre>
 * enum Sentiment {
 *     POSITIVE, NEUTRAL, NEGATIVE
 * }
 *
 * interface SentimentAnalyzer {
 *
 *     {@code @UserMessage}("Analyze sentiment of {{it}}")
 *     Sentiment analyzeSentimentOf(String text);
 * }
 *
 * SentimentAnalyzer assistant = AiServices.create(SentimentAnalyzer.class, model);
 *
 * Sentiment sentiment = analyzeSentimentOf.chat("I love you");
 * System.out.println(sentiment); // POSITIVE
 * </pre>
 * <p>
 * As demonstrated, you can put @{@link UserMessage} and @{@link SystemMessage} annotations above a method to define
 * templates for user and system messages, respectively.
 * 如示例所示，您可以在方法上方放置 @{@link UserMessage} 和 @{@link SystemMessage} 注解，分别用于定义用户消息和系统消息的模板。
 * In this example, the special {@code {{it}}} prompt template variable is used because there's only one method parameter.
 * 在这个例子中，使用了特殊的 {@code {{it}}} 提示模板变量，因为只有一个方法参数。
 * However, you can use more parameters as demonstrated in the following example:
 * 但是，您可以使用更多参数，如下例所示：
 * <pre>
 * interface Translator {
 *
 *     {@code @SystemMessage}("You are a professional translator into {{language}}")
 *     {@code @UserMessage}("Translate the following text: {{text}}")
 *     String translate(@V("text") String text, @V("language") String language);
 * }
 * </pre>
 * <p>
 * See more examples <a href="https://github.com/langchain4j/langchain4j-examples/tree/main/other-examples/src/main/java">here</a>.
 *
 * @param <T> The interface for which AiServices will provide an implementation.
 * @param <T> AiServices 将为其提供实现的接口。
 */
public abstract class AiServices<T> {

    //  上下文 AiService
    protected final AiServiceContext context;

    // 内容检索器套装
    private boolean contentRetrieverSet = false;
    // 检索增强器套装
    private boolean retrievalAugmentorSet = false;

    protected AiServices(AiServiceContext context) {
        this.context = context;
    }

    /**
     * Creates an AI Service (an implementation of the provided interface), that is backed by the provided chat model.
     * 创建一个由所提供的聊天模型支持的 AI 服务（所提供接口的实现）。
     * This convenience method can be used to create simple AI Services.
     * 这种便捷的方法可用于创建简单的AI服务。
     * For more complex cases, please use {@link #builder}.
     * 对于更复杂的情况，请使用 {@link #builder}。
     *
     * @param aiService The class of the interface to be implemented.
     *                  要实现的接口的类。
     * @param chatModel The chat model to be used under the hood.
     *                  底层使用的聊天模型。
     * @return An instance of the provided interface, implementing all its defined methods.
     *         提供所提供接口的一个实例，并实现其所有定义的方法。
     */
    public static <T> T create(Class<T> aiService, ChatModel chatModel) {
        return builder(aiService).chatModel(chatModel).build();
    }

    /**
     * Creates an AI Service (an implementation of the provided interface), that is backed by the provided streaming chat model.
     * This convenience method can be used to create simple AI Services.
     * For more complex cases, please use {@link #builder}.
     *
     * @param aiService          The class of the interface to be implemented.
     * @param streamingChatModel The streaming chat model to be used under the hood.
     *                           The return type of all methods should be {@link TokenStream}.
     * @return An instance of the provided interface, implementing all its defined methods.
     */
    public static <T> T create(Class<T> aiService, StreamingChatModel streamingChatModel) {
        return builder(aiService).streamingChatModel(streamingChatModel).build();
    }

    /**
     * Begins the construction of an AI Service.
     * 开始构建人工智能服务。
     *
     * @param aiService The class of the interface to be implemented.
     *                  要实现的接口的类。
     * @return builder
     */
    public static <T> AiServices<T> builder(Class<T> aiService) {
        AiServiceContext context = AiServiceContext.create(aiService);
        return builder(context);
    }

    private static class FactoryHolder {
        private static final AiServicesFactory aiServicesFactory = loadFactory(AiServicesFactory.class);
    }

    @Internal
    public static <T> AiServices<T> builder(AiServiceContext context) {
        return FactoryHolder.aiServicesFactory != null
                ? FactoryHolder.aiServicesFactory.create(context)
                : new DefaultAiServices<>(context);
    }

    /**
     * Configures chat model that will be used under the hood of the AI Service.
     * 配置人工智能服务底层将使用的聊天模型。
     * <p>
     * Either {@link ChatModel} or {@link StreamingChatModel} should be configured,
     * but not both at the same time.
     * ChatModel 或 StreamingChatModel 只能配置其中之一，不能同时配置两者。
     *
     * @param chatModel Chat model that will be used under the hood of the AI Service.
     * @return builder
     */
    public AiServices<T> chatModel(ChatModel chatModel) {
        context.chatModel = chatModel;
        return this;
    }

    /**
     * Configures streaming chat model that will be used under the hood of the AI Service.
     * 配置 AI 服务底层将使用的流式聊天模型。
     * The methods of the AI Service must return a {@link TokenStream} type.
     * AI 服务的方法必须返回 {@link TokenStream} 类型。
     * <p>
     * Either {@link ChatModel} or {@link StreamingChatModel} should be configured,
     * but not both at the same time.
     * 应该配置 {@link ChatModel} 或 {@link StreamingChatModel}，但不能同时配置两者。
     *
     * @param streamingChatModel Streaming chat model that will be used under the hood of the AI Service.
     * @param streamingChatModel 将在 AI 服务底层使用的流式聊天模型。
     * @return builder
     */
    public AiServices<T> streamingChatModel(StreamingChatModel streamingChatModel) {
        context.streamingChatModel = streamingChatModel;
        return this;
    }

    /**
     * Configures the system message to be used each time an AI service is invoked.
     * 配置每次调用 AI 服务时要使用的系统消息。
     * It can be either a complete system message or a system message template containing unresolved template
     * variables (e.g. "{{name}}"), which will be resolved using the values of method parameters annotated with @{@link V}.
     * 它可以是完整的系统消息，也可以是包含未解析模板变量（例如“{{name}}”）的系统消息模板，
     * 这些未解析模板变量将使用带有 @V 注解的方法参数的值进行解析。
     * <br>
     * When both {@code @SystemMessage} and the system message provider are configured,
     * {@code @SystemMessage} takes precedence.
     * 当同时配置了 @SystemMessage 和系统消息提供程序时，@SystemMessage 优先。
     *
     * @param systemMessage The system message to be used.
     *                      要使用的系统消息。
     * @return builder
     */
    public AiServices<T> systemMessage(String systemMessage) {
        return systemMessageProvider(ignore -> systemMessage);
    }

    /**
     * Configures the system message provider, which provides a system message to be used each time an AI service is invoked.
     * 配置系统消息提供程序，该提供程序提供每次调用 AI 服务时要使用的系统消息。
     * <br>
     * When both {@code @SystemMessage} and the system message provider are configured,
     * {@code @SystemMessage} takes precedence.
     * 当同时配置了 @SystemMessage 和系统消息提供程序时，@SystemMessage 优先。
     *
     * @param systemMessageProvider A {@link Function} that accepts a chat memory ID
     *                              (a value of a method parameter annotated with @{@link MemoryId})
     *                              and returns a system message to be used.
     *                              If there is no parameter annotated with {@code @MemoryId},
     *                              the value of memory ID is "default".
     *                              The returned {@link String} can be either a complete system message
     *                              or a system message template containing unresolved template variables (e.g. "{{name}}"),
     *                              which will be resolved using the values of method parameters annotated with @{@link V}.
     *                              一个函数，它接受一个聊天内存 ID（一个带有 @MemoryId 注解的方法参数的值），
     *                              并返回一条要使用的系统消息。
     *                              如果没有带有 @MemoryId 注解的参数，
     *                              则内存 ID 的值为“default”。
     *                              返回的字符串可以是完整的系统消息，
     *                              也可以是包含未解析模板变量（例如“{{name}}”）的系统消息模板，
     *                              这些未解析的模板变量将使用带有 @V 注解的方法参数的值进行解析。
     * @return builder
     */
    public AiServices<T> systemMessageProvider(Function<Object, String> systemMessageProvider) {
        context.systemMessageProvider = systemMessageProvider.andThen(Optional::ofNullable);
        return this;
    }

    /**
     * Configures a transformer that will be applied to the system message on each AI service invocation,
     * after all other system message configuration (i.e., {@code @SystemMessage} annotation and
     * {@link #systemMessageProvider(Function)}) has been applied, but before the
     * {@link #chatRequestTransformer(UnaryOperator)} is invoked.
     * 配置一个转换器，该转换器将在每次 AI 服务调用时应用于系统消息，
     * 在应用所有其他系统消息配置（即 @SystemMessage 注解和 systemMessageProvider(Function)）之后，
     * 但在调用 chatRequestTransformer(UnaryOperator) 之前。
     * <p>
     * This can be used to dynamically modify the content of the system message,
     * for example to append or prepend additional instructions.
     * The transformer receives the current system message text (or {@code null} if no system message
     * has been configured) and must return the new system message text.
     * 这可用于动态修改系统消息的内容，例如添加或添加其他指令。
     * 转换器接收当前系统消息文本（如果未配置系统消息，则接收 null），
     * 并必须返回新的系统消息文本。
     *
     * @param systemMessageTransformer A {@link UnaryOperator} that accepts the current system message
     *                                 text and returns the transformed text.
     *                                 一个接受当前系统消息文本并返回转换后文本的一元运算符。
     * @return builder
     * @see #systemMessageTransformer(BiFunction)
     * @since 1.12.0
     */
    public AiServices<T> systemMessageTransformer(UnaryOperator<String> systemMessageTransformer) {
        context.systemMessageTransformer = (msg, ctx) -> systemMessageTransformer.apply(msg);
        return this;
    }

    /**
     * Configures a transformer that will be applied to the system message on each AI service invocation,
     * after all other system message configuration (i.e., {@code @SystemMessage} annotation and
     * {@link #systemMessageProvider(Function)}) has been applied, but before the
     * {@link #chatRequestTransformer(UnaryOperator)} is invoked.
     * <p>
     * This can be used to dynamically modify the content of the system message,
     * for example to append or prepend additional instructions.
     * The transformer receives the current system message text (or {@code null} if no system message
     * has been configured) and the {@link InvocationContext} of the current invocation,
     * and must return the new system message text.
     *
     * @param systemMessageTransformer A {@link BiFunction} that accepts the current system message text
     *                                 and the {@link InvocationContext}, and returns the transformed text.
     * @return builder
     * @see #systemMessageTransformer(UnaryOperator)
     * @since 1.12.0
     */
    public AiServices<T> systemMessageTransformer(
            BiFunction<String, InvocationContext, String> systemMessageTransformer) {
        context.systemMessageTransformer = systemMessageTransformer;
        return this;
    }

    /**
     * Configures the user message to be used each time an AI service is invoked.
     * It can be either a complete user message or a user message template containing unresolved template
     * variables (e.g. "{{name}}"), which will be resolved using the values of method parameters annotated with @{@link V}.
     * 配置每次调用 AI 服务时要使用的用户消息。
     * 它可以是完整的用户消息，也可以是包含未解析模板变量（例如“{{name}}”）的用户消息模板，
     * 这些未解析的模板变量将使用带有 @V 注解的方法参数的值进行解析。
     * <br>
     * When both {@code @UserMessage} and the user message provider are configured,
     * {@code @UserMessage} takes precedence.
     * 当同时配置了 @UserMessage 和用户消息提供程序时，@UserMessage 优先。
     *
     * @param userMessage The user message to be used.
     * @return builder
     */
    public AiServices<T> userMessage(String userMessage) {
        return userMessageProvider(ignore -> userMessage);
    }

    /**
     * Configures the user message provider, which provides a user message to be used each time an AI service is invoked.
     * 配置用户消息提供程序，该提供程序提供每次调用 AI 服务时要使用的用户消息。
     * <br>
     * When both {@code @UserMessage} and the user message provider are configured,
     * {@code @UserMessage} takes precedence.
     *
     * @param userMessageProvider A {@link Function} that accepts a chat memory ID
     *                              (a value of a method parameter annotated with @{@link MemoryId})
     *                              and returns a user message to be used.
     *                              If there is no parameter annotated with {@code @MemoryId},
     *                              the value of memory ID is "default".
     *                              The returned {@link String} can be either a complete user message
     *                              or a user message template containing unresolved template variables (e.g. "{{name}}"),
     *                              which will be resolved using the values of method parameters annotated with @{@link V}.
     * @return builder
     */
    public AiServices<T> userMessageProvider(Function<Object, String> userMessageProvider) {
        context.userMessageProvider = userMessageProvider.andThen(Optional::ofNullable);
        return this;
    }

    /**
     * Configures the chat memory that will be used to preserve conversation history between method calls.
     * 配置用于在方法调用之间保存对话历史记录的聊天内存。
     * <p>
     * Unless a {@link ChatMemory} or {@link ChatMemoryProvider} is configured, all method calls will be independent of each other.
     * In other words, the LLM will not remember the conversation from the previous method calls.
     * <p>
     * The same {@link ChatMemory} instance will be used for every method call.
     * <p>
     * If you want to have a separate {@link ChatMemory} for each user/conversation, configure {@link #chatMemoryProvider} instead.
     * 如果您希望为每个用户/对话设置单独的 {@link ChatMemory}，请配置 {@link #chatMemoryProvider}。
     *  <p>
     * Either a {@link ChatMemory} or a {@link ChatMemoryProvider} can be configured, but not both simultaneously.
     * 可以配置 {@link ChatMemory} 或 {@link ChatMemoryProvider}，但不能同时配置两者。
     *
     * @param chatMemory An instance of chat memory to be used by the AI Service.
     * @return builder
     */
    public AiServices<T> chatMemory(ChatMemory chatMemory) {
        if (chatMemory != null) {
            context.initChatMemories(chatMemory);
        }
        return this;
    }

    /**
     * Configures the chat memory provider, which provides a dedicated instance of {@link ChatMemory} for each user/conversation.
     * To distinguish between users/conversations, one of the method's arguments should be a memory ID (of any data type)
     * annotated with {@link MemoryId}.
     * For each new (previously unseen) memoryId, an instance of {@link ChatMemory} will be automatically obtained
     * by invoking {@link ChatMemoryProvider#get(Object id)}.
     * Example:
     * <pre>
     * interface Assistant {
     *
     *     String chat(@MemoryId int memoryId, @UserMessage String message);
     * }
     * </pre>
     * If you prefer to use the same (shared) {@link ChatMemory} for all users/conversations, configure a {@link #chatMemory} instead.
     * 如果您希望为所有用户/对话使用相同的（共享的）{@link ChatMemory}，请配置一个{@link #chatMemory}。
     * <p>
     * Either a {@link ChatMemory} or a {@link ChatMemoryProvider} can be configured, but not both simultaneously.
     * 可以配置 {@link ChatMemory} 或 {@link ChatMemoryProvider}，但不能同时配置两者。
     *
     * @param chatMemoryProvider The provider of a {@link ChatMemory} for each new user/conversation.
     * @return builder
     */
    public AiServices<T> chatMemoryProvider(ChatMemoryProvider chatMemoryProvider) {
        if (chatMemoryProvider != null) {
            context.initChatMemories(chatMemoryProvider);
        }
        return this;
    }

    /**
     * Configures a transformer that will be applied to the {@link ChatRequest} before it is sent to the LLM.
     * <p>
     * This can be used to modify the request, e.g., by adding additional messages or modifying existing ones.
     *
     * @param chatRequestTransformer A {@link UnaryOperator} that transforms the {@link ChatRequest}.
     * @return builder
     */
    public AiServices<T> chatRequestTransformer(UnaryOperator<ChatRequest> chatRequestTransformer) {
        context.chatRequestTransformer = (req, memId) -> chatRequestTransformer.apply(req);
        return this;
    }

    /**
     * Configures a transformer that will be applied to the {@link ChatRequest} before it is sent to the LLM.
     * <p>
     * This can be used to modify the request, e.g., by adding additional messages or modifying existing ones.
     * <p>
     * The transformer receives the {@link ChatRequest} and the memory ID (the value of a method parameter annotated with @{@link MemoryId}),
     * which can be used to retrieve additional information from the chat memory.
     *
     * @param chatRequestTransformer A {@link BiFunction} that transforms the {@link ChatRequest} and memory ID.
     * @return builder
     */
    public AiServices<T> chatRequestTransformer(BiFunction<ChatRequest, Object, ChatRequest> chatRequestTransformer) {
        context.chatRequestTransformer = chatRequestTransformer;
        return this;
    }

    /**
     * Configures a moderation model to be used for automatic content moderation.
     * If a method in the AI Service is annotated with {@link Moderate}, the moderation model will be invoked
     * to check the user content for any inappropriate or harmful material.
     * 配置用于自动内容审核的审核模型。
     * 如果 AI 服务中的方法被标注为“审核”，则会调用该审核模型来检查用户内容中是否存在任何不当或有害内容。
     *
     * @param moderationModel The moderation model to be used for content moderation.
     * @return builder
     * @see Moderate
     */
    public AiServices<T> moderationModel(ModerationModel moderationModel) {
        context.moderationModel = moderationModel;
        return this;
    }

    /**
     * Configures the tools that the LLM can use.
     *
     * @param objectsWithTools One or more objects whose methods are annotated with {@link Tool}.
     *                         All these tools (methods annotated with {@link Tool}) will be accessible to the LLM.
     *                         Note that inherited methods are ignored.
     * @return builder
     * @see Tool
     */
    public AiServices<T> tools(Object... objectsWithTools) {
        return tools(asList(objectsWithTools));
    }

    /**
     * Configures the tools that the LLM can use.
     *
     * @param objectsWithTools A list of objects whose methods are annotated with {@link Tool}.
     *                         All these tools (methods annotated with {@link Tool}) are accessible to the LLM.
     *                         Note that inherited methods are ignored.
     * @return builder
     * @see Tool
     */
    public AiServices<T> tools(Collection<Object> objectsWithTools) {
        context.toolService.tools(objectsWithTools);
        return this;
    }

    /**
     * Configures a tool provider that dynamically supplies tools for each LLM request.
     * <p>
     * Unlike {@link #tools(Object...)}, which registers a fixed set of tools upfront,
     * a {@link ToolProvider} is invoked on every AI service call and can return a
     * different set of tools based on the current request context (e.g. the user message,
     * memory ID, or invocation parameters).
     *
     * @param toolProvider the tool provider to use
     * @return this builder
     * @see #toolProviders(Collection)
     * @see #toolProviders(ToolProvider...)
     * @see ToolProvider
     */
    public AiServices<T> toolProvider(ToolProvider toolProvider) {
        context.toolService.toolProvider(toolProvider);
        return this;
    }

    /**
     * Configures multiple tool providers that dynamically supply tools for each LLM request.
     * <p>
     * All registered providers are invoked on every AI service call. Tools returned by each
     * provider are merged and included in the request to the LLM. In case of a conflict
     * (e.g. duplicate tool names), an exception will be thrown and AI Service invocation will fail.
     *
     * @param toolProviders the tool providers to use
     * @return this builder
     * @see #toolProvider(ToolProvider)
     * @see #toolProviders(ToolProvider...)
     * @see ToolProvider
     */
    public AiServices<T> toolProviders(Collection<ToolProvider> toolProviders) {
        context.toolService.toolProviders(toolProviders);
        return this;
    }

    /**
     * Configures multiple tool providers that dynamically supply tools for each LLM request.
     * <p>
     * All registered providers are invoked on every AI service call. Tools returned by each
     * provider are merged and included in the request to the LLM. In case of a conflict
     * (e.g. duplicate tool names), an exception will be thrown and AI Service invocation will fail.
     *
     * @param toolProviders the tool providers to use
     * @return this builder
     * @see #toolProvider(ToolProvider)
     * @see #toolProviders(Collection)
     * @see ToolProvider
     */
    public AiServices<T> toolProviders(ToolProvider... toolProviders) {
        if (toolProviders != null && toolProviders.length > 0) {
            context.toolService.toolProviders(asList(toolProviders));
        }
        return this;
    }

    /**
     * Configures the tools that the LLM can use.
     * <p>
     * Each {@link AiServiceTool} carries its own {@link ToolSpecification}, {@link ToolExecutor},
     * and {@link ReturnBehavior}.
     *
     * @param tools list of {@link AiServiceTool}s to expose to the LLM.
     * @return builder
     * @since 1.14.0
     */
    public AiServices<T> tools(List<AiServiceTool> tools) {
        context.toolService.tools(tools);
        return this;
    }

    /**
     * Configures the tools that the LLM can use.
     *
     * @param tools A map of {@link ToolSpecification} to {@link ToolExecutor} entries.
     *              This method of configuring tools is useful when tools must be configured programmatically.
     *              Otherwise, it is recommended to use the {@link Tool}-annotated java methods
     *              and configure tools with the {@link #tools(Object...)} and {@link #tools(Collection)} methods.
     * @return builder
     */
    public AiServices<T> tools(Map<ToolSpecification, ToolExecutor> tools) {
        context.toolService.tools(tools);
        return this;
    }

    /**
     * Configures the tools that the LLM can use.
     *
     * @param tools A map of {@link ToolSpecification} to {@link ToolExecutor} entries.
     * @param immediateReturnToolNames A set of Tool names {@link ToolSpecification#name()}
     *               This method of configuring tools is useful when tools must be configured programmatically.
     *               Otherwise, it is recommended to use the {@link Tool}-annotated java methods
     *               and configure tools with the {@link #tools(Object...)} and {@link #tools(Collection)} methods.
     *               Specifically, this method allows you to specify a set of tool that should not automatically
     *               perform a llm call with the tool results provided by a {@link ToolExecutor}.
     *               This is similar to using the {@link ReturnBehavior#IMMEDIATE} when using the {@link Tool}-annotated java methods
     * @return builder
     * @deprecated use {@link #tools(List)} instead in order to specify {@link ReturnBehavior}
     */
    @Deprecated(since = "1.14.0")
    public AiServices<T> tools(Map<ToolSpecification, ToolExecutor> tools, Set<String> immediateReturnToolNames) {
        context.toolService.tools(tools, immediateReturnToolNames);
        return this;
    }

    /**
     * By default, when the LLM calls multiple tools, the AI Service executes them sequentially.
     * If you enable this option, tools will be executed concurrently (with one exception - see below),
     * using the default {@link Executor}.
     * You can also specify your own {@link Executor}, see {@link #executeToolsConcurrently(Executor)}.
     * <ul>
     *     <li>When using {@link ChatModel}:
     *         <ul>
     *             <li>When the LLM calls multiple tools, they are executed concurrently in separate threads
     *                 using the {@link Executor}.</li>
     *             <li>When the LLM calls a single tool, it is executed in the same (caller) thread,
     *                 the {@link Executor} is not used to avoid wasting resources.</li>
     *         </ul>
     *     </li>
     *     <li>When using {@link StreamingChatModel}:
     *         <ul>
     *             <li>When the LLM calls multiple tools, they are executed concurrently in separate threads
     *                 using the {@link Executor}.
     *                 Each tool is executed as soon as {@link StreamingChatResponseHandler#onCompleteToolCall(CompleteToolCall)}
     *                 is called, without waiting for other tools or for the response streaming to complete.</li>
     *             <li>When the LLM calls a single tool, it is executed in a separate thread using the {@link Executor}.
     *                 We cannot execute it in the same thread because, at that point,
     *                 we do not yet know how many tools the LLM will call.</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * @return builder
     * @see #executeToolsConcurrently(Executor)
     * @since 1.4.0
     */
    public AiServices<T> executeToolsConcurrently() {
        context.toolService.executeToolsConcurrently();
        return this;
    }

    /**
     * See {@link #executeToolsConcurrently()}'s Javadoc for more info.
     * <p>
     * If {@code null} is specified, the default {@link Executor} will be used.
     *
     * @param executor The {@link Executor} to be used to execute tools.
     * @return builder
     * @see #executeToolsConcurrently()
     * @since 1.4.0
     */
    public AiServices<T> executeToolsConcurrently(Executor executor) {
        context.toolService.executeToolsConcurrently(executor);
        return this;
    }

    /**
     * Sets the maximum number of times the LLM may respond with tool calls.
     * 设置允许LLM返回tool调用的最大次数。
     * If this limit is exceeded, an exception is thrown and the AI service invocation is terminated.
     * 如果超过此限制，则会抛出异常并终止 AI 服务调用。
     *
     * <p>
     * NOTE: This value does not represent the total number of tool calls.
     * 注意：此值并不代表工具调用的总次数。
     * Each LLM response that contains one or more tool calls counts as a single invocation
     * and reduces this limit by one.
     * 每个包含一个或多个工具调用的 LLM 响应都算作一次调用，并将此限制减少 1。
     *
     * <p>
     * The default value is 100.
     *
     * @param maxSequentialToolsInvocations the maximum number of LLM responses containing tool calls
     * @return the builder instance
     */
    public AiServices<T> maxSequentialToolsInvocations(int maxSequentialToolsInvocations) {
        context.toolService.maxSequentialToolsInvocations(maxSequentialToolsInvocations);
        return this;
    }

    /**
     * Configures a callback to be invoked before each tool execution.
     * 配置在每个工具执行之前调用的回调。
     *
     * @param beforeToolExecution A {@link Consumer} that accepts a {@link BeforeToolExecution}
     *                            representing the tool execution request about to be executed.
     * @return builder
     * @since 1.11.0
     */
    public AiServices<T> beforeToolExecution(Consumer<BeforeToolExecution> beforeToolExecution) {
        context.toolService.beforeToolExecution(beforeToolExecution);
        return this;
    }

    /**
     * Configures a callback to be invoked after each tool execution.
     * 配置在每个工具执行之后调起的回调。
     *
     * @param afterToolExecution A {@link Consumer} that accepts a {@link ToolExecution}
     *                           containing the tool execution request that was executed and its result.
     * @return builder
     * @since 1.11.0
     */
    public AiServices<T> afterToolExecution(Consumer<ToolExecution> afterToolExecution) {
        context.toolService.afterToolExecution(afterToolExecution);
        return this;
    }

    /**
     * Configures the strategy to be used when the LLM hallucinates a tool name (i.e., attempts to call a nonexistent tool).
     * 配置 LLM 假想工具名称（即尝试调用不存在的工具）时使用的策略。
     *
     * @param hallucinatedToolNameStrategy A Function from {@link ToolExecutionRequest} to {@link ToolExecutionResultMessage} defining
     *                                     the response provided to the LLM when it hallucinates a tool name.
     * @return builder
     * @see #toolArgumentsErrorHandler(ToolArgumentsErrorHandler)
     * @see #toolExecutionErrorHandler(ToolExecutionErrorHandler)
     */
    public AiServices<T> hallucinatedToolNameStrategy(
            Function<ToolExecutionRequest, ToolExecutionResultMessage> hallucinatedToolNameStrategy) {
        context.toolService.hallucinatedToolNameStrategy(hallucinatedToolNameStrategy);
        return this;
    }

    /**
     * Configures the handler to be invoked when errors related to tool arguments occur,
     * such as JSON parsing failures or mismatched argument types.
     * 配置当出现与工具参数相关的错误时要调用的处理程序，例如 JSON 解析失败或参数类型不匹配。
     *
     * <p>
     * Within this handler, you can either:
     * 在此处理程序中，您可以执行以下操作之一：
     * <p>
     * 1. Throw an exception: this will stop the AI Service flow. This is the default behavior if no handler is configured.
     * 1. 抛出异常：这将停止 AI 服务流程。如果未配置任何处理程序，则这是默认行为。
     * <p>
     * 2. Return a text message (e.g., an error description) that will be sent back to the LLM,
     * allowing it to respond appropriately (for example, by correcting the error and retrying).
     * 2. 返回文本消息（例如错误描述），该消息将返回给 LLM，允许 LLM 响应 appropriately（例如，通过纠正错误并重新尝试）。
     * <p>
     * NOTE: If you create a {@link DefaultToolExecutor} manually or use a custom {@link ToolExecutor},
     * ensure that a {@link ToolArgumentsException} is thrown by {@link ToolExecutor} in such cases.
     * For {@link DefaultToolExecutor}, you can enable this by setting
     * {@link DefaultToolExecutor.Builder#wrapToolArgumentsExceptions(Boolean)} to {@code true}.
     *
     * @param handler The handler responsible for processing tool argument errors
     * @return builder
     * @see #hallucinatedToolNameStrategy(Function)
     * @see #toolExecutionErrorHandler(ToolExecutionErrorHandler)
     */
    public AiServices<T> toolArgumentsErrorHandler(ToolArgumentsErrorHandler handler) {
        context.toolService.argumentsErrorHandler(handler);
        return this;
    }

    /**
     * Configures the handler to be invoked when errors occur during tool execution.
     * <p>
     * Within this handler, you can either:
     * <p>
     * 1. Throw an exception: this will stop the AI Service flow.
     * <p>
     * 2. Return a text message (e.g., an error description) that will be sent back to the LLM,
     * allowing it to respond appropriately (for example, by correcting the error and retrying).
     * This is the default behavior if no handler is configured.
     * The {@link Throwable#getMessage()} is sent to the LLM by default.
     * <p>
     * NOTE: If you create a {@link DefaultToolExecutor} manually or use a custom {@link ToolExecutor},
     * ensure that a {@link ToolExecutionException} is thrown by {@link ToolExecutor} in such cases.
     * For {@link DefaultToolExecutor}, you can enable this by setting
     * {@link DefaultToolExecutor.Builder#propagateToolExecutionExceptions(Boolean)} to {@code true}.
     *
     * @param handler The handler responsible for processing tool execution errors
     * @return builder
     * @see #hallucinatedToolNameStrategy(Function)
     * @see #toolArgumentsErrorHandler(ToolArgumentsErrorHandler)
     */
    public AiServices<T> toolExecutionErrorHandler(ToolExecutionErrorHandler handler) {
        context.toolService.executionErrorHandler(handler);
        return this;
    }

    /**
     * Configures a tool search strategy that can be used to reduce token usage.
     * 配置一个工具搜索策略，该策略可用于减少令牌使用量。
     * <p>
     * When configured, the LLM initially "sees" only a single special tool, which it can call
     * to discover additional tools. Once tools are found, they are included in subsequent
     * requests to the LLM.
     * <p>
     * Previously found tools are accumulated until the {@link ToolExecutionResultMessage}
     * containing the tool search results is evicted from the {@link ChatMemory}.
     * <p>
     * You can use one of the out-of-the-box implementations, such as
     * {@link dev.langchain4j.service.tool.search.simple.SimpleToolSearchStrategy}
     * or {@link dev.langchain4j.service.tool.search.vector.VectorToolSearchStrategy}, or implement your own.
     *
     * @param toolSearchStrategy the tool search strategy to use
     * @return builder
     * @since 1.12.0
     */
    public AiServices<T> toolSearchStrategy(ToolSearchStrategy toolSearchStrategy) {
        context.toolService.toolSearchStrategy(toolSearchStrategy);
        return this;
    }

    /**
     * Configures a content retriever to be invoked on every method call for retrieving relevant content
     * related to the user's message from an underlying data source
     * (e.g., an embedding store in the case of an {@link EmbeddingStoreContentRetriever}).
     * 配置内容检索器，使其在每次方法调用时都从底层数据源（例如，对于 EmbeddingStoreContentRetriever 而言，
     * 则是从嵌入存储）检索与用户消息相关的内容。检索到的相关内容随后会自动合并到发送给 LLM 的消息中。
     * The retrieved relevant content is then automatically incorporated into the message sent to the LLM.
     * 然后，检索到的相关内容会自动合并到发送给 LLM 的消息中。
     * <br>
     * This method provides a straightforward approach for those who do not require
     * a customized {@link RetrievalAugmentor}.
     * It configures a {@link DefaultRetrievalAugmentor} with the provided {@link ContentRetriever}.
     *
     * @param contentRetriever The content retriever to be used by the AI Service.
     * @return builder
     */
    public AiServices<T> contentRetriever(ContentRetriever contentRetriever) {
        if (retrievalAugmentorSet) {
            throw illegalConfiguration("Only one out of [retriever, contentRetriever, retrievalAugmentor] can be set");
        }
        contentRetrieverSet = true;
        context.retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .contentRetriever(ensureNotNull(contentRetriever, "contentRetriever"))
                .build();
        return this;
    }

    /**
     * Configures a retrieval augmentor to be invoked on every method call.
     *
     * @param retrievalAugmentor The retrieval augmentor to be used by the AI Service.
     * @return builder
     */
    public AiServices<T> retrievalAugmentor(RetrievalAugmentor retrievalAugmentor) {
        if (contentRetrieverSet) {
            throw illegalConfiguration("Only one out of [retriever, contentRetriever, retrievalAugmentor] can be set");
        }
        retrievalAugmentorSet = true;
        context.retrievalAugmentor = ensureNotNull(retrievalAugmentor, "retrievalAugmentor");
        return this;
    }

    /**
     * Registers an {@link AiServiceListener} listener for AI service events for this AI Service.
     *
     * @param listener the listener to be registered, must not be {@code null}
     * @return builder
     */
    public <I extends AiServiceEvent> AiServices<T> registerListener(AiServiceListener<I> listener) {
        context.eventListenerRegistrar.register(ensureNotNull(listener, "listener"));
        return this;
    }

    /**
     * Registers one or more invocation event listeners to the AI service.
     * This enables tracking and handling of invocation events through the provided listeners.
     *
     * @param listeners the invocation event listeners to be registered; can be null or empty
     * @return builder
     */
    public AiServices<T> registerListeners(AiServiceListener<?>... listeners) {
        context.eventListenerRegistrar.register(listeners);
        return this;
    }

    /**
     * Registers one or more invocation event listeners to the AI service.
     * This enables tracking and handling of invocation events through the provided listeners.
     *
     * @param listeners the invocation event listeners to be registered; can be null or empty
     * @return builder
     */
    public AiServices<T> registerListeners(Collection<? extends AiServiceListener<?>> listeners) {
        context.eventListenerRegistrar.register(listeners);
        return this;
    }

    /**
     * Unregisters an {@link AiServiceListener} listener for AI service events for this AI Service.
     *
     * @param listener the listener to be registered, must not be {@code null}
     * @return builder
     */
    public <I extends AiServiceEvent> AiServices<T> unregisterListener(AiServiceListener<I> listener) {
        context.eventListenerRegistrar.unregister(ensureNotNull(listener, "listener"));
        return this;
    }

    /**
     * Unregisters one or more invocation event listeners from the AI service.
     *
     * @param listeners the invocation event listeners to be unregistered.
     *                  Can be null, in which case no action will be performed.
     * @return builder
     */
    public AiServices<T> unregisterListeners(AiServiceListener<?>... listeners) {
        context.eventListenerRegistrar.unregister(listeners);
        return this;
    }

    /**
     * Unregisters one or more invocation event listeners to the AI service.
     * This enables tracking and handling of invocation events through the provided listeners.
     *
     * @param listeners the invocation event listeners to be unregistered; can be null or empty
     * @return builder
     */
    public AiServices<T> unregisterListeners(Collection<? extends AiServiceListener<?>> listeners) {
        context.eventListenerRegistrar.unregister(listeners);
        return this;
    }

    /**
     * Configures the input guardrails for the AI service context by setting the provided InputGuardrailsConfig.
     * 通过设置提供的 InputGuardrailsConfig，配置 AI 服务上下文的输入保护机制。
     *
     * @param inputGuardrailsConfig the configuration object that defines input guardrails for the AI service
     * @return the current instance of {@link AiServices} to allow method chaining
     */
    public AiServices<T> inputGuardrailsConfig(InputGuardrailsConfig inputGuardrailsConfig) {
        context.guardrailServiceBuilder.inputGuardrailsConfig(inputGuardrailsConfig);
        return this;
    }

    /**
     * Configures the output guardrails for AI services.
     * 配置 AI 服务的输出防护措施。
     *
     * @param outputGuardrailsConfig the configuration object specifying the output guardrails
     * @return the current instance of {@link AiServices} to allow for method chaining
     */
    public AiServices<T> outputGuardrailsConfig(OutputGuardrailsConfig outputGuardrailsConfig) {
        context.guardrailServiceBuilder.outputGuardrailsConfig(outputGuardrailsConfig);
        return this;
    }

    /**
     * Configures the input guardrail classes for the AI services.
     * 为AI services配置输入防护措施。
     * <p>
     *     Configuring this way is exactly the same as using the {@link dev.langchain4j.service.guardrail.InputGuardrails InptputGuardrails}
     *     annotation at the class level. Using the annotation takes precedence.
     *     这种配置方式与在类级别使用 `{@link dev.langchain4j.service.guardrail.InputGuardrails InputGuardrails}` 注解完全相同。
     * 注解的使用优先级更高。
     * </p>
     * <p>
     *     An input guardrail is a rule that is applied to the input of the model (essentially the user message) to ensure
     *     that the input is safe and meets the expectations of the model. It does not replace a moderation model, but it can
     *     be used to add additional checks (i.e. prompt injection, etc).
     *     输入防护规则是应用于模型输入（本质上是用户消息）的一条规则，旨在确保输入安全并符合模型预期。它不能取代审核模型，但可以用来添加额外的检查（例如提示注入等）。
     * </p>
     * <p>
     *     Unlike for output guardrails, the input guardrails do not support retry or reprompt. The failure is passed directly
     *     to the caller, wrapped into a {@link dev.langchain4j.guardrail.GuardrailException GuardrailException}.
     *     与输出防护机制不同，输入防护机制不支持重试或重新提示。失败会直接传递给调用者，并封装成 {@link dev.langchain4j.guardrail.GuardrailException GuardrailException}。
     * </p>
     * <p>
     *     When several guardrails are applied, the order of the guardrails is important, as the guardrails are applied in the order
     *     they are listed.
     *     当安装多个护栏时，护栏的安装顺序很重要，因为护栏是按照清单上的顺序安装的。
     * </p>
     *
     * @param guardrailClasses A list of {@link InputGuardrailsConfig} classes, which will be used for input validation.
     *                         The list can be {@code null} if no guardrails are to be applied.
     * @param <I> The type of {@link InputGuardrail}
     * @return The instance of {@link AiServices} to allow method chaining.
     */
    public <I extends InputGuardrail> AiServices<T> inputGuardrailClasses(List<Class<? extends I>> guardrailClasses) {
        context.guardrailServiceBuilder.inputGuardrailClasses(guardrailClasses);
        return this;
    }

    /**
     * Configures input guardrail classes for the AI service.
     * <p>
     *     Configuring this way is exactly the same as using the {@link dev.langchain4j.service.guardrail.InputGuardrails InptputGuardrails}
     *     annotation at the class level. Using the annotation takes precedence.
     * </p>
     * <p>
     *     An input guardrail is a rule that is applied to the input of the model (essentially the user message) to ensure
     *     that the input is safe and meets the expectations of the model. It does not replace a moderation model, but it can
     *     be used to add additional checks (i.e. prompt injection, etc).
     * </p>
     * <p>
     *     Unlike for output guardrails, the input guardrails do not support retry or reprompt. The failure is passed directly
     *     to the caller, wrapped into a {@link dev.langchain4j.guardrail.GuardrailException GuardrailException}.
     * </p>
     * <p>
     *     When several guardrails are applied, the order of the guardrails is important, as the guardrails are applied in the order
     *     they are listed.
     * </p>
     *
     * @param guardrailClasses A list of {@link InputGuardrail} classes, which
     *                         can include {@code null} to indicate no guardrails or optional configurations.
     * @param <I> The type of {@link InputGuardrail}
     * @return the current instance of {@link AiServices} for chaining further configurations.
     */
    public <I extends InputGuardrail> AiServices<T> inputGuardrailClasses(Class<? extends I>... guardrailClasses) {
        context.guardrailServiceBuilder.inputGuardrailClasses(guardrailClasses);
        return this;
    }

    /**
     * Sets the input guardrails to be used by the guardrail service builder in the current context.
     * <p>
     *     Configuring this way is exactly the same as using the {@link dev.langchain4j.service.guardrail.InputGuardrails InptputGuardrails}
     *     annotation at the class level. Using the annotation takes precedence.
     * </p>
     * <p>
     *     An input guardrail is a rule that is applied to the input of the model (essentially the user message) to ensure
     *     that the input is safe and meets the expectations of the model. It does not replace a moderation model, but it can
     *     be used to add additional checks (i.e. prompt injection, etc).
     * </p>
     * <p>
     *     Unlike for output guardrails, the input guardrails do not support retry or reprompt. The failure is passed directly
     *     to the caller, wrapped into a {@link dev.langchain4j.guardrail.GuardrailException GuardrailException}.
     * </p>
     * <p>
     *     When several guardrails are applied, the order of the guardrails is important, as the guardrails are applied in the order
     *     they are listed.
     * </p>
     *
     * @param guardrails a list of input guardrails, or null if no guardrails are to be set
     * @return the current instance of {@link AiServices} for method chaining
     */
    public <I extends InputGuardrail> AiServices<T> inputGuardrails(List<I> guardrails) {
        context.guardrailServiceBuilder.inputGuardrails(guardrails);
        return this;
    }

    /**
     * Adds the specified input guardrails to the context's guardrail service builder.
     * <p>
     *     Configuring this way is exactly the same as using the {@link dev.langchain4j.service.guardrail.InputGuardrails InptputGuardrails}
     *     annotation at the class level. Using the annotation takes precedence.
     * </p>
     * <p>
     *     An input guardrail is a rule that is applied to the input of the model (essentially the user message) to ensure
     *     that the input is safe and meets the expectations of the model. It does not replace a moderation model, but it can
     *     be used to add additional checks (i.e. prompt injection, etc).
     * </p>
     * <p>
     *     Unlike for output guardrails, the input guardrails do not support retry or reprompt. The failure is passed directly
     *     to the caller, wrapped into a {@link dev.langchain4j.guardrail.GuardrailException GuardrailException}.
     * </p>
     * <p>
     *     When several guardrails are applied, the order of the guardrails is important, as the guardrails are applied in the order
     *     they are listed.
     * </p>
     *
     * @param guardrails an array of input guardrails to set, may be null
     * @return the current instance of {@link AiServices} for chaining
     */
    public <I extends InputGuardrail> AiServices<T> inputGuardrails(I... guardrails) {
        context.guardrailServiceBuilder.inputGuardrails(guardrails);
        return this;
    }

    /**
     * Configures the output guardrail classes for the AI services.
     * 为AI services配置输出守护者类
     * <p>
     *     Configuring this way is exactly the same as using the {@link dev.langchain4j.service.guardrail.OutputGuardrails OutputGuardrails}
     *     annotation at the class level. Using the annotation takes precedence.
     *     这种配置方式与在类级别使用 `{@link dev.langchain4j.service.guardrail.OutputGuardrails OutputGuardrails}` 注解完全相同。
     * 注解的使用优先级更高。
     * </p>
     * <p>
     *     Am output guardrail is a rule that is applied to the output of the model to ensure that the output is safe and meets
     *     certain expectations.
     *     输出护栏是应用于模型输出的规则，以确保输出安全并符合某些预期。
     * </p>
     * <p>
     *     When a validation fails, the result can indicate whether the request should be retried as-is, or to provide a
     *     {@code reprompt} message to append to the prompt.
     *     当验证失败时，结果可以指示是否应按原样重试请求，或者提供 {@code reprompt} 消息以附加到提示符。
     * </p>
     * <p>
     *     In the case of re-prompting, the reprompt message is added to the LLM context and the request is then retried.
     *     如果需要重新提示，则将重新提示消息添加到 LLM 上下文中，然后重试请求。
     * </p>
     * <p>
     *     When several guardrails are applied, the order of the guardrails is important, as the guardrails are applied in
     *     the order they are listed.
     *     当安装多个护栏时，护栏的安装顺序很重要，因为护栏是按照清单上的顺序安装的。
     * </p>
     * <p>
     *     When several {@link OutputGuardrail}s are applied, if any guardrail forces a retry or reprompt, then all of the
     *     guardrails will be re-applied to the new response.
     *     当应用多个 {@link OutputGuardrail} 时，如果任何护栏强制重试或重新提示，则所有护栏都将重新应用于新的响应。
     * </p>
     *
     * @param guardrailClasses a list of {@link OutputGuardrail} classes. These classes
     *                         define the output guardrails to be applied. Can be null.
     * @param <O> The type of {@link OutputGuardrail}
     * @return the current instance of {@link AiServices}.
     */
    public <O extends OutputGuardrail> AiServices<T> outputGuardrailClasses(List<Class<? extends O>> guardrailClasses) {
        context.guardrailServiceBuilder.outputGuardrailClasses(guardrailClasses);
        return this;
    }

    /**
     * Sets the output guardrail classes to be used in the guardrail service.
     * <p>
     *     Configuring this way is exactly the same as using the {@link dev.langchain4j.service.guardrail.OutputGuardrails OutputGuardrails}
     *     annotation at the class level. Using the annotation takes precedence.
     * </p>
     * <p>
     *     Am output guardrail is a rule that is applied to the output of the model to ensure that the output is safe and meets
     *     certain expectations.
     * </p>
     * <p>
     *     When a validation fails, the result can indicate whether the request should be retried as-is, or to provide a
     *     {@code reprompt} message to append to the prompt.
     * </p>
     * <p>
     *     In the case of re-prompting, the reprompt message is added to the LLM context and the request is then retried.
     * </p>
     * <p>
     *     When several guardrails are applied, the order of the guardrails is important, as the guardrails are applied in
     *     the order they are listed.
     * </p>
     * <p>
     *     When several {@link OutputGuardrail}s are applied, if any guardrail forces a retry or reprompt, then all of the
     *     guardrails will be re-applied to the new response.
     * </p>
     *
     * @param guardrailClasses A list of {@link OutputGuardrail} classes.
     *                         These classes define the guardrails for output behavior.
     *                         Nullable, meaning guardrails can be omitted.
     * @param <O> The type of {@link OutputGuardrail}
     * @return The current instance of {@link AiServices}, enabling method chaining.
     */
    public <O extends OutputGuardrail> AiServices<T> outputGuardrailClasses(Class<? extends O>... guardrailClasses) {
        context.guardrailServiceBuilder.outputGuardrailClasses(guardrailClasses);
        return this;
    }

    /**
     * Configures the output guardrails for the AI service.
     * <p>
     *     Configuring this way is exactly the same as using the {@link dev.langchain4j.service.guardrail.OutputGuardrails OutputGuardrails}
     *     annotation at the class level. Using the annotation takes precedence.
     * </p>
     * <p>
     *     Am output guardrail is a rule that is applied to the output of the model to ensure that the output is safe and meets
     *     certain expectations.
     * </p>
     * <p>
     *     When a validation fails, the result can indicate whether the request should be retried as-is, or to provide a
     *     {@code reprompt} message to append to the prompt.
     * </p>
     * <p>
     *     In the case of re-prompting, the reprompt message is added to the LLM context and the request is then retried.
     * </p>
     * <p>
     *     When several guardrails are applied, the order of the guardrails is important, as the guardrails are applied in
     *     the order they are listed.
     * </p>
     * <p>
     *     When several {@link OutputGuardrail}s are applied, if any guardrail forces a retry or reprompt, then all of the
     *     guardrails will be re-applied to the new response.
     * </p>
     *
     * @param guardrails a list of output guardrails to be applied; can be {@code null}
     * @return the current instance of {@link AiServices} for method chaining
     */
    public <O extends OutputGuardrail> AiServices<T> outputGuardrails(List<O> guardrails) {
        context.guardrailServiceBuilder.outputGuardrails(guardrails);
        return this;
    }

    /**
     * Configures output guardrails for the AI services.
     * <p>
     *     Configuring this way is exactly the same as using the {@link dev.langchain4j.service.guardrail.OutputGuardrails OutputGuardrails}
     *     annotation at the class level. Using the annotation takes precedence.
     * </p>
     * <p>
     *     Am output guardrail is a rule that is applied to the output of the model to ensure that the output is safe and meets
     *     certain expectations.
     * </p>
     * <p>
     *     When a validation fails, the result can indicate whether the request should be retried as-is, or to provide a
     *     {@code reprompt} message to append to the prompt.
     * </p>
     * <p>
     *     In the case of re-prompting, the reprompt message is added to the LLM context and the request is then retried.
     * </p>
     * <p>
     *     When several guardrails are applied, the order of the guardrails is important, as the guardrails are applied in
     *     the order they are listed.
     * </p>
     * <p>
     *     When several {@link OutputGuardrail}s are applied, if any guardrail forces a retry or reprompt, then all of the
     *     guardrails will be re-applied to the new response.
     * </p>
     *
     * @param guardrails an array of output guardrails to be applied; can be {@code null}
     *                   or contain multiple instances of OutputGuardrail
     * @return the current instance of {@link AiServices} with the specified guardrails applied
     */
    public <O extends OutputGuardrail> AiServices<T> outputGuardrails(O... guardrails) {
        context.guardrailServiceBuilder.outputGuardrails(guardrails);
        return this;
    }

    /**
     * Configures whether user messages that were augmented with retrieved content
     * (RAG) should be stored in {@link ChatMemory}.
     * 配置是否应将使用检索内容 (RAG) 增强的用户消息存储在 {@link ChatMemory} 中。
     * <p>
     * By default, this is {@code true}, meaning that the final augmented user
     * message (after RAG augmentation) is stored in chat memory. This matches
     * the historical behaviour and ensures that the model sees the same
     * augmented content in subsequent turns.
     * 默认情况下，此值为 {@code true}，这意味着最终增强后的用户消息（经过 RAG 增强后）会存储在聊天内存中。
     * 这与历史行为一致，并确保模型在后续回合中看到相同的增强内容。
     * <p>
     * If set to {@code false}, only the original user message (before RAG
     * augmentation) is stored in chat memory, while the augmented message is
     * still used for the LLM request. This helps to avoid storing retrieved
     * content in the conversation history and keeps the memory size smaller.
     * 如果设置为 {@code false}，则聊天内存中仅存储原始用户消息（RAG 增强之前），
     * 而增强后的消息仍用于 LLM 请求。这有助于避免将检索到的内容存储在对话历史记录中，从而减少内存占用。
     *
     * @param storeRetrievedContentInChatMemory whether to store RAG-augmented user messages in chat memory
     *                                          是否将 RAG 增强的用户消息存储在聊天内存中
     * @return builder
     */
    public AiServices<T> storeRetrievedContentInChatMemory(boolean storeRetrievedContentInChatMemory) {
        context.storeRetrievedContentInChatMemory = storeRetrievedContentInChatMemory;
        return this;
    }

    /**
     * Constructs and returns the AI Service.
     *
     * @return An instance of the AI Service implementing the specified interface.
     */
    public abstract T build();

    // 执行基本验证
    protected void performBasicValidation() {
        if (context.chatModel == null && context.streamingChatModel == null) {
            throw illegalConfiguration("Please specify either chatModel or streamingChatModel");
        }
    }

    public static List<ChatMessage> removeToolMessages(List<ChatMessage> messages) {
        return messages.stream()
                .filter(it -> !(it instanceof ToolExecutionResultMessage))
                .filter(it -> !(it instanceof AiMessage && ((AiMessage) it).hasToolExecutionRequests()))
                .collect(toList());
    }

    public static void verifyModerationIfNeeded(Future<Moderation> moderationFuture) {
        if (moderationFuture != null) {
            try {
                Moderation moderation = moderationFuture.get();
                if (moderation.flagged()) {
                    throw new ModerationException(
                            String.format("Text \"%s\" violates content policy", moderation.flaggedText()), moderation);
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
