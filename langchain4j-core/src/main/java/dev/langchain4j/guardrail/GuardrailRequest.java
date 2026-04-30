package dev.langchain4j.guardrail;

/**
 * Represents the parameter passed to {@link Guardrail#validate(GuardrailRequest)}} in order to validate an interaction
 * between a user and the LLM.
 */
/**
 * 表示传递给 {@link Guardrail#validate(GuardrailRequest)} 的参数，
 * 用于校验用户与大语言模型（LLM）之间的交互内容。
 */
public sealed interface GuardrailRequest<P extends GuardrailRequest<P>>
        permits InputGuardrailRequest, OutputGuardrailRequest {

    /**
     * Retrieves the common parameters that are shared across guardrail checks.
     *
     * @return an instance of {@code GuardrailRequestParams} containing shared parameters such as chat memory,
     *         user message template, and additional variables.
     */
    /**
     * 获取在护栏校验过程中共享的通用参数。
     *
     * @return 包含共享参数的 GuardrailRequestParams 实例，这些参数包括对话上下文、用户消息模板
     *         以及其他附加变量等
     */
    GuardrailRequestParams requestParams();

    /**
     * Recreate this guardrail param with the given input or output text.
     *
     * @param text
     *            The text of the rewritten param.
     *
     * @return A clone of this guardrail params with the given input or output text.
     */
    /**
     * 使用指定的输入或输出文本重新创建此护栏参数对象。
     *
     * @param text 重写后的参数文本内容
     * @return 携带指定输入/输出文本的护栏参数副本（克隆对象）
     */
    P withText(String text);
}
