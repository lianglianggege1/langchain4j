package dev.langchain4j.service.guardrail;

import dev.langchain4j.guardrail.OutputGuardrail;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to apply guardrails to the output of the model using the declarative {@link dev.langchain4j.service.AiServices AiServices}
 * approach.
 * <p>
 *     An output guardrail is a rule that is applied to the output of the model to ensure that the output is safe and meets
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
 *     If the annotation is present on a class, the guardrails will be applied to all the methods of the class.
 * </p>
 * <p>
 *     When several guardrails are applied, the order of the guardrails is important, as the guardrails are applied in
 *     the order they are listed.
 * </p>
 * <p>
 *     When several {@link OutputGuardrail}s are applied, if any guardrail forces a retry or reprompt, then all of the
 *     guardrails will be re-applied to the new response.
 * </p>
 */
/**
 * 一个用于通过声明式 {@link dev.langchain4j.service.AiServices AiServices} 方式
 * 为模型输出添加护栏的注解。
 * <p>
 *     输出护栏是应用于模型输出的规则，用于确保输出安全且符合特定预期。
 * </p>
 * <p>
 *     当校验失败时，结果可指示请求是否应原样重试，或提供一个
 *     {@code reprompt} 消息追加到提示词中。
 * </p>
 * <p>
 *     若为重新提示场景，重新提示消息会被添加到大语言模型上下文，然后请求会被重试。
 * </p>
 * <p>
 *     如果该注解作用于类上，护栏将应用于该类的所有方法。
 * </p>
 * <p>
 *     当应用多个护栏时，顺序至关重要，因为护栏会按照声明的顺序依次执行。
 * </p>
 * <p>
 *     当应用多个 {@link OutputGuardrail} 时，如果任意一个护栏触发重试或重新提示，
 *     则所有护栏都会重新应用于新的响应结果。
 * </p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface OutputGuardrails {
    /**
     * The ordered list of guardrails to apply to the output of the model.
     * <p>
     *     The order of the classes is important as the guardrails are applied in the order they are listed.
     *     Guardrails can not be present twice in the list.
     * </p>
     */
    /**
     * 要应用于模型输出的、按顺序排列的护栏列表。
     * <p>
     *     类的顺序至关重要，因为护栏会按照列表中的声明顺序依次执行。
     *     同一个护栏不能在列表中重复出现。
     * </p>
     */
    Class<? extends OutputGuardrail>[] value();

    /**
     * The maximum number of retries to perform when an output guardrail forces a retry or reprompt.
     * <p>
     *     Set to {@code 0} to disable retries
     * </p>
     * @see dev.langchain4j.guardrail.config.OutputGuardrailsConfig#maxRetries()
     */
    /**
     * 当输出护栏触发重试或重新提示时，允许执行的最大重试次数。
     * <p>
     *     设置为 {@code 0} 表示禁用重试
     * </p>
     * @see dev.langchain4j.guardrail.config.OutputGuardrailsConfig#maxRetries()
     */
    int maxRetries() default dev.langchain4j.guardrail.config.OutputGuardrailsConfig.MAX_RETRIES_DEFAULT;
}
