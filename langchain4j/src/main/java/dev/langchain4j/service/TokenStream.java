package dev.langchain4j.service;

import dev.langchain4j.Experimental;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialResponse;
import dev.langchain4j.model.chat.response.PartialResponseContext;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.PartialThinkingContext;
import dev.langchain4j.model.chat.response.PartialToolCall;
import dev.langchain4j.model.chat.response.PartialToolCallContext;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.tool.BeforeToolExecution;
import dev.langchain4j.service.tool.ToolExecution;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Represents a token stream from the model to which you can subscribe and receive updates
 * when a new partial response (usually a single token) is available,
 *  when the model finishes streaming, or when an error occurs during streaming.
 * It is intended to be used as a return type in AI Service.
 * <p/>
 * 表示来自模型的令牌流，
 * 当有新的部分响应（通常是单个令牌）可用、模型完成流式传输或流式传输过程中发生错误时，
 * 您可以订阅该令牌流并接收更新。它旨在用作AI服务中的返回类型。
 */
public interface TokenStream {

    /**
     * The provided consumer will be invoked every time a new partial textual response (usually a single token)
     * from a language model is available.
     * <p>
     * Either this or the {@link #onPartialResponseWithContext(BiConsumer)} callback can be used
     * if you want to consume tokens as soon as they become available.
     *
     * @param partialResponseHandler lambda that will be invoked when a model generates a new partial textual response
     * @return token stream instance used to configure or start stream processing
     * @see #onPartialResponseWithContext(BiConsumer)
     */
    /**
     * 每当语言模型有新的部分文本响应（通常是单个词元）生成时，
     * 传入的消费者函数都会被调用。
     * <p>
     * 如果你希望在词元生成后立即进行处理，可以使用此回调，
     * 或者使用 {@link #onPartialResponseWithContext(BiConsumer)} 回调（二选一即可）。
     *
     * @param partialResponseHandler 模型生成新的部分文本响应时会被调用的 Lambda 函数
     * @return 用于配置或启动流处理的词元流实例
     * @see #onPartialResponseWithContext(BiConsumer)
     */
    TokenStream onPartialResponse(Consumer<String> partialResponseHandler);

    /**
     * The provided consumer will be invoked every time a new partial textual response (usually a single token)
     * from a language model is available.
     * <p>
     * Either this or the {@link #onPartialResponse(Consumer)} callback can be used
     * if you want to consume tokens as soon as they become available.
     *
     * @param handler lambda that will be invoked when a model generates a new partial textual response
     * @return token stream instance used to configure or start stream processing
     * @see #onPartialResponse(Consumer)
     * @since 1.8.0
     */
    /**
     * 每当语言模型生成新的部分文本响应（通常为单个词元）时，
     * 传入的消费者函数都会被自动调用。
     * <p>
     * 若需要在词元生成后立即处理，可选择使用此回调，
     * 或 {@link #onPartialResponse(Consumer)} 回调（二者任选其一）。
     *
     * @param handler 模型生成新的部分文本响应时将被调用的 Lambda 表达式
     * @return 用于配置或启动流处理的词元流实例
     * @see #onPartialResponse(Consumer)
     * @since 1.8.0
     */
    @Experimental
    default TokenStream onPartialResponseWithContext(BiConsumer<PartialResponse, PartialResponseContext> handler) {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * The provided consumer will be invoked every time a new partial thinking/reasoning text (usually a single token)
     * from a language model is available.
     * <p>
     * Either this or the {@link #onPartialThinkingWithContext(BiConsumer)} callback can be used
     * if you want to consume thinking tokens as soon as they become available.
     *
     * @param partialThinkingHandler lambda that will be invoked when a model generates a new partial thinking/reasoning text
     * @return token stream instance used to configure or start stream processing
     * @see #onPartialThinkingWithContext(BiConsumer)
     * @since 1.2.0
     */
    /**
     * 每当语言模型生成新的部分思考/推理文本（通常为单个词元）时，
     * 传入的消费者函数都会被调用。
     * <p>
     * 若需要在思考词元生成后立即处理，可选择使用此回调，
     * 或 {@link #onPartialThinkingWithContext(BiConsumer)} 回调（二者任选其一）。
     *
     * @param partialThinkingHandler 模型生成新的部分思考/推理文本时将被调用的 Lambda 表达式
     * @return 用于配置或启动流处理的词元流实例
     * @see #onPartialThinkingWithContext(BiConsumer)
     * @since 1.2.0
     */
    @Experimental
    default TokenStream onPartialThinking(Consumer<PartialThinking> partialThinkingHandler) {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * The provided consumer will be invoked every time a new partial thinking/reasoning text (usually a single token)
     * from a language model is available.
     * <p>
     * Either this or the {@link #onPartialThinking(Consumer)} callback can be used
     * if you want to consume thinking tokens as soon as they become available.
     *
     * @param handler lambda that will be invoked when a model generates a new partial thinking/reasoning text
     * @return token stream instance used to configure or start stream processing
     * @see #onPartialThinking(Consumer)
     * @since 1.8.0
     */
    /**
     * 每当语言模型生成新的部分思考/推理文本（通常为单个词元）时，
     * 传入的消费者函数都会被调用。
     * <p>
     * 若需要在思考词元生成后立即处理，可选择使用此回调，
     * 或 {@link #onPartialThinking(Consumer)} 回调（二者任选其一）。
     *
     * @param handler 模型生成新的部分思考/推理文本时将被调用的 Lambda 表达式
     * @return 用于配置或启动流处理的词元流实例
     * @see #onPartialThinking(Consumer)
     * @since 1.8.0
     */
    @Experimental
    default TokenStream onPartialThinkingWithContext(BiConsumer<PartialThinking, PartialThinkingContext> handler) {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * The provided consumer will be invoked every time a new partial tool call
     * (usually containing a single token of the tool's arguments) from a language model is available.
     * <p>
     * Either this or the {@link #onPartialToolCallWithContext(BiConsumer)} callback can be used
     * if you want to consume partial tool calls as soon as they become available.
     *
     * @param partialToolCallHandler lambda that will be invoked when a model generates a new partial tool call
     * @return token stream instance used to configure or start stream processing
     * @see #onPartialToolCallWithContext(BiConsumer)
     * @since 1.11.0
     */
    /**
     * 每当语言模型生成新的部分工具调用（通常包含单个工具参数词元）时，
     * 传入的消费者函数都会被调用。
     * <p>
     * 若需要在部分工具调用生成后立即处理，可选择使用此回调，
     * 或 {@link #onPartialToolCallWithContext(BiConsumer)} 回调（二者任选其一）。
     *
     * @param partialToolCallHandler 模型生成新的部分工具调用时将被调用的 Lambda 表达式
     * @return 用于配置或启动流处理的词元流实例
     * @see #onPartialToolCallWithContext(BiConsumer)
     * @since 1.11.0
     */
    @Experimental
    default TokenStream onPartialToolCall(Consumer<PartialToolCall> partialToolCallHandler) {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * The provided consumer will be invoked every time a new partial tool call
     * (usually containing a single token of the tool's arguments) from a language model is available.
     * <p>
     * Either this or the {@link #onPartialToolCall(Consumer)} callback can be used
     * if you want to consume partial tool calls as soon as they become available.
     *
     * @param handler lambda that will be invoked when a model generates a new partial tool call
     * @return token stream instance used to configure or start stream processing
     * @see #onPartialToolCall(Consumer)
     * @since 1.11.0
     */
    /**
     * 每当语言模型生成新的部分工具调用（通常包含工具参数的单个词元）时，
     * 传入的消费者函数都会被调用。
     * <p>
     * 若需要在部分工具调用生成后立即处理，可选择使用此回调，
     * 或 {@link #onPartialToolCall(Consumer)} 回调（二者任选其一）。
     *
     * @param handler 模型生成新的部分工具调用时将被调用的 Lambda 表达式
     * @return 用于配置或启动流处理的词元流实例
     * @see #onPartialToolCall(Consumer)
     * @since 1.11.0
     */
    @Experimental
    default TokenStream onPartialToolCallWithContext(BiConsumer<PartialToolCall, PartialToolCallContext> handler) {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * The provided consumer will be invoked if any {@link Content}s are retrieved using {@link RetrievalAugmentor}.
     * <p>
     * The invocation happens before any call is made to the language model.
     *
     * @param contentHandler lambda that consumes all retrieved contents
     * @return token stream instance used to configure or start stream processing
     */
    /**
     * 当通过 {@link RetrievalAugmentor} 获取到任意 {@link Content} 内容时，
     * 传入的消费者函数将会被调用。
     * <p>
     * 该调用会在向语言模型发起任何请求之前执行。
     *
     * @param contentHandler 用于处理所有已获取内容的 Lambda 表达式
     * @return 用于配置或启动流处理的词元流实例
     */
    TokenStream onRetrieved(Consumer<List<Content>> contentHandler);

    /**
     * The provided consumer will be invoked when a language model finishes streaming the <i>intermediate</i> chat response,
     * as opposed to the final response (see {@link #onCompleteResponse(Consumer)}).
     * Intermediate chat responses contain {@link ToolExecutionRequest}s, AI service will execute them
     * after returning from this consumer.
     *
     * @param intermediateResponseHandler lambda that consumes intermediate chat responses
     * @return token stream instance used to configure or start stream processing
     * @see #onCompleteResponse(Consumer)
     * @since 1.2.0
     */
    /**
     * 每当语言模型生成新的中间聊天响应时，
     * 传入的消费者函数都会被调用。
     * <p>
     * 该调用会在向语言模型发起任何请求之前执行。
     *
     * @param intermediateResponseHandler 用于处理中间聊天响应的 Lambda 表达式
     * @return 用于配置或启动流处理的词元流实例
     * @see #onCompleteResponse(Consumer)
     * @since 1.2.0
     */
    default TokenStream onIntermediateResponse(Consumer<ChatResponse> intermediateResponseHandler) {
        throw new UnsupportedOperationException("Consuming intermediate responses is not supported "
                + "by this implementation of TokenStream: " + this.getClass().getName());
    }

    /**
     * The provided consumer will be invoked right before a tool is executed.
     *
     * @param beforeToolExecutionHandler lambda that consumes {@link BeforeToolExecution}
     * @return token stream instance used to configure or start stream processing
     * @since 1.2.0
     */
    /**
     * 每当执行一个工具之前，
     * 传入的消费者函数都会被调用。
     * <p>
     * 该调用会在向语言模型发起任何请求之前执行。
     *
     * @param beforeToolExecutionHandler 用于处理工具执行前信息的 Lambda 表达式
     * @return 用于配置或启动流处理的词元流实例
     * @since 1.2.0
     */
    default TokenStream beforeToolExecution(Consumer<BeforeToolExecution> beforeToolExecutionHandler) {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * The provided consumer will be invoked right after a tool is executed.
     * <p>
     * The invocation happens after the tool method has finished and before any other tool is executed.
     *
     * @param toolExecuteHandler lambda that consumes {@link ToolExecution}
     * @return token stream instance used to configure or start stream processing
     */
    /**
     * 传入的消费者函数将在工具执行完毕后立即被调用。
     * <p>
     * 该调用在工具方法执行结束后、其他工具执行之前触发。
     *
     * @param toolExecuteHandler 处理 {@link ToolExecution} 的 Lambda 表达式
     * @return 用于配置或启动流处理的词元流实例
     */
    TokenStream onToolExecuted(Consumer<ToolExecution> toolExecuteHandler);

    /**
     * The provided consumer will be invoked when a language model finishes streaming the <i>final</i> chat response,
     * as opposed to the intermediate response (see {@link #onIntermediateResponse(Consumer)}).
     * <p>
     * Please note that {@link ChatResponse#tokenUsage()} contains aggregate token usage across all calls to the LLM.
     * It is a sum of {@link ChatResponse#tokenUsage()}s of all intermediate responses
     * ({@link #onIntermediateResponse(Consumer)}).
     *
     * @param completeResponseHandler lambda that will be invoked when language model finishes streaming
     * @return token stream instance used to configure or start stream processing
     * @see #onIntermediateResponse(Consumer)
     */
    /**
     * 每当语言模型生成新的最终聊天响应时，
     * 传入的消费者函数都会被调用。
     * <p>
     * 该调用会在向语言模型发起任何请求之前执行。
     *
     * @param completeResponseHandler 用于处理最终聊天响应的 Lambda 表达式
     * @return 用于配置或启动流处理的词元流实例
     * @see #onIntermediateResponse(Consumer)
     */
    TokenStream onCompleteResponse(Consumer<ChatResponse> completeResponseHandler);

    /**
     * The provided consumer will be invoked when an error occurs during streaming.
     *
     * @param errorHandler lambda that will be invoked when an error occurs
     * @return token stream instance used to configure or start stream processing
     */
    /**
     * 每当流处理过程中发生错误时，
     * 传入的消费者函数都会被调用。
     * <p>
     * 该调用会在向语言模型发起任何请求之前执行。
     *
     * @param errorHandler 用于处理错误的 Lambda 表达式
     * @return 用于配置或启动流处理的词元流实例
     */
    TokenStream onError(Consumer<Throwable> errorHandler);

    /**
     * All errors during streaming will be ignored (but will be logged with a WARN log level).
     *
     * @return token stream instance used to configure or start stream processing
     */
    /**
     * 流式处理过程中发生的所有异常都会被忽略（但会以WARN日志级别记录）。
     *
     * @return 用于配置或启动流处理的词元流实例
     */
    TokenStream ignoreErrors();

    /**
     * Completes the current token stream building and starts processing.
     * <p>
     * Will send a request to LLM and start response streaming.
     */
    /**
     * 完成当前词元流的构建并启动处理。
     * <p>
     * 将向大语言模型发送请求并启动响应流式传输。
     */
    void start();
}
