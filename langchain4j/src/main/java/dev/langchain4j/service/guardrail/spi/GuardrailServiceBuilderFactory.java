package dev.langchain4j.service.guardrail.spi;

import dev.langchain4j.service.guardrail.GuardrailService;

/**
 * A factory for providing instances of {@link GuardrailService.Builder}
 * 提供｛@link GuardrailService.Builder｝实例的工厂
 */
public interface GuardrailServiceBuilderFactory {
    /**
     * Gets an instance of the {@link GuardrailService.Builder}
     * @param aiServiceClass The class of the AI service
     * @return The {@link GuardrailService.Builder} instance
     */
    GuardrailService.Builder getBuilder(Class<?> aiServiceClass);
}
