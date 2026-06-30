package dev.langchain4j.guardrail;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrailResult.Failure;

/**
 * An input guardrail is a rule that is applied to the input of the model to ensure that the input (i.e. the user
 * message and parameters) is safe and meets the expectations of the model.
 * <p>
 *     Input guardrails are either successful or failed. A successful guardrail means that the input is valid and can be sent to
 *     the model. A failed guardrail means that the input is invalid and cannot be sent to the model.
 * </p>
 * <p>
 *     A failed guardrail will stop further processing of any other input guardrails.
 * </p>
 */
/**
 * 输入护栏是应用于模型输入的规则，用于确保输入（即用户消息和参数）是安全的，
 * 且符合模型的预期要求。
 * <p>
 *     输入护栏的执行结果分为成功和失败。
 *     成功表示输入有效，可以发送给模型；
 *     失败表示输入无效，禁止发送给模型。
 * </p>
 * <p>
 *     一旦某个输入护栏执行失败，将立即终止其他所有输入护栏的后续处理流程。
 * </p>
 */
public interface InputGuardrail extends Guardrail<InputGuardrailRequest, InputGuardrailResult> {
    /**
     * Validates the {@code user message} that will be sent to the LLM.
     * <p>
     *
     * @param userMessage
     *            the user message to be sent to the LLM
     */
    /**
     * 校验即将发送给大语言模型（LLM）的用户消息。
     *
     * @param userMessage 将要发送给 LLM 的用户消息
     */
    default InputGuardrailResult validate(UserMessage userMessage) {
        return failure("Validation not implemented");
    }

    /**
     * Validates the input that will be sent to the LLM.
     * <p>
     * Unlike {@link #validate(UserMessage)}, this method allows you to access the memory and the augmentation result (in
     * the case of a RAG).
     * <p>
     * Implementation must not attempt to write to the memory or the augmentation result.
     *
     * @param request
     *            the parameters, including the user message, the memory, and the augmentation result.
     */
    /**
     * 校验即将发送给大语言模型（LLM）的输入内容。
     * <p>
     * 与 {@link #validate(UserMessage)} 不同，此方法可以访问对话记忆（memory）和增强结果（augmentation result），
     * 适用于 RAG（检索增强生成）场景。
     * <p>
     * 实现类**不得**尝试修改记忆体或增强结果。
     *
     * @param request 校验参数，包含用户消息、对话记忆和增强结果
     */
    @Override
    default InputGuardrailResult validate(InputGuardrailRequest request) {
        ensureNotNull(request, "params");
        return validate(request.userMessage());
    }

    /**
     * Produces a successful result without any successful text
     *
     * @return The result of a successful input guardrail validation.
     */
    /**
     * 生成一个不携带任何成功文本的成功结果
     *
     * @return 输入护栏校验通过的结果
     */
    default InputGuardrailResult success() {
        return InputGuardrailResult.success();
    }

    /**
     * Produces a successful result with specific success text
     *
     * @return The result of a successful input guardrail validation with a specific text.
     *
     * @param successfulText
     *            The text of the successful result.
     */
    /**
     * 生成包含指定成功文本的成功结果
     *
     * @param successfulText 成功结果附带的文本
     * @return 携带指定文本的输入护栏校验成功结果
     */
    default InputGuardrailResult successWith(String successfulText) {
        return InputGuardrailResult.successWith(successfulText);
    }

    /**
     * Produces a non-fatal failure
     *
     * @param message
     *            A message describing the failure.
     *
     * @return The result of a failed input guardrail validation.
     */
    /**
     * 生成一个非致命性的失败结果
     *
     * @param message 描述失败原因的信息
     * @return 输入护栏校验失败的结果
     */
    default InputGuardrailResult failure(String message) {
        return new InputGuardrailResult(new Failure(message), false);
    }

    /**
     * Produces a non-fatal failure
     *
     * @param message
     *            A message describing the failure.
     * @param cause
     *            The exception that caused this failure.
     *
     * @return The result of a failed input guardrail validation.
     */
    /**
     * 生成一个非致命性的失败结果
     *
     * @param message 描述失败原因的信息
     * @param cause   导致此次失败的异常
     * @return 输入护栏校验失败的结果
     */
    default InputGuardrailResult failure(String message, Throwable cause) {
        return new InputGuardrailResult(new Failure(message, cause), false);
    }

    /**
     * Produces a fatal failure
     *
     * @param message
     *            A message describing the failure.
     *
     * @return The result of a failed input guardrail validation.
     */
    /**
     * 生成一个致命性失败结果
     *
     * @param message 描述失败原因的信息
     * @return 输入护栏校验失败的结果
     */
    default InputGuardrailResult fatal(String message) {
        return new InputGuardrailResult(new Failure(message), true);
    }

    /**
     * Produces a fatal failure
     *
     * @param message
     *            A message describing the failure.
     * @param cause
     *            The exception that caused this failure.
     *
     * @return The result of a failed input guardrail validation.
     */
    /**
     * 生成一个致命性失败结果
     *
     * @param message 描述失败原因的信息
     * @param cause   导致此次失败的异常
     * @return 输入护栏校验失败的结果
     */
    default InputGuardrailResult fatal(String message, Throwable cause) {
        return new InputGuardrailResult(new Failure(message, cause), true);
    }
}
