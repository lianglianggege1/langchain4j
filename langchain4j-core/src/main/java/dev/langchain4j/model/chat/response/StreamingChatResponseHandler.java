package dev.langchain4j.model.chat.response;

import dev.langchain4j.Experimental;
import dev.langchain4j.model.chat.StreamingChatModel;

/**
 * Represents a handler for a {@link StreamingChatModel} response.
 * 表示 {@link StreamingChatModel} 响应的处理程序。
 *
 * @see StreamingChatModel
 */
public interface StreamingChatResponseHandler {

    /**
     * Invoked each time the model generates a partial textual response, usually a single token.
     * <p>
     * Please note that some LLM providers do not stream individual tokens, but send responses in batches.
     * In such cases, this callback may receive multiple tokens at once.
     * <p>
     * Either this or the {@link #onPartialResponse(PartialResponse, PartialResponseContext)} method
     * should be implemented if you want to consume tokens as soon as they become available.
     *
     * @param partialResponse A partial textual response, usually a single token.
     * @see #onPartialResponse(PartialResponse, PartialResponseContext)
     */
    /**
     * 每次模型生成一段文本式的部分响应时都会调用，通常是单个令牌。
     * <p>
     * 请注意，部分大语言模型服务商不会逐个流式传输令牌，而是批量发送响应。
     * 在这种情况下，此回调可能会一次性接收多个令牌。
     * <p>
     * 如果你希望令牌一产生就立即消费，
     * 要么实现此方法，要么实现 {@link #onPartialResponse(PartialResponse, PartialResponseContext)} 方法。
     *
     * @param partialResponse 部分文本响应，通常是单个令牌
     * @see #onPartialResponse(PartialResponse, PartialResponseContext)
     */
    default void onPartialResponse(String partialResponse) {}

    /**
     * Invoked each time the model generates a partial textual response, usually a single token.
     * <p>
     * Please note that some LLM providers do not stream individual tokens, but send responses in batches.
     * In such cases, this callback may receive multiple tokens at once.
     * <p>
     * Either this or the {@link #onPartialResponse(String)} method
     * should be implemented if you want to consume tokens as soon as they become available.
     *
     * @param partialResponse A partial textual response, usually a single token.
     * @param context         A partial response context.
     *                        Contains a {@link StreamingHandle} that can be used to cancel streaming.
     * @see #onPartialResponse(String)
     * @since 1.8.0
     */
    /**
     * 每次模型生成一段文本式的部分响应时都会调用，通常是单个令牌。
     * <p>
     * 请注意，部分大语言模型服务商不会逐个流式传输令牌，而是批量发送响应。
     * 在这种情况下，此回调可能会一次性接收多个令牌。
     * <p>
     * 如果你希望令牌一产生就立即消费，
     * 要么实现此方法，要么实现 {@link #onPartialResponse(String)} 方法。
     *
     * @param partialResponse 部分文本响应，通常是单个令牌
     * @param context 部分响应上下文
     *                包含可用于取消流式传输的 {@link StreamingHandle}
     * @see #onPartialResponse(String)
     * @since 1.8.0
     */
    @Experimental
    default void onPartialResponse(PartialResponse partialResponse, PartialResponseContext context) {
        onPartialResponse(partialResponse.text());
    }

    /**
     * Invoked each time the model generates a partial thinking/reasoning text, usually a single token.
     * <p>
     * Please note that some LLM providers do not stream individual tokens, but send thinking tokens in batches.
     * In such cases, this callback may receive multiple tokens at once.
     * <p>
     * Either this or the {@link #onPartialThinking(PartialThinking, PartialThinkingContext)} method
     * should be implemented if you want to consume thinking tokens as soon as they become available.
     *
     * @param partialThinking A partial thinking text, usually a single token.
     * @see #onPartialThinking(PartialThinking, PartialThinkingContext)
     * @since 1.2.0
     */
    /**
     * 每次模型生成一段思考/推理文本片段时都会调用，通常是单个令牌。
     * <p>
     * 请注意，部分大语言模型服务商不会逐个流式传输令牌，而是批量发送思考令牌。
     * 在这种情况下，此回调可能会一次性接收多个令牌。
     * <p>
     * 如果你希望思考令牌一产生就立即消费，
     * 要么实现此方法，要么实现 {@link #onPartialThinking(PartialThinking, PartialThinkingContext)} 方法。
     *
     * @param partialThinking 思考文本片段，通常是单个令牌
     * @see #onPartialThinking(PartialThinking, PartialThinkingContext)
     * @since 1.2.0
     */
    @Experimental
    default void onPartialThinking(PartialThinking partialThinking) {}

    /**
     * Invoked each time the model generates a partial thinking/reasoning text, usually a single token.
     * <p>
     * Please note that some LLM providers do not stream individual tokens, but send thinking tokens in batches.
     * In such cases, this callback may receive multiple tokens at once.
     * <p>
     * Either this or the {@link #onPartialThinking(PartialThinking)} method
     * should be implemented if you want to consume thinking tokens as soon as they become available.
     *
     * @param partialThinking A partial thinking text, usually a single token.
     * @param context         A partial thinking context.
     *                        Contains a {@link StreamingHandle} that can be used to cancel streaming.
     * @see #onPartialThinking(PartialThinking)
     * @since 1.8.0
     */
    /**
     * 每次模型生成一段思考/推理文本片段时都会调用，通常是单个令牌。
     * <p>
     * 请注意，部分大语言模型服务商不会逐个流式传输令牌，而是批量发送思考令牌。
     * 在这种情况下，此回调可能会一次性接收多个令牌。
     * <p>
     * 如果你希望思考令牌一产生就立即消费，
     * 要么实现此方法，要么实现 {@link #onPartialThinking(PartialThinking)} 方法。
     *
     * @param partialThinking 思考文本片段，通常是单个令牌
     * @param context 思考响应上下文
     *                包含可用于取消流式传输的 {@link StreamingHandle}
     * @see #onPartialThinking(PartialThinking)
     * @since 1.8.0
     */
    @Experimental
    default void onPartialThinking(PartialThinking partialThinking, PartialThinkingContext context) {
        onPartialThinking(partialThinking);
    }

    /**
     * This callback is invoked each time the model generates a partial tool call,
     * which contains a single token of the tool's arguments.
     * It is typically invoked multiple times for a single tool call
     * until {@link #onCompleteToolCall(CompleteToolCall)} is eventually invoked,
     * indicating that the streaming for that tool call is finished.
     * <p>
     * Here's an example of what streaming a single tool call might look like:
     * <pre>
     * 1. onPartialToolCall(index = 0, id = "call_abc", name = "get_weather", partialArguments = "{\"")
     * 2. onPartialToolCall(index = 0, id = "call_abc", name = "get_weather", partialArguments = "city")
     * 3. onPartialToolCall(index = 0, id = "call_abc", name = "get_weather", partialArguments = ""\":\"")
     * 4. onPartialToolCall(index = 0, id = "call_abc", name = "get_weather", partialArguments = "Mun")
     * 5. onPartialToolCall(index = 0, id = "call_abc", name = "get_weather", partialArguments = "ich")
     * 6. onPartialToolCall(index = 0, id = "call_abc", name = "get_weather", partialArguments = "\"}")
     * 7. onCompleteToolCall(index = 0, id = "call_abc", name = "get_weather", arguments = "{\"city\":\"Munich\"}")
     * </pre>
     * <p>
     * If the model decides to call multiple tools, the index will increment, allowing you to correlate.
     * <p>
     * Please note that not all LLM providers stream tool calls token by token.
     * Some providers (e.g., Bedrock, Google, Mistral, Ollama) return only complete tool calls.
     * In those cases, this callback won't be invoked - only {@link #onCompleteToolCall(CompleteToolCall)}
     * will be called.
     * <p>
     * Either this or the {@link #onPartialToolCall(PartialToolCall, PartialToolCallContext)} method
     * should be implemented if you want to consume partial tool calls as soon as they become available.
     *
     * @param partialToolCall A partial tool call that contains
     *                        the index, tool ID, tool name and partial arguments.
     * @see #onPartialToolCall(PartialToolCall, PartialToolCallContext)
     * @since 1.2.0
     */
    /**
     * 每次模型生成一段工具调用片段时都会触发此回调，
     * 每次仅包含工具参数的单个令牌。
     * 对于单次工具调用，该方法通常会被多次调用，
     * 直到最终触发 {@link #onCompleteToolCall(CompleteToolCall)}，
     * 表示该工具调用的流式传输完成。
     * <p>
     * 以下是单次工具调用流式传输的示例：
     * <pre>
     * 1. onPartialToolCall(index = 0, id = "call_abc", name = "get_weather", partialArguments = "{\"")
     * 2. onPartialToolCall(index = 0, id = "call_abc", name = "get_weather", partialArguments = "city")
     * 3. onPartialToolCall(index = 0, id = "call_abc", name = "get_weather", partialArguments = ""\":\"")
     * 4. onPartialToolCall(index = 0, id = "call_abc", name = "get_weather", partialArguments = "Mun")
     * 5. onPartialToolCall(index = 0, id = "call_abc", name = "get_weather", partialArguments = "ich")
     * 6. onPartialToolCall(index = 0, id = "call_abc", name = "get_weather", partialArguments = "\"}")
     * 7. onCompleteToolCall(index = 0, id = "call_abc", name = "get_weather", arguments = "{\"city\":\"Munich\"}")
     * </pre>
     * <p>
     * 如果模型需要调用多个工具，索引值会递增，便于你进行关联匹配。
     * <p>
     * 请注意：并非所有大模型服务商都支持逐令牌流式传输工具调用。
     * 部分服务商（如 Bedrock、Google、Mistral、Ollama）仅返回完整的工具调用。
     * 在这种情况下，此回调不会被触发 —— 仅 {@link #onCompleteToolCall(CompleteToolCall)} 会被调用。
     * <p>
     * 如果你希望工具调用片段一产生就立即消费，
     * 要么实现此方法，要么实现 {@link #onPartialToolCall(PartialToolCall, PartialToolCallContext)} 方法。
     *
     * @param partialToolCall 工具调用片段，包含索引、工具ID、工具名称和片段参数
     * @see #onPartialToolCall(PartialToolCall, PartialToolCallContext)
     * @since 1.2.0
     */
    @Experimental
    default void onPartialToolCall(PartialToolCall partialToolCall) {}

    /**
     * This callback is invoked each time the model generates a partial tool call,
     * which contains a single token of the tool's arguments.
     * It is typically invoked multiple times for a single tool call
     * until {@link #onCompleteToolCall(CompleteToolCall)} is eventually invoked,
     * indicating that the streaming for that tool call is finished.
     * <p>
     * Here's an example of what streaming a single tool call might look like:
     * <pre>
     * 1. onPartialToolCall(index = 0, id = "call_abc", name = "get_weather", partialArguments = "{\"")
     * 2. onPartialToolCall(index = 0, id = "call_abc", name = "get_weather", partialArguments = "city")
     * 3. onPartialToolCall(index = 0, id = "call_abc", name = "get_weather", partialArguments = ""\":\"")
     * 4. onPartialToolCall(index = 0, id = "call_abc", name = "get_weather", partialArguments = "Mun")
     * 5. onPartialToolCall(index = 0, id = "call_abc", name = "get_weather", partialArguments = "ich")
     * 6. onPartialToolCall(index = 0, id = "call_abc", name = "get_weather", partialArguments = "\"}")
     * 7. onCompleteToolCall(index = 0, id = "call_abc", name = "get_weather", arguments = "{\"city\":\"Munich\"}")
     * </pre>
     * <p>
     * If the model decides to call multiple tools, the index will increment, allowing you to correlate.
     * <p>
     * Please note that not all LLM providers stream tool calls token by token.
     * Some providers (e.g., Bedrock, Google, Mistral, Ollama) return only complete tool calls.
     * In those cases, this callback won't be invoked - only {@link #onCompleteToolCall(CompleteToolCall)}
     * will be called.
     * <p>
     * Either this or the {@link #onPartialToolCall(PartialToolCall)} method
     * should be implemented if you want to consume partial tool calls as soon as they become available.
     *
     * @param partialToolCall A partial tool call that contains
     *                        the index, tool ID, tool name and partial arguments.
     * @param context         A partial tool call context.
     *                        Contains a {@link StreamingHandle} that can be used to cancel streaming.
     * @see #onPartialToolCall(PartialToolCall)
     * @since 1.8.0
     */
    /**
     * 每次模型生成一段工具调用片段时都会触发此回调，
     * 每次仅包含工具参数的单个令牌。
     * 对于单次工具调用，该方法通常会被多次调用，
     * 直到最终触发 {@link #onCompleteToolCall(CompleteToolCall)}，
     * 表示该工具调用的流式传输完成。
     * <p>
     * 以下是单次工具调用流式传输的示例：
     * <pre>
     * 1. onPartialToolCall(index = 0, id = "call_abc", name = "get_weather", partialArguments = "{\"")
     * 2. onPartialToolCall(index = 0, id = "call_abc", name = "get_weather", partialArguments = "city")
     * 3. onPartialToolCall(index = 0, id = "call_abc", name = "get_weather", partialArguments = ""\":\"")
     * 4. onPartialToolCall(index = 0, id = "call_abc", name = "get_weather", partialArguments = "Mun")
     * 5. onPartialToolCall(index = 0, id = "call_abc", name = "get_weather", partialArguments = "ich")
     * 6. onPartialToolCall(index = 0, id = "call_abc", name = "get_weather", partialArguments = "\"}")
     * 7. onCompleteToolCall(index = 0, id = "call_abc", name = "get_weather", arguments = "{\"city\":\"Munich\"}")
     * </pre>
     * <p>
     * 如果模型需要调用多个工具，索引值会递增，便于你进行关联匹配。
     * <p>
     * 请注意：并非所有大模型服务商都支持逐令牌流式传输工具调用。
     * 部分服务商（如 Bedrock、Google、Mistral、Ollama）仅返回完整的工具调用。
     * 在这种情况下，此回调不会被触发 —— 仅 {@link #onCompleteToolCall(CompleteToolCall)} 会被调用。
     * <p>
     * 如果你希望工具调用片段一产生就立即消费，
     * 要么实现此方法，要么实现 {@link #onPartialToolCall(PartialToolCall)} 方法。
     *
     * @param partialToolCall 工具调用片段，包含索引、工具ID、工具名称和片段参数
     * @param context 工具调用上下文
     *                包含可用于取消流式传输的 {@link StreamingHandle}
     * @see #onPartialToolCall(PartialToolCall)
     * @since 1.8.0
     */
    @Experimental
    default void onPartialToolCall(PartialToolCall partialToolCall, PartialToolCallContext context) {
        onPartialToolCall(partialToolCall);
    }

    /**
     * Invoked when the model has finished streaming a single tool call.
     *
     * @param completeToolCall A complete tool call that contains
     *                         the index, tool ID, tool name, and fully assembled arguments.
     * @since 1.2.0
     */
    /**
     * 当模型完成单次工具调用的流式传输后触发。
     *
     * @param completeToolCall 完整的工具调用，包含索引、工具ID、工具名称以及已组装完成的完整参数
     * @since 1.2.0
     */
    @Experimental
    default void onCompleteToolCall(CompleteToolCall completeToolCall) {}

    /**
     * Invoked when a provider emits a raw streaming event that is <b>not</b> already exposed through one of the
     * typed callbacks (such as {@link #onPartialResponse(PartialResponse, PartialResponseContext)},
     * {@link #onPartialThinking(PartialThinking, PartialThinkingContext)},
     * {@link #onPartialToolCall(PartialToolCall, PartialToolCallContext)} or
     * {@link #onCompleteToolCall(CompleteToolCall)}).
     * <p>
     * This acts as an escape hatch for provider-specific events that langchain4j does not model, such as
     * server-tool lifecycle events (e.g., OpenAI's {@code web_search_call.in_progress}). Events that are already
     * delivered as partial responses, thinking or tool calls are not repeated here.
     * <p>
     * The event type depends on the provider implementation. Implementations using the
     * {@code dev.langchain4j.http.client.HttpClient} abstraction (e.g., OpenAI, Anthropic, Google AI Gemini)
     * typically expose {@code ServerSentEvent}; other implementations can expose provider-specific event objects
     * (e.g., the OpenAI official Responses model exposes the SDK's {@code ResponseStreamEvent}).
     *
     * @param rawEvent A raw provider streaming event.
     * @since 1.17.0
     */
    @Experimental
    default void onUnmappedRawEvent(Object rawEvent) {}

    /**
     * Invoked when the model has finished streaming a response.
     *
     * @param completeResponse The complete response generated by the model,
     *                         containing all assembled partial text and tool calls.
     */
    /**
     * 当模型完成响应的流式传输后触发。
     *
     * @param completeResponse 模型生成的完整响应，
     *                         包含所有已拼接完成的文本片段和工具调用信息
     */
    void onCompleteResponse(ChatResponse completeResponse);

    /**
     * This method is invoked when an error occurs during streaming.
     *
     * @param error The error that occurred
     */
    /**
     * 当流式传输过程中发生错误时调用此方法。
     *
     * @param error 发生的错误
     */
    void onError(Throwable error);
}
