package dev.langchain4j.guardrail.config;

/**
 * Builder for {@link GuardrailsConfig} instances.
 * {@link GuardrailsConfig} 实例的构建器。
 * @param <C> The type of configuration being build 正在构建的配置类型
 */
public interface GuardrailsConfigBuilder<C extends GuardrailsConfig> {
    /**
     * Builds the configuration.
     * @return The configuration
     */
    C build();
}
