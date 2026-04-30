package dev.langchain4j.guardrail.config;

import dev.langchain4j.internal.JacocoIgnoreCoverageGenerated;
import dev.langchain4j.spi.guardrail.config.OutputGuardrailsConfigBuilderFactory;
import java.util.ServiceLoader;

/**
 * Configuration specifically for output guardrails.
 * <p>
 *     Frameworks that extend this library (like Quarkus or Spring) may provide their own implementations of this configuration.
 * </p>
 */
/**
 * 专门用于输出护栏的配置。
 * <p>
 *     扩展该库的框架（如 Quarkus 或 Spring）可提供此配置的自定义实现。
 * </p>
 */
@JacocoIgnoreCoverageGenerated
public interface OutputGuardrailsConfig extends GuardrailsConfig {
    /**
     * Default maximum number of retries for the guardrail.
     * 护栏默认最大重试次数。
     */
    int MAX_RETRIES_DEFAULT = 2;

    /**
     * Configures the maximum number of retries for the guardrail.
     * <p>
     *     Defaults to {@link #MAX_RETRIES_DEFAULT} if not set.
     * </p>
     * Set to {@code 0} to disable retries.
     */
    /**
     * 配置护栏的最大重试次数。
     * <p>
     *     未设置时，默认使用 {@link #MAX_RETRIES_DEFAULT}。
     * </p>
     * 设置为 {@code 0} 可禁用重试功能。
     */
    int maxRetries();

    /**
     * Gets a newBuilder instance for building {@link OutputGuardrailsConfig} instances.
     * @return A {@link OutputGuardrailsConfigBuilder} for building {@link OutputGuardrailsConfig} instances.
     */
    /**
     * 获取用于构建 {@link OutputGuardrailsConfig} 实例的全新构建器实例。
     * @return 用于构建 {@link OutputGuardrailsConfig} 实例的 {@link OutputGuardrailsConfigBuilder}。
     */
    static OutputGuardrailsConfigBuilder builder() {
        return ServiceLoader.load(OutputGuardrailsConfigBuilderFactory.class)
                .findFirst()
                .map(OutputGuardrailsConfigBuilderFactory::get)
                .orElseGet(DefaultOutputGuardrailsConfig::builder);
    }

    /**
     * Builder for {@link OutputGuardrailsConfig} instances.
     * <p>
     *     This is needed so other frameworks (like Quarkus and Spring) can extend the configuration mechanism with their own
     *     implementations while also adhering to the interfaces and specs defined here.
     * </p>
     */
    /**
     * 用于构建 {@link OutputGuardrailsConfig} 实例的构建器。
     * <p>
     *     设计此构建器的目的是：允许其他框架（如 Quarkus 和 Spring）
     *     使用自身实现扩展配置机制，同时遵循此处定义的接口与规范。
     * </p>
     */
    interface OutputGuardrailsConfigBuilder extends GuardrailsConfigBuilder<OutputGuardrailsConfig> {
        /**
         * Sets the maximum number of retries for output guardrails.
         * <p>
         *     Defaults to {@link OutputGuardrailsConfig#maxRetries()} if not set.
         * </p>
         * @param maxRetries The maximum number of retries for output guardrails
         * @return The maximum number of retries for output guardrails
         * @see OutputGuardrailsConfig#maxRetries()
         */
        /**
         * 设置输出护栏的最大重试次数。
         * <p>
         *     未设置时，默认使用 {@link OutputGuardrailsConfig#maxRetries()}。
         * </p>
         * @param maxRetries 输出护栏的最大重试次数
         * @return 当前构建器实例（用于链式调用）
         * @see OutputGuardrailsConfig#maxRetries()
         */
        OutputGuardrailsConfigBuilder maxRetries(int maxRetries);
    }
}
