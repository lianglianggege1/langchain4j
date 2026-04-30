package dev.langchain4j.guardrail;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The result of the validation of an interaction between a user and the LLM.
 *
 * @param <GR>
 *            The type of guardrail result to expect
 *
 * @see InputGuardrailResult
 * @see OutputGuardrailResult
 */
/**
 * 用户与大语言模型（LLM）之间交互的校验结果。
 *
 * @param <GR> 预期的护栏结果类型
 *
 * @see InputGuardrailResult
 * @see OutputGuardrailResult
 */
public sealed interface GuardrailResult<GR extends GuardrailResult<GR>>
        permits InputGuardrailResult, OutputGuardrailResult {
    /**
     * The possible results of a guardrails validation.
     */
    /**
     * 护栏校验的所有可能结果。
     */
    enum Result {
        /**
         * A successful validation.
         * 校验通过。
         */
        SUCCESS,
        /**
         * A successful validation with a specific result.
         * 附带特定结果的校验通过。
         */
        SUCCESS_WITH_RESULT,
        /**
         * A failed validation not preventing the subsequent validations eventually registered to be evaluated.
         * 校验失败，但不会阻止后续已注册的其他校验继续执行。
         */
        FAILURE,
        /**
         * A fatal failed validation, blocking the evaluation of any other validations eventually registered.
         * 致命性校验失败，将阻止执行其他所有已注册的校验。
         */
        FATAL
    }

    /**
     * The message and the cause of the failure of a single validation.
     * 单次校验失败的提示信息与失败原因。
     */
    sealed interface Failure permits InputGuardrailResult.Failure, OutputGuardrailResult.Failure {
        /**
         * Build a failure from a specific {@link Guardrail} class
         * 根据指定的护栏（Guardrail）类构建一个失败结果。
         */
        Failure withGuardrailClass(Class<? extends Guardrail> guardrailClass);

        /**
         * The failure message
         */
        String message();

        /**
         * The cause of the failure
         */
        Throwable cause();

        /**
         * The {@link Guardrail} class
         */
        Class<? extends Guardrail> guardrailClass();

        /**
         * The string representation of the failure
         * @return A string representation of the failure
         */
        default String asString() {
            var guardrailName =
                    Optional.ofNullable(guardrailClass()).map(Class::getName).orElse("");

            return "The guardrail %s failed with this message: %s".formatted(guardrailName, message());
        }
    }

    /**
     * The result of the guardrail
     */
    Result result();

    /**
     * @return The list of failures eventually resulting from a set of validations.
     */
    <F extends Failure> List<F> failures();

    /**
     * The message of the successful result
     */
    String successfulText();

    /**
     * Whether or not the result is successful, but the result was re-written, potentially due to re-prompting
     */
    default boolean hasRewrittenResult() {
        return result() == Result.SUCCESS_WITH_RESULT;
    }

    /**
     * Whether or not the result is considered fatal
     */
    default boolean isFatal() {
        return result() == Result.FATAL;
    }

    /**
     * Whether or not the result is considered successful
     */
    default boolean isSuccess() {
        var result = result();
        return (result == Result.SUCCESS) || (result == Result.SUCCESS_WITH_RESULT);
    }

    /**
     * Gets the exception from the first failure
     */
    default Throwable getFirstFailureException() {
        return !isSuccess()
                ? failures().stream()
                        .map(Failure::cause)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null)
                : null;
    }

    /**
     * The {@link Guardrail} class which performed this validation
     */
    default GR validatedBy(Class<? extends Guardrail> guardrailClass) {
        ensureNotNull(guardrailClass, "guardrailClass");

        if (!isSuccess()) {
            var failures = failures();

            if (failures.size() != 1) {
                throw new IllegalArgumentException();
            }

            failures.set(0, failures.get(0).withGuardrailClass(guardrailClass));
        }

        return (GR) this;
    }

    default String asString() {
        if (isSuccess()) {
            return hasRewrittenResult() ? "Success with '%s'".formatted(successfulText()) : "Success";
        }

        return failures().stream().map(Failure::toString).collect(Collectors.joining(", "));
    }
}
