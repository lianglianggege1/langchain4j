package dev.langchain4j.guardrail;

/**
 * Exception thrown when an output guardrail validation fails.
 * <p>
 *     This class is not intended to be thrown within guardrail implementations. It is for the framework only. It is ok to catch it.
 * </p>
 */
/**
 * 输出护栏校验失败时抛出的异常。
 * <p>
 *     该类不建议在护栏实现代码内部直接抛出，仅供框架内部使用；捕获该异常是安全的。
 * </p>
 */
public final class OutputGuardrailException extends GuardrailException {
    public OutputGuardrailException(String message) {
        super(message);
    }

    public OutputGuardrailException(String message, Throwable cause) {
        super(message, cause);
    }
}
