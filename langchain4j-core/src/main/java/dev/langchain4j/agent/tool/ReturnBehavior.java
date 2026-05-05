package dev.langchain4j.agent.tool;

import dev.langchain4j.Experimental;

/**
 * Per-tool setting controlling what happens with a tool's result after execution.
 * <p>
 * In the default case ({@link #TO_LLM}), every tool result is appended to the conversation
 * and sent back to the LLM for further processing — AI Service execution loop runs another turn.
 * With {@link #IMMEDIATE} or {@link #IMMEDIATE_IF_LAST}, certain tool-call patterns
 * short-circuit the loop and return tool call result(s) directly to the caller inside the
 * {@code dev.langchain4j.service.Result}.
 * <p>
 * <b>Immediate-return rule</b> applied after each LLM response: AI Service execution loop
 * returns immediately iff
 * <ol>
 *   <li>no tool in the response errored, AND</li>
 *   <li>either the last tool is {@link #IMMEDIATE_IF_LAST}, or every tool is
 *       {@link #IMMEDIATE}/{@link #IMMEDIATE_IF_LAST} (no {@link #TO_LLM} mixed in).</li>
 * </ol>
 * <p>
 * <b>Immediate return vs. reprocess for every order of behaviors</b> (no errors), as
 * exercised by {@code ReturnBehaviorCombinationsTest}:
 * <pre>
 *   [TO_LLM]                                 -&gt; reprocess
 *   [TO_LLM, TO_LLM]                         -&gt; reprocess
 *   [IMMEDIATE]                              -&gt; return immediately
 *   [IMMEDIATE, IMMEDIATE]                   -&gt; return immediately
 *   [TO_LLM, IMMEDIATE]                      -&gt; reprocess
 *   [IMMEDIATE, TO_LLM]                      -&gt; reprocess
 *   [IMMEDIATE_IF_LAST]                      -&gt; return immediately
 *   [IMMEDIATE_IF_LAST, IMMEDIATE_IF_LAST]   -&gt; return immediately
 *   [TO_LLM, IMMEDIATE_IF_LAST]              -&gt; return immediately
 *   [IMMEDIATE_IF_LAST, TO_LLM]              -&gt; reprocess
 *   [IMMEDIATE, IMMEDIATE_IF_LAST]           -&gt; return immediately
 *   [IMMEDIATE_IF_LAST, IMMEDIATE]           -&gt; return immediately
 *   [TO_LLM, IMMEDIATE, IMMEDIATE_IF_LAST]   -&gt; return immediately
 *   [TO_LLM, IMMEDIATE_IF_LAST, IMMEDIATE]   -&gt; reprocess
 *   [IMMEDIATE, TO_LLM, IMMEDIATE_IF_LAST]   -&gt; return immediately
 *   [IMMEDIATE, IMMEDIATE_IF_LAST, TO_LLM]   -&gt; reprocess
 *   [IMMEDIATE_IF_LAST, TO_LLM, IMMEDIATE]   -&gt; reprocess
 *   [IMMEDIATE_IF_LAST, IMMEDIATE, TO_LLM]   -&gt; reprocess
 * </pre>
 * <p>
 * <b>Any tool error forces reprocess</b> regardless of behaviors, so the LLM
 * can react to the error on the next turn.
 * <p>
 * {@link #IMMEDIATE} and {@link #IMMEDIATE_IF_LAST} are only allowed on AI services declaring
 * {@code dev.langchain4j.service.Result} as their return type. Using either on a service with
 * a different return type causes an {@code IllegalConfigurationException} the first time an
 * immediate return would occur.
 */
/**
 * 单个工具的设置项，用于控制工具执行完毕后其结果的处理方式。
 * <p>
 * 默认情况（{@link #TO_LLM}）下，所有工具结果都会追加到对话中，并返回给大语言模型进行后续处理——
 * AI服务执行循环会继续运行下一轮。
 * 使用 {@link #IMMEDIATE} 或 {@link #IMMEDIATE_IF_LAST} 时，特定的工具调用模式会中断循环，
 * 并将工具调用结果直接返回给 {@code dev.langchain4j.service.Result} 内的调用方。
 * <p>
 * 每次大语言模型响应后应用<b>立即返回规则</b>：AI服务执行循环会立即返回，当且仅当
 * <ol>
 *   <li>响应中没有工具发生错误，并且</li>
 *   <li>最后一个工具为 {@link #IMMEDIATE_IF_LAST}，或者所有工具均为
 *       {@link #IMMEDIATE}/{@link #IMMEDIATE_IF_LAST}（未混入 {@link #TO_LLM}）。</li>
 * </ol>
 * <p>
 * <b>立即返回 与 重新处理的所有行为组合对比</b>（无错误场景），
 * 由 {@code ReturnBehaviorCombinationsTest} 验证：
 * <pre>
 *   [TO_LLM]                                 -&gt; 重新处理
 *   [TO_LLM, TO_LLM]                         -&gt; 重新处理
 *   [IMMEDIATE]                              -&gt; 立即返回
 *   [IMMEDIATE, IMMEDIATE]                   -&gt; 立即返回
 *   [TO_LLM, IMMEDIATE]                      -&gt; 重新处理
 *   [IMMEDIATE, TO_LLM]                      -&gt; 重新处理
 *   [IMMEDIATE_IF_LAST]                      -&gt; 立即返回
 *   [IMMEDIATE_IF_LAST, IMMEDIATE_IF_LAST]   -&gt; 立即返回
 *   [TO_LLM, IMMEDIATE_IF_LAST]              -&gt; 立即返回
 *   [IMMEDIATE_IF_LAST, TO_LLM]              -&gt; 重新处理
 *   [IMMEDIATE, IMMEDIATE_IF_LAST]           -&gt; 立即返回
 *   [IMMEDIATE_IF_LAST, IMMEDIATE]           -&gt; 立即返回
 *   [TO_LLM, IMMEDIATE, IMMEDIATE_IF_LAST]   -&gt; 立即返回
 *   [TO_LLM, IMMEDIATE_IF_LAST, IMMEDIATE]   -&gt; 重新处理
 *   [IMMEDIATE, TO_LLM, IMMEDIATE_IF_LAST]   -&gt; 立即返回
 *   [IMMEDIATE, IMMEDIATE_IF_LAST, TO_LLM]   -&gt; 重新处理
 *   [IMMEDIATE_IF_LAST, TO_LLM, IMMEDIATE]   -&gt; 重新处理
 *   [IMMEDIATE_IF_LAST, IMMEDIATE, TO_LLM]   -&gt; 重新处理
 * </pre>
 * <p>
 * <b>任何工具错误都会强制重新处理</b>，无论设置何种行为，
 * 以便大语言模型在下一轮中对错误做出响应。
 * <p>
 * {@link #IMMEDIATE} 和 {@link #IMMEDIATE_IF_LAST} 仅允许在返回类型为
 * {@code dev.langchain4j.service.Result} 的AI服务上使用。
 * 在返回类型不同的服务上使用二者之一，会在首次触发立即返回时抛出 {@code IllegalConfigurationException} 异常。
 */
@Experimental
public enum ReturnBehavior {

    /**
     * The tool result is sent back to the LLM for further processing — AI Service execution loop
     * continues. This is the default behavior.
     */
    /**
     * 工具结果会被送回给大语言模型进行后续处理——AI服务的执行循环将继续。
     * 这是默认行为。
     */
    TO_LLM,

    /**
     * Returns AI Service execution loop result(s) to the caller (inside the
     * {@code dev.langchain4j.service.Result}) when every tool in the response is
     * {@code IMMEDIATE} or {@link #IMMEDIATE_IF_LAST}, and no tool errored.
     * <p>
     * A single {@link #TO_LLM} tool anywhere in the response prevents the immediate return and
     * the loop runs another turn. Errors in any tool also prevent the immediate return so the
     * LLM can react to the error.
     * <p>
     * Examples (full matrix in the {@link ReturnBehavior class-level Javadoc}):
     * <pre>
     *   [IMMEDIATE]                              -&gt; return immediately
     *   [IMMEDIATE, IMMEDIATE]                   -&gt; return immediately
     *   [IMMEDIATE, IMMEDIATE_IF_LAST]           -&gt; return immediately   (every tool is IMMEDIATE/IMMEDIATE_IF_LAST)
     *   [IMMEDIATE_IF_LAST, IMMEDIATE]           -&gt; return immediately   (every tool is IMMEDIATE/IMMEDIATE_IF_LAST)
     *   [TO_LLM, IMMEDIATE]                      -&gt; reprocess
     *   [IMMEDIATE, TO_LLM]                      -&gt; reprocess
     * </pre>
     * <p>
     * Only allowed on AI services returning {@code dev.langchain4j.service.Result}.
     */
    /**
     * 当响应中的所有工具均为 {@code IMMEDIATE} 或 {@link #IMMEDIATE_IF_LAST} 类型，
     * 且无任何工具执行出错时，将AI服务执行循环的结果返回给调用方（封装在
     * {@code dev.langchain4j.service.Result} 中）。
     * <p>
     * 只要响应中存在任意一个 {@link #TO_LLM} 类型的工具，就会阻止立即返回，
     * 执行循环会继续运行下一轮。任意工具发生错误同样会阻止立即返回，
     * 以便大语言模型对错误进行处理。
     * <p>
     * 示例（完整组合见 {@link ReturnBehavior 类级别的文档}）：
     * <pre>
     *   [IMMEDIATE]                              -&gt; 立即返回
     *   [IMMEDIATE, IMMEDIATE]                   -&gt; 立即返回
     *   [IMMEDIATE, IMMEDIATE_IF_LAST]           -&gt; 立即返回   （所有工具均为 IMMEDIATE/IMMEDIATE_IF_LAST）
     *   [IMMEDIATE_IF_LAST, IMMEDIATE]           -&gt; 立即返回   （所有工具均为 IMMEDIATE/IMMEDIATE_IF_LAST）
     *   [TO_LLM, IMMEDIATE]                      -&gt; 重新处理
     *   [IMMEDIATE, TO_LLM]                      -&gt; 重新处理
     * </pre>
     * <p>
     * 仅允许在返回类型为 {@code dev.langchain4j.service.Result} 的AI服务上使用。
     */
    IMMEDIATE,

    /**
     * Returns AI Service execution loop result(s) to the caller when this tool is positioned
     * <b>last</b> in the LLM response. Intended for tools the LLM uses to explicitly close an
     * action sequence — placing the tool last is the LLM's signal that no further LLM processing
     * is needed.
     * <p>
     * Also counts toward the all-immediate rule of {@link #IMMEDIATE}: a response made up only
     * of {@code IMMEDIATE} and/or {@code IMMEDIATE_IF_LAST} tools returns immediately regardless
     * of which one is last.
     * <p>
     * If positioned anywhere other than last, AND any other tool in the response is
     * {@link #TO_LLM}, the loop runs another turn — all tool call results (including this one)
     * are sent to the LLM. Errors in any tool also prevent the immediate return so the LLM can
     * react to the error.
     * <p>
     * Examples (full matrix in the {@link ReturnBehavior class-level Javadoc}):
     * <pre>
     *   [IMMEDIATE_IF_LAST]                      -&gt; return immediately
     *   [TO_LLM, IMMEDIATE_IF_LAST]              -&gt; return immediately   (last is IMMEDIATE_IF_LAST)
     *   [IMMEDIATE_IF_LAST, IMMEDIATE_IF_LAST]   -&gt; return immediately
     *   [IMMEDIATE_IF_LAST, IMMEDIATE]           -&gt; return immediately   (every tool is IMMEDIATE/IMMEDIATE_IF_LAST)
     *   [IMMEDIATE_IF_LAST, TO_LLM]              -&gt; reprocess  (not last; TO_LLM disqualifies all-immediate rule)
     *   [TO_LLM, IMMEDIATE_IF_LAST, IMMEDIATE]   -&gt; reprocess  (not last; TO_LLM disqualifies all-immediate rule)
     * </pre>
     * <p>
     * Only allowed on AI services returning {@code dev.langchain4j.service.Result}.
     */
    /**
     * 当该工具在大语言模型响应中处于**最后一个**位置时，
     * 将AI服务执行循环的结果返回给调用方。
     * 专为大语言模型用于显式结束操作序列的工具设计——将该工具放在最后，
     * 是大语言模型表示无需进一步处理的信号。
     * <p>
     * 同时适用于 {@link #IMMEDIATE} 的全立即返回规则：
     * 若响应仅由 {@code IMMEDIATE} 和/或 {@code IMMEDIATE_IF_LAST} 工具组成，
     * 无论哪个工具排在最后，都会立即返回。
     * <p>
     * 如果该工具不处于最后位置，且响应中存在任意 {@link #TO_LLM} 工具，
     * 执行循环将继续下一轮——所有工具调用结果（包括当前工具）都会发送给大语言模型。
     * 任意工具发生错误同样会阻止立即返回，以便大语言模型处理错误。
     * <p>
     * 示例（完整组合见 {@link ReturnBehavior 类级别的文档}）：
     * <pre>
     *   [IMMEDIATE_IF_LAST]                      -&gt; 立即返回
     *   [TO_LLM, IMMEDIATE_IF_LAST]              -&gt; 立即返回   （最后一个是 IMMEDIATE_IF_LAST）
     *   [IMMEDIATE_IF_LAST, IMMEDIATE_IF_LAST]   -&gt; 立即返回
     *   [IMMEDIATE_IF_LAST, IMMEDIATE]           -&gt; 立即返回   （所有工具均为 IMMEDIATE/IMMEDIATE_IF_LAST）
     *   [IMMEDIATE_IF_LAST, TO_LLM]              -&gt; 重新处理  （不处于最后；TO_LLM 不满足全立即返回规则）
     *   [TO_LLM, IMMEDIATE_IF_LAST, IMMEDIATE]   -&gt; 重新处理  （不处于最后；TO_LLM 不满足全立即返回规则）
     * </pre>
     * <p>
     * 仅允许在返回类型为 {@code dev.langchain4j.service.Result} 的AI服务上使用。
     */
    IMMEDIATE_IF_LAST;
}
