package dev.langchain4j.agentic.supervisor;

/**
 * Strategy to decide which response the supervisor agent should return.
 * 决定主管agent应返回何种响应的策略。
 */
public enum SupervisorResponseStrategy {
    /**
     * Use an internal LLM to score the last response and the summarization of the interaction of the supervisor
     * with its sub-agents against the original user request, and return the one with the higher score.
     * 使用内部 LLM 对主管与其子代理的交互的最后响应和摘要与原始用户请求进行评分，并返回得分较高的那个。
     */
    SCORED,
    /**
     * Return a summarization of the interaction of the supervisor with its sub-agents.
     * 返回主管与其子代理交互的摘要。
     */
    SUMMARY,
    /**
     * Return only the final response of the last invoked sub-agent (default).
     * 仅返回最后调用的子代理的最终响应（默认）。
     */
    LAST
}
