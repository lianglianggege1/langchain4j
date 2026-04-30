package dev.langchain4j.guardrail.config;

import dev.langchain4j.spi.guardrail.config.InputGuardrailsConfigBuilderFactory;
import java.util.ServiceLoader;

/**
 * Configuration specifically for input guardrails.
 * 专门用于输入护栏的配置。
 * <p>
 *     Frameworks that extend this library (like Quarkus or Spring) may provide their own implementations of this configuration.
 *     扩展该库的框架（如 Quarkus、Spring）可自行提供此配置的实现。
 * </p>
 */
public interface InputGuardrailsConfig extends GuardrailsConfig {
    /**
     * Gets a builder instance for building {@link InputGuardrailsConfig} instances.
     * 获取用于构建 {@link InputGuardrailsConfig} 实例的构建器对象。
     * @return A {@link InputGuardrailsConfigBuilder} for building {@link InputGuardrailsConfig} instances.
     * 用于构建 {@link InputGuardrailsConfig} 实例的 {@link InputGuardrailsConfigBuilder} 构建器。
     */
    static InputGuardrailsConfigBuilder builder() {
        return ServiceLoader.load(InputGuardrailsConfigBuilderFactory.class)
                .findFirst()
                .map(InputGuardrailsConfigBuilderFactory::get)
                .orElseGet(DefaultInputGuardrailsConfig::builder);
    }

    /**
     * Builder for {@link InputGuardrailsConfig} instances.
     * <p>
     *     This is needed so other frameworks (like Quarkus and Spring) can extend the configuration mechanism with their own
     *     implementations while also adhering to the interfaces and specs defined here.
     * </p>
     */
    /**
     * 这一设计是必要的，以便其他框架（如 Quarkus 和 Spring）能够
     * 使用自身的实现扩展配置机制，同时遵循此处定义的接口与规范。
     */
    interface InputGuardrailsConfigBuilder extends GuardrailsConfigBuilder<InputGuardrailsConfig> {}
}
