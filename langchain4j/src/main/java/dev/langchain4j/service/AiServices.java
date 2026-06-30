package dev.langchain4j.service;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;
import static dev.langchain4j.service.IllegalConfigurationException.illegalConfiguration;
import static dev.langchain4j.spi.ServiceHelper.loadFactory;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import dev.langchain4j.Internal;
import dev.langchain4j.agent.tool.ReturnBehavior;
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
import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
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
import dev.langchain4j.service.tool.ToolErrorHandlerResult;
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
 * <p>
 * You can define your own API (a Java interface with one or more methods),
 * and {@code AiServices} will provide an implementation for it, hiding all the complexity from you.
 * <p>
 * You can find more details <a href="https://docs.langchain4j.dev/tutorials/ai-services">here</a>.
 * <p>
 * Please note that AI Service should not be called concurrently for the same @{@link MemoryId},
 * as it can lead to corrupted {@link ChatMemory}. Currently, AI Service does not implement any mechanism
 * to prevent concurrent calls for the same @{@link MemoryId}.
 * <p>
 * Currently, AI Services support:
 * <pre>
 * - Static system message templates, configured via @{@link SystemMessage} annotation on top of the method
 * - Dynamic system message templates, configured via {@link #systemMessageProvider(Function)}
 * - Static user message templates, configured via @{@link UserMessage} annotation on top of the method
 * - Dynamic user message templates, configured via method parameter annotated with @{@link UserMessage}
 * - Single (shared) {@link ChatMemory}, configured via {@link #chatMemory(ChatMemory)}
 * - Separate (per-user) {@code ChatMemory}, configured via {@link #chatMemoryProvider(ChatMemoryProvider)} and a method parameter annotated with @{@link MemoryId}
 * - RAG, configured via {@link #contentRetriever(ContentRetriever)} or {@link #retrievalAugmentor(RetrievalAugmentor)}
 * - Tools, configured via {@link #tools(Collection)}, {@link #tools(Object...)}, {@link #tools(Map)} or {@link #toolProvider(ToolProvider)} and methods annotated with @{@link Tool}
 * - Various method return types (output parsers), see more details below
 * - Streaming (use {@link TokenStream} as a return type)
 * - Structured prompts as method arguments (see @{@link StructuredPrompt})
 * - Auto-moderation, configured via @{@link Moderate} annotation
 * </pre>
 * <p>
 * Here is the simplest example of an AI Service:
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
 * - a {@link String} or an {@link AiMessage}, if you want to get the answer from the LLM as-is
 * - a {@code List<String>} or {@code Set<String>}, if you want to receive the answer as a collection of items or bullet points
 * - any {@link Enum} or a {@code boolean}, if you want to use the LLM for classification
 * - a primitive or boxed Java type: {@code int}, {@code Double}, etc., if you want to use the LLM for data extraction
 * - many default Java types: {@code Date}, {@code LocalDateTime}, {@code BigDecimal}, etc., if you want to use the LLM for data extraction
 * - any custom POJO, if you want to use the LLM for data extraction.
 * - Result&lt;T&gt; if you want to access {@link TokenUsage} or sources ({@link Content}s retrieved during RAG), aside from T, which can be of any type listed above. For example: Result&lt;String&gt;, Result&lt;MyCustomPojo&gt;
 * For POJOs, it is advisable to use the "json mode" feature if the LLM provider supports it. For OpenAI, this can be enabled by calling {@code responseFormat("json_object")} during model construction.
 *
 * </pre>
 * <p>
 * Let's see how we can classify the sentiment of a text:
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
 * In this example, the special {@code {{it}}} prompt template variable is used because there's only one method parameter.
 * However, you can use more parameters as demonstrated in the following example:
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
 */
public abstract class AiServices<T> {

    protected final AiServiceContext context;

    private boolean contentRetrieverSet = false;
    private boolean retrievalAugmentorSet = false;

    protected AiServices(AiServiceContext context) {
        this.context = context;
    }

    /**
     * Creates an AI Service (an implementation of the provided interface), that is backed by the provided chat model.
     * This convenience method can be used to create simple AI Services.
     * For more complex cases, please use {@link #builder}.
     *
     * @param aiService The class of the interface to be implemented.
     * @param chatModel The chat model to be used under the hood.
     * @return An instance of the provided interface, implementing all its defined methods.
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
     *
     * @param aiService The class of the interface to be implemented.
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
     * <p>
     * Either {@link ChatModel} or {@link StreamingChatModel} should be configured,
     * but not both at the same time.
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
     * The methods of the AI Service must return a {@link TokenStream} type.
     * <p>
     * Either {@link ChatModel} or {@link StreamingChatModel} should be configured,
     * but not both at the same time.
     *
     * @param streamingChatModel Streaming chat model that will be used under the hood of the AI Service.
     * @return builder
     */
    public AiServices<T> streamingChatModel(StreamingChatModel streamingChatModel) {
        context.streamingChatModel = streamingChatModel;
        return this;
    }

    /**
     * Configures the system message to be used each time an AI service is invoked.
     * It can be either a complete system message or a system message template containing unresolved template
     * variables (e.g. "{{name}}"), which will be resolved using the values of method parameters annotated with @{@link V}.
     * <br>
     * When both {@code @SystemMessage} and the system message provider are configured,
     * {@code @SystemMessage} takes precedence.
     *
     * @param systemMessage The system message to be used.
     * @return builder
     */
    public AiServices<T> systemMessage(String systemMessage) {
        return systemMessageProvider(ignore -> systemMessage);
    }

    /**
     * Configures the system message provider, which provides a system message to be used each time an AI service is invoked.
     * <br>
     * When both {@code @SystemMessage} and the system message provider are configured,
     * {@code @SystemMessage} takes precedence.
     *
     * @param systemMessageProvider A {@link Function} that accepts a chat memory ID
     *                              (a value of a method parameter annotated with @{@link MemoryId})
     *                              and returns a system message to be used.
     *                              If there is no parameter annotated with {@code @MemoryId},
     *                              the value of memory ID is "default".
     *                              The returned {@link String} can be either a complete system message
     *                              or a system message template containing unresolved template variables (e.g. "{{name}}"),
     *                              which will be resolved using the values of method parameters annotated with @{@link V}.
     * @return builder
     */
    public AiServices<T> systemMessageProvider(Function<Object, String> systemMessageProvider) {
        context.systemMessageProvider = systemMessageProvider.andThen(Optional::ofNullable);
        return this;
    }

    /**
     * Configures a system message provider that provides a system message each time an AI service is invoked.
     * <p>
     * This is similar to {@link #systemMessageProvider(Function)}, but the provider receives the full
     * {@link InvocationContext}, which exposes the {@link ChatRequestParameters} and {@link ModelProvider}
     * of the {@link ChatModel} or {@link StreamingChatModel} configured for this AI service,
     * allowing the system message to vary based on the model used.
     * <p>
     * When both {@code @SystemMessage} and this provider are configured,
     * {@code @SystemMessage} takes precedence.
     *
     * @param systemMessageProvider A {@link Function} that accepts an {@link InvocationContext}
     *                              and returns a system message to be used.
     *                              The returned {@link String} can be either a complete system message
     *                              or a system message template containing unresolved template variables (e.g. "{{name}}"),
     *                              which will be resolved using the values of method parameters annotated with @{@link V}.
     * @return builder
     */
    public AiServices<T> systemMessageProviderWithContext(Function<InvocationContext, String> systemMessageProvider) {
        context.systemMessageProviderWithContext = systemMessageProvider;
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
     * has been configured) and must return the new system message text.
     *
     * @param systemMessageTransformer A {@link UnaryOperator} that accepts the current system message
     *                                 text and returns the transformed text.
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
     * <br>
     * When both {@code @UserMessage} and the user message provider are configured,
     * {@code @UserMessage} takes precedence.
     *
     * @param userMessage The user message to be used.
     * @return builder
     */
    public AiServices<T> userMessage(String userMessage) {
        return userMessageProvider(ignore -> userMessage);
    }

    /**
     * Configures the user message provider, which provides a user message to be used each time an AI service is invoked.
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
     * <p>
     * Unless a {@link ChatMemory} or {@link ChatMemoryProvider} is configured, all method calls will be independent of each other.
     * In other words, the LLM will not remember the conversation from the previous method calls.
     * <p>
     * The same {@link ChatMemory} instance will be used for every method call.
     * <p>
     * If you want to have a separate {@link ChatMemory} for each user/conversation, configure {@link #chatMemoryProvider} instead.
     * <p>
     * Either a {@link ChatMemory} or a {@link ChatMemoryProvider} can be configured, but not both simultaneously.
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
     * <p>
     * Either a {@link ChatMemory} or a {@link ChatMemoryProvider} can be configured, but not both simultaneously.
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
    /**
     * 配置聊天请求转换器，该转换器会在请求发送至大语言模型前对 {@link ChatRequest} 进行处理。
     * <p>
     * 可用于修改请求内容，例如新增消息或调整已有消息。
     *
     * @param chatRequestTransformer 用于转换 {@link ChatRequest} 的一元操作器
     * @return 构造器实例
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
    /**
     * 配置聊天请求转换器，该转换器会在请求发送至大语言模型前对 {@link ChatRequest} 进行处理。
     * <p>
     * 可用于修改请求内容，例如新增消息或调整已有消息。
     * <p>
     * 转换器会接收 {@link ChatRequest} 和内存ID（被 @{@link MemoryId} 注解标记的方法参数值），
     * 可通过内存ID从聊天内存中获取额外信息。
     *
     * @param chatRequestTransformer 用于转换 {@link ChatRequest} 和内存ID的双函数处理器
     * @return 构造器实例
     */
    public AiServices<T> chatRequestTransformer(BiFunction<ChatRequest, Object, ChatRequest> chatRequestTransformer) {
        context.chatRequestTransformer = chatRequestTransformer;
        return this;
    }

    /**
     * Configures a moderation model to be used for automatic content moderation.
     * If a method in the AI Service is annotated with {@link Moderate}, the moderation model will be invoked
     * to check the user content for any inappropriate or harmful material.
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
    /**
     * 默认情况下，当大语言模型（LLM）调用多个工具时，AI 服务会按顺序执行这些工具。
     * 若启用该选项，工具将以并发方式执行（唯一例外情况见下文说明），
     * 执行时将使用默认的 {@link Executor} 线程执行器。
     * 你也可以指定自定义的 {@link Executor}，详情参考 {@link #executeToolsConcurrently(Executor)}。
     * <ul>
     *     <li>使用 {@link ChatModel} 时：
     *         <ul>
     *             <li>当大语言模型调用多个工具时，工具会通过 {@link Executor} 在独立线程中并发执行。</li>
     *             <li>当大语言模型仅调用单个工具时，工具将在当前（调用方）线程中执行，
     *                 不会使用 {@link Executor}，避免资源浪费。</li>
     *         </ul>
     *     </li>
     *     <li>使用 {@link StreamingChatModel} 流式对话模型时：
     *         <ul>
     *             <li>当大语言模型调用多个工具时，工具会通过 {@link Executor} 在独立线程中并发执行。
     *                 每当触发 {@link StreamingChatResponseHandler#onCompleteToolCall(CompleteToolCall)} 回调时，
     *                 对应工具会立即执行，无需等待其他工具执行完毕，也无需等待响应流传输完成。</li>
     *             <li>当大语言模型仅调用单个工具时，工具会通过 {@link Executor} 在独立线程中执行。
     *                 无法在当前线程中执行的原因是：此时无法预知大语言模型最终会调用多少个工具。</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * @return 构建器
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
     * Sets the maximum number of tool calling round trips (i.e. LLM responses containing tool calls).
     * If this limit is exceeded, an exception is thrown and the AI service invocation is terminated.
     *
     * <p>
     * NOTE: This value does not represent the total number of individual tool calls.
     * Each LLM response that contains one or more tool calls counts as a single round trip
     * and reduces this limit by one.
     *
     * <p>
     * The default value is 100.
     *
     * @param maxToolCallingRoundTrips the maximum number of LLM responses containing tool calls
     * @return the builder instance
     */
    public AiServices<T> maxToolCallingRoundTrips(int maxToolCallingRoundTrips) {
        context.toolService.maxToolCallingRoundTrips(maxToolCallingRoundTrips);
        return this;
    }

    /**
     * @deprecated Use {@link #maxToolCallingRoundTrips(int)} instead.
     */
    @Deprecated(since = "1.15.0")
    public AiServices<T> maxSequentialToolsInvocations(int maxSequentialToolsInvocations) {
        return maxToolCallingRoundTrips(maxSequentialToolsInvocations);
    }

    /**
     * Configures a callback to be invoked before each tool execution.
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
     *
     * @param hallucinatedToolNameStrategy A Function from {@link ToolExecutionRequest} to {@link ToolExecutionResultMessage} defining
     *                                     the response provided to the LLM when it hallucinates a tool name.
     * @return builder
     * @see #toolArgumentsErrorHandler(ToolArgumentsErrorHandler)
     * @see #toolExecutionErrorHandler(ToolExecutionErrorHandler)
     */
    /**
     * 配置大语言模型出现工具名称幻觉（即尝试调用不存在的工具）时使用的处理策略。
     *
     * @param hallucinatedToolNameStrategy 一个将 {@link ToolExecutionRequest} 转换为 {@link ToolExecutionResultMessage} 的函数，
     *                                     用于定义模型出现工具名称幻觉时返回给它的响应内容。
     * @return 构造器实例
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
     * such as JSON parsing failures, missing required parameters, or mismatched argument types.
     * <p>
     * Within this handler, you can either:
     * <p>
     * 1. Throw an exception: this will stop the AI Service flow. This is the default behavior if no handler is configured.
     * <p>
     * 2. Return a text message (e.g., an error description) that will be sent back to the LLM,
     * allowing it to respond appropriately (for example, by correcting the error and retrying).
     * <p>
     * <b>Recommendation:</b> the current default (throw) is rarely what you want.
     * Argument errors usually originate from the LLM (malformed JSON, missing fields, wrong types),
     * and LLMs can typically self-correct when given a clear error message. Configuring a handler
     * that returns the error text via {@link ToolErrorHandlerResult#text(String)} lets the LLM retry,
     * which is more in line with how agentic systems are expected to behave.
     * The default will change to "Return a text message" in LangChain4j 2.0.
     * <p>
     * Example:
     * <pre>{@code
     * .toolArgumentsErrorHandler((error, ctx) -> ToolErrorHandlerResult.text(error.getMessage()))
     * }</pre>
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
    /**
     * 配置工具参数相关错误发生时触发的处理器，
     * 错误类型包括JSON解析失败、缺少必填参数、参数类型不匹配等。
     * <p>
     * 在该处理器中可选择以下两种处理方式：
     * <p>
     * 1. 抛出异常：终止AI服务流程。未配置处理器时将默认采用此方式。
     * <p>
     * 2. 返回文本信息（如错误描述）并回传给大语言模型，
     * 使其做出相应处理（例如修正错误后重试）。
     * <p>
     * <b>建议：</b>当前默认的“抛出异常”行为通常不符合实际需求。
     * 参数错误通常由大语言模型导致（JSON格式错误、字段缺失、类型错误），
     * 当收到清晰的错误信息时，模型通常可以自行修正。
     * 配置处理器通过 {@link ToolErrorHandlerResult#text(String)} 返回错误文本，可让模型重试执行，
     * 这更符合智能代理系统的预期行为。
     * LangChain4j 2.0 版本会将默认行为改为“返回文本信息”。
     * <p>
     * 示例：
     * <pre>{@code
     * .toolArgumentsErrorHandler((error, ctx) -> ToolErrorHandlerResult.text(error.getMessage()))
     * }</pre>
     * <p>
     * 注意：若手动创建 {@link DefaultToolExecutor} 或使用自定义 {@link ToolExecutor}，
     * 需确保工具参数出错时 {@link ToolExecutor} 会抛出 {@link ToolArgumentsException}。
     * 对于 {@link DefaultToolExecutor}，可将
     * {@link DefaultToolExecutor.Builder#wrapToolArgumentsExceptions(Boolean)} 设置为 {@code true} 来开启该能力。
     *
     * @param handler 用于处理工具参数错误的处理器
     * @return 构造器实例
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
     * <b>Recommendation:</b> the current default sends the raw exception message to the LLM,
     * which can leak internal application data — stack traces, file paths, downstream API responses,
     * credentials and/or PII embedded in error strings, etc.
     * Once fed to the LLM, this content can flow into responses, chat history, observability pipelines,
     * and the LLM provider's logs. For production use, configure a handler that returns either a
     * generic message or a sanitized/curated description of the failure, and rely on logs/events for
     * the underlying detail.
     * The default will change to "Throw an exception" in LangChain4j 2.0.
     * <p>
     * Example:
     * <pre>{@code
     * .toolExecutionErrorHandler((error, ctx) -> ToolErrorHandlerResult.text("Tool execution failed."))
     * }</pre>
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
    /**
     * 配置工具执行出错时触发的处理器。
     * <p>
     * 在该处理器中可选择以下两种处理方式：
     * <p>
     * 1. 抛出异常：终止AI服务流程。
     * <p>
     * 2. 返回文本信息（如错误描述）并回传给大语言模型，
     * 使其做出相应处理（例如修正错误后重试）。
     * 未配置处理器时将默认采用此方式，默认会将 {@link Throwable#getMessage()} 的内容发送给大语言模型。
     * <p>
     * <b>建议：</b>当前默认会将原始异常信息发送给大语言模型，
     * 可能导致应用内部数据泄露，包括堆栈轨迹、文件路径、下游接口响应、
     * 凭证信息以及错误文本中包含的个人身份信息等。
     * 这些内容一旦传入大语言模型，可能会出现在模型回复、聊天记录、可观测链路以及模型服务商日志中。
     * 生产环境请配置自定义处理器，返回通用提示或经过脱敏、整理的错误描述，
     * 原始详细信息请通过日志或事件进行记录。
     * LangChain4j 2.0 版本会将默认行为改为“抛出异常”。
     * <p>
     * 示例：
     * <pre>{@code
     * .toolExecutionErrorHandler((error, ctx) -> ToolErrorHandlerResult.text("Tool execution failed."))
     * }</pre>
     * <p>
     * 注意：若手动创建 {@link DefaultToolExecutor} 或使用自定义 {@link ToolExecutor}，
     * 需确保工具执行出错时 {@link ToolExecutor} 会抛出 {@link ToolExecutionException}。
     * 对于 {@link DefaultToolExecutor}，可将
     * {@link DefaultToolExecutor.Builder#propagateToolExecutionExceptions(Boolean)} 设置为 {@code true} 来开启该能力。
     *
     * @param handler 用于处理工具执行错误的处理器
     * @return 构造器实例
     * @see #hallucinatedToolNameStrategy(Function)
     * @see #toolArgumentsErrorHandler(ToolArgumentsErrorHandler)
     */
    public AiServices<T> toolExecutionErrorHandler(ToolExecutionErrorHandler handler) {
        context.toolService.executionErrorHandler(handler);
        return this;
    }

    /**
     * Configures a tool search strategy that can be used to reduce token usage.
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
    /**
     * 配置工具检索策略，用于减少令牌消耗。
     * <p>
     * 配置后，大语言模型初始仅能看到一个专用工具，可调用该工具来查找其他工具。
     * 检索到的工具会在后续请求中提供给大语言模型。
     * <p>
     * 已检索到的工具会持续保留，直至包含工具检索结果的 {@link ToolExecutionResultMessage} 从 {@link ChatMemory} 中移除。
     * <p>
     * 可使用内置实现，例如
     * {@link dev.langchain4j.service.tool.search.simple.SimpleToolSearchStrategy}
     * 或 {@link dev.langchain4j.service.tool.search.vector.VectorToolSearchStrategy}，也可自行实现。
     *
     * @param toolSearchStrategy 要使用的工具检索策略
     * @return 构造器实例
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
     * The retrieved relevant content is then automatically incorporated into the message sent to the LLM.
     * <br>
     * This method provides a straightforward approach for those who do not require
     * a customized {@link RetrievalAugmentor}.
     * It configures a {@link DefaultRetrievalAugmentor} with the provided {@link ContentRetriever}.
     *
     * @param contentRetriever The content retriever to be used by the AI Service.
     * @return builder
     */
    /**
     * 配置一个内容检索器，该检索器会在**每次方法调用时**执行，
     * 从底层数据源中检索与用户消息相关的内容
     * （例如，对于 {@link EmbeddingStoreContentRetriever} 来说，底层数据源就是向量存储库）。
     * 检索到的相关内容会**自动添加**到发送给大语言模型（LLM）的消息中。
     * <br>
     * 该方法为**不需要自定义检索增强器（RetrievalAugmentor）** 的用户提供了一种简单直接的方式。
     * 它会使用你传入的 {@link ContentRetriever}，自动配置一个 {@link DefaultRetrievalAugmentor}。
     *
     * @param contentRetriever AI服务将要使用的内容检索器
     * @return 返回构建器自身（支持链式调用）
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
    /**
     * 配置一个检索增强器，使其在**每次方法调用时**都被执行
     *
     * @param retrievalAugmentor AI服务将要使用的检索增强器
     * @return 返回构建器自身（支持链式调用）
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
     * <p>
     * By default, this is {@code true}, meaning that the final augmented user
     * message (after RAG augmentation) is stored in chat memory. This matches
     * the historical behaviour and ensures that the model sees the same
     * augmented content in subsequent turns.
     * <p>
     * If set to {@code false}, only the original user message (before RAG
     * augmentation) is stored in chat memory, while the augmented message is
     * still used for the LLM request. This helps to avoid storing retrieved
     * content in the conversation history and keeps the memory size smaller.
     *
     * @param storeRetrievedContentInChatMemory whether to store RAG-augmented user messages in chat memory
     * @return builder
     */
    public AiServices<T> storeRetrievedContentInChatMemory(boolean storeRetrievedContentInChatMemory) {
        context.storeRetrievedContentInChatMemory = storeRetrievedContentInChatMemory;
        return this;
    }

    /**
     * Enables or disables compensating actions on tool errors.
     * When enabled, if any tool execution fails, all previously executed tools'
     * compensating actions (declared via {@link dev.langchain4j.agent.tool.CompensateFor}) are called
     * in reverse order to undo the effects of the successful tool calls.
     *
     * @param compensateOnToolErrors whether to run compensating actions on tool errors
     * @return builder
     */
    public AiServices<T> compensateOnToolErrors(boolean compensateOnToolErrors) {
        context.toolService.compensateOnToolErrors(compensateOnToolErrors);
        return this;
    }

    /**
     * Constructs and returns the AI Service.
     *
     * @return An instance of the AI Service implementing the specified interface.
     */
    public abstract T build();

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
