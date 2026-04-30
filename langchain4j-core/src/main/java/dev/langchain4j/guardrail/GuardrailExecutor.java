package dev.langchain4j.guardrail;

import dev.langchain4j.guardrail.config.GuardrailsConfig;
import dev.langchain4j.observability.api.event.GuardrailExecutedEvent;
import java.util.List;

/**
 * Represents a mechanism to execute a set of guardrails on given parameters.
 * This interface defines the contract for validating interactions (input or output)
 * using multiple guardrails.
 *
 * @param <C>
 *            The type of {@link GuardrailsConfig} to use for configuration
 * @param <P>
 *            The type of {@link GuardrailRequest} to validate
 * @param <R>
 *            The type of {@link GuardrailResult} to return
 * @param <G>
 *            The type of {@link Guardrail}s being executed
 * @param <E> The type of {@link GuardrailExecutedEvent} to be fired
 */
/**
 * 表示一种在指定参数上执行一组护栏规则的机制。
 * 该接口定义了使用多个护栏校验交互内容（输入或输出）的约定。
 *
 * @param <C> 用于配置的 {@link GuardrailsConfig} 类型
 * @param <P> 待校验的 {@link GuardrailRequest} 类型
 * @param <R> 要返回的 {@link GuardrailResult} 类型
 * @param <G> 正在执行的 {@link Guardrail} 护栏类型
 * @param <E> 要触发的 {@link GuardrailExecutedEvent} 事件类型
 */
public sealed interface GuardrailExecutor<
                C extends GuardrailsConfig,
                P extends GuardrailRequest<P>,
                R extends GuardrailResult<R>,
                G extends Guardrail<P, R>,
                E extends GuardrailExecutedEvent<P, R, G>>
        permits AbstractGuardrailExecutor {

    /**
     * The {@link GuardrailsConfig} to use for configuration of the guardrail execution
     * @return The {@link GuardrailsConfig} to use for configuration of the guardrail execution
     */
    /**
     * 用于配置护栏执行的 {@link GuardrailsConfig}
     * @return 用于配置护栏执行的 {@link GuardrailsConfig} 实例
     */
    C config();

    /**
     * Retrieves the guardrails associated with the implementation.
     * @return The guardrails which can be used for validating inputs or outputs against predefined rules.
     */
    /**
     * 获取与当前实现关联的护栏规则集合。
     * @return 可用于根据预定义规则校验输入或输出的护栏规则列表
     */
    List<G> guardrails();

    /**
     * Executes the provided guardrails on the given parameters.
     * @param request The {@link GuardrailRequest} to validate
     * @return The {@link GuardrailResult} of the validation
     */
    /**
     * 对给定参数执行已配置的护栏规则校验。
     * @param request 待校验的 {@link GuardrailRequest}
     * @return 护栏规则的校验结果 {@link GuardrailResult}
     */
    R execute(P request);
}
