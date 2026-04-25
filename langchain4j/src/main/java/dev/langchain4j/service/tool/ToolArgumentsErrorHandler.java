package dev.langchain4j.service.tool;

import java.util.function.Function;
import dev.langchain4j.exception.ToolArgumentsException;
import dev.langchain4j.service.AiServices;

/**
 * Handler for {@link ToolArgumentsException}s thrown by a {@link ToolExecutor}.
 * 让{@link ToolExecutor}使用的 错误参数处理器{@link ToolArgumentsException}s
 * <p>
 * Currently, there are two ways to handle errors:
 * 当前，这里有两种方式处理错误：
 * <p>
 * 1. Throw an exception: this will stop the AI service flow.
 * 1. 抛出一个错误：这将停止AI服务流程。
 * <p>
 * 2. Return a text message (e.g., an error description) that will be sent back to the LLM,
 * allowing it to respond appropriately (for example, by correcting the error and retrying).
 * 2. 返回一个文本消息（例如，错误描述），这将被发送回LLM，允许它以适当的方式响应（例如，通过纠正错误并重试）。
 *
 * @see ToolExecutionErrorHandler
 * @see AiServices#hallucinatedToolNameStrategy(Function)
 * @since 1.4.0
 */
@FunctionalInterface
public interface ToolArgumentsErrorHandler {

    /**
     * Handles an error that occurred during the parsing and preparation of tool arguments.
     * 在解析和准备工具参数时发生的错误
     * <p>
     * This method should either throw an exception or return a {@link ToolErrorHandlerResult#text(String)},
     * which will be sent to the LLM as the result of the tool execution.
     * 这个方式应该抛出一个异常或返回一个{@link ToolErrorHandlerResult#text(String)}，其将作为工具执行的结果被发送到LLM。
     *
     * @param error   The actual error that occurred. 实际发生的错误。
     * @param context The context in which the error occurred. 发生错误的上下文。
     * @return The result of error handling. 错误处理的结果。
     */
    ToolErrorHandlerResult handle(Throwable error, ToolErrorContext context);
}
