package dev.langchain4j.agent.tool;

import dev.langchain4j.Experimental;

/**
 * Defines the behavior of a tool when {@code dev.langchain4j.service.tool.search.ToolSearchStrategy}
 * is configured for an AI Service.
 * 定义当为 AI 服务配置 dev.langchain4j.service.tool.search.ToolSearchStrategy 时工具的行为。
 *
 * @since 1.12.0
 */
@Experimental
public enum SearchBehavior {

    /**
     * This is the default setting. When {@code ToolSearchStrategy} is configured for an AI Service,
     * all tools configured for that AI Service automatically become searchable.
     * They will not be visible to the LLM until they are found.
     * 这是默认设置。当为 AI 服务配置 ToolSearchStrategy 时，所有为该 AI 服务配置的工具都会自动变为可搜索状态。
     * 在找到这些工具之前，LLM 将无法看到它们。
     */
    SEARCHABLE,

    /**
     * If you want a tool to always be visible to the LLM, use this setting.
     * 如果您希望某个工具始终对 LLM 可见，请使用此设置。
     */
    ALWAYS_VISIBLE;
}
