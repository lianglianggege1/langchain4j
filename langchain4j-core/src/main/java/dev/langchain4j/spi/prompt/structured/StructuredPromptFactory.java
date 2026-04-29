package dev.langchain4j.spi.prompt.structured;

import dev.langchain4j.Internal;
import dev.langchain4j.model.input.Prompt;

/**
 * Represents a factory for structured prompts.
 * 表示一个构建结构化提示词的工厂
 */
@Internal
public interface StructuredPromptFactory {

    /**
     * Converts the given structured prompt to a prompt.
     * 转换给定的结构化提示为提示。
     * @param structuredPrompt the structured prompt.
     * @return the prompt.
     */
    Prompt toPrompt(Object structuredPrompt);
}
