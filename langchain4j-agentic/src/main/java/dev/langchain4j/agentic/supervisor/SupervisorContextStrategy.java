package dev.langchain4j.agentic.supervisor;

import dev.langchain4j.memory.ChatMemory;

/**
 * Strategy for providing context to the supervisor agent.
 */
/**
 * 为主控智能体提供上下文的策略。
 */
public enum SupervisorContextStrategy {
    /**
     * Use only the supervisors {@link ChatMemory} (default).
     */
    /**
     * 仅使用主控智能体的{@link ChatMemory}（默认）。
     */
    CHAT_MEMORY,
    /**
     * Use only a summarization of the interaction of the supervisor with its sub-agents.
     */
    /**
     * 仅使用主控智能体与子智能体交互内容的摘要。
     */
    SUMMARIZATION,
    /**
     * Use both the supervisor's {@link ChatMemory} and a summarization of the interaction of the supervisor with its sub-agents.
     */
    /**
     * 同时使用主控智能体的{@link ChatMemory}及其与子智能体的交互摘要。
     */
    CHAT_MEMORY_AND_SUMMARIZATION
}
