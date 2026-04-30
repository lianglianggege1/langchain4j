package dev.langchain4j.guardrail;

/**
 * A guardrail is a rule that is applied when interacting with an LLM either to the input (the user message) or to the
 * output of the model to ensure that they are safe and meet the expectations of the model.
 *
 * @param <P>
 *            The type of the {@link GuardrailRequest}
 * @param <R>
 *            The type of the {@link GuardrailResult}
 */
/**
 * 护栏是与大语言模型（LLM）交互时应用的规则，
 * 可作用于输入（用户消息）或模型输出，确保内容安全且符合模型预期。
 *
 * @param <P> {@link GuardrailRequest} 请求类型
 * @param <R> {@link GuardrailResult} 结果类型
 */
public interface Guardrail<P extends GuardrailRequest, R extends GuardrailResult<R>> {
    /**
     * Validate the interaction between the model and the user in one of the two directions.
     *
     * @param request
     *            The parameters of the request or the response to be validated
     *
     * @return The result of the validation
     */
    /**
     * 从两个方向中的一个，校验模型与用户之间的交互内容。
     *
     * @param request 待校验的请求参数或响应内容
     * @return 校验结果
     */
    R validate(P request);
}
