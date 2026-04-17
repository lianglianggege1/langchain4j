package dev.langchain4j.agentic.supervisor;

import dev.langchain4j.memory.ChatMemory;

/**
 * Strategy for providing context to the supervisor agent.
 * 为主管代理提供背景信息的策略。
 */
public enum SupervisorContextStrategy {
    /**
     * Use only the supervisors {@link ChatMemory} (default).
     * 仅使用主管 {@link ChatMemory}（默认）。
     */
    CHAT_MEMORY,
    /**
     * Use only a summarization of the interaction of the supervisor with its sub-agents.
     * 仅使用主管与子代理之间的交互的摘要。
     */
    SUMMARIZATION,
    /**
     * Use both the supervisor's {@link ChatMemory} and a summarization of the interaction of the supervisor with its sub-agents.
     * 同时使用主管的 {@link ChatMemory} 和主管与其子代理交互的摘要。
     */
    CHAT_MEMORY_AND_SUMMARIZATION
}
