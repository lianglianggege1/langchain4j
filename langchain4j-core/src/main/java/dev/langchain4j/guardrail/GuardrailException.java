package dev.langchain4j.guardrail;

import dev.langchain4j.exception.LangChain4jException;

/**
 * Exception thrown when an input or output guardrail validation fails.
 * <p>
 *     This class is not intended to be used within guardrail implementations. It is for the framework only.
 * </p>
 * @see InputGuardrailException
 * @see OutputGuardrailException
 */
/**
 * 输入或输出护栏校验失败时抛出的异常。
 * <p>
 *     此类不建议在护栏实现内部使用，仅供框架内部使用。
 * </p>
 * @see InputGuardrailException
 * @see OutputGuardrailException
 */
public sealed class GuardrailException extends LangChain4jException
        permits InputGuardrailException, OutputGuardrailException {
    protected GuardrailException(String message) {
        super(message);
    }

    protected GuardrailException(String message, Throwable cause) {
        super(message, cause);
    }
}
