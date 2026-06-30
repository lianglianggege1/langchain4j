package dev.langchain4j.service.tool;

import dev.langchain4j.exception.ToolExecutionException;
import dev.langchain4j.service.AiServices;
import java.util.function.Function;

/**
 * Handler for {@link ToolExecutionException}s thrown by a {@link ToolExecutor}.
 * <p>
 * There are two ways to handle errors:
 * <p>
 * 1. Return a {@link ToolErrorHandlerResult#text(String) text message} that will be sent back
 * to the LLM, allowing it to respond appropriately (for example, by correcting the error and retrying).
 * <p>
 * 2. Throw an exception: this will stop the AI service flow.
 * Use {@link ToolErrorContext#rawError()} to access the raw error before cause-unwrapping
 * when deciding whether to throw.
 *
 * @see ToolArgumentsErrorHandler
 * @see AiServices#hallucinatedToolNameStrategy(Function)
 * @since 1.4.0
 */
/**
 * 工具执行器抛出{@link ToolExecutionException}异常时的处理器。
 * <p>
 * 目前提供两种错误处理方式：
 * <p>
 * 1. 抛出异常：终止AI服务流程。
 * <p>
 * 2. 返回文本信息（如错误描述）并回传给大语言模型，
 * 使其做出相应处理（例如修正错误后重试）。
 *
 * @see ToolExecutionErrorHandler
 * @see AiServices#hallucinatedToolNameStrategy(Function)
 * @since 1.4.0
 */
@FunctionalInterface
public interface ToolExecutionErrorHandler {

    /**
     * Handles an error that occurred during tool execution.
     * <p>
     * This method should either throw an exception or return a {@link ToolErrorHandlerResult#text(String)},
     * which will be sent to the LLM as the result of the tool execution.
     *
     * @param error   The actual error that occurred (cause-unwrapped).
     *                Use {@link ToolErrorContext#rawError()} for the error before unwrapping.
     * @param context The context in which the error occurred.
     * @return The result of error handling.
     */
    /**
     * 处理工具执行过程中发生的错误。
     * <p>
     * 该方法要么抛出异常，要么返回 {@link ToolErrorHandlerResult#text(String)} 类型的结果，
     * 该结果将作为工具执行的最终结果发送给大语言模型。
     *
     * @param error   实际发生的错误信息。
     * @param context 错误发生时的上下文信息。
     * @return 错误处理的结果。
     */
    ToolErrorHandlerResult handle(Throwable error, ToolErrorContext context);
}
