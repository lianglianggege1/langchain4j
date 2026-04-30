package dev.langchain4j.guardrail;

/**
 * Exception thrown when an input guardrail validation fails.
 * <p>
 *     This class is not intended to be thrown within guardrail implementations. It is for the framework only. It is ok to catch it.
 * </p>
 */
/**
 * 输入护栏校验失败时抛出的异常。
 * <p>
 *     此类不建议在护栏实现内部抛出，仅供框架内部使用。可以安全捕获该异常。
 * </p>
 */
public final class InputGuardrailException extends GuardrailException {
    public InputGuardrailException(String message) {
        super(message);
    }

    public InputGuardrailException(String message, Throwable cause) {
        super(message, cause);
    }
}
