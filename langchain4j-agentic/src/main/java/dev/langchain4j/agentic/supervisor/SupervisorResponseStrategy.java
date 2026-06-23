package dev.langchain4j.agentic.supervisor;

/**
 * Strategy to decide which response the supervisor agent should return.
 */
/**
 * 用于决定主控智能体应返回何种响应的策略。
 */
public enum SupervisorResponseStrategy {
    /**
     * Use an internal LLM to score the last response and the summarization of the interaction of the supervisor
     * with its sub-agents against the original user request, and return the one with the higher score.
     */
    /**
     * 调用内部大模型，依据用户原始请求，对最终回复以及主控智能体和子智能体的交互摘要进行打分，返回得分更高的内容。
     */
    SCORED,
    /**
     * Return a summarization of the interaction of the supervisor with its sub-agents.
     */
    /**
     * 返回主控智能体与子智能体的交互摘要。
     */
    SUMMARY,
    /**
     * Return only the final response of the last invoked sub-agent (default).
     */
    /**
     * 仅返回最后调用的子智能体的最终响应（默认）。
     */
    LAST
}
