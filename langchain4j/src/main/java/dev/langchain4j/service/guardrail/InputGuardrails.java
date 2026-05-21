package dev.langchain4j.service.guardrail;

import dev.langchain4j.guardrail.InputGuardrail;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to apply input guardrails to the input of the model using the declarative {@link dev.langchain4j.service.AiServices AiServices} approach.
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
 *     If the annotation is present on a class, the guardrails will be applied to all the methods of the class.
 * </p>
 * <p>
 *     When several guardrails are applied, the order of the guardrails is important, as the guardrails are applied in the order
 *     they are listed.
 * </p>
 */
/**
 * 一个用于通过声明式 {@link dev.langchain4j.service.AiServices AiServices} 方式
 * 为模型输入添加输入护栏的注解。
 * <p>
 *     输入护栏是应用于模型输入（本质上是用户消息）的规则，用于确保
 *     输入安全且符合模型预期。它不会替代内容审核模型，但可用于
 *     增加额外校验（例如提示词注入检测等）。
 * </p>
 * <p>
 *     与输出护栏不同，输入护栏不支持重试或重新提示。失败会直接
 *     封装为 {@link dev.langchain4j.guardrail.GuardrailException GuardrailException}
 *     传递给调用方。
 * </p>
 * <p>
 *     如果该注解作用于类上，护栏将应用于该类的所有方法。
 * </p>
 * <p>
 *     当应用多个护栏时，顺序至关重要，因为护栏会按照声明的顺序依次执行。
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface InputGuardrails {
    /**
     * The ordered list of {@link InputGuardrail}s to apply to the input of the model.
     * <p>
     *     The order of the classes is important as the guardrails are applied in the order they are listed.
     *     Guardrails can not be present twice in the list.
     * </p>
     */
    /**
     * 要应用于模型输入的、按顺序排列的 {@link InputGuardrail} 列表。
     * <p>
     *     类的顺序至关重要，因为护栏会按照列表中的声明顺序依次执行。
     *     同一个护栏不能在列表中重复出现。
     * </p>
     */
    Class<? extends InputGuardrail>[] value();
}
