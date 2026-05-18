package dev.langchain4j.classloading;

import dev.langchain4j.spi.classloading.ClassMetadataProviderFactory;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;

/**
 * Utility class for returning metadata about a class and its methods. Intended to allow downstream frameworks (like Quarkus
 * or Spring) to use their own mechanisms for providing this information.
 */
/**
 * 用于返回类及其方法元数据的工具类。
 * 旨在让下游框架（如 Quarkus 或 Spring）能够使用自身的机制来提供此类信息。
 */
public final class ClassMetadataProvider {
    private static final ReflectionBasedClassMetadataProviderFactory DEFAULT_CLASS_METADATA_PROVIDER_FACTORY =
            new ReflectionBasedClassMetadataProviderFactory();

    private ClassMetadataProvider() {}

    /**
     * Retrieves an implementation of a {@link ClassMetadataProviderFactory}. This method first looks for
     * implementations of the factory via the {@link ServiceLoader}. It filters out the default factory implementation
     * ({@link ReflectionBasedClassMetadataProviderFactory}) to allow for custom implementations provided by external frameworks.
     * If no custom implementations are available, the method returns the default factory.
     *
     * @param <MethodKey> The type of the method key, representing a unique identifier for methods.
     * @return An instance of {@link ClassMetadataProviderFactory} either provided by an external framework or falling back
     *         to the default implementation.
     */
    /**
     * 获取 {@link ClassMetadataProviderFactory} 的实现类。
     * 该方法首先通过 {@link ServiceLoader} 查找工厂的实现类，
     * 并会排除默认工厂实现（{@link ReflectionBasedClassMetadataProviderFactory}），
     * 以便支持外部框架提供的自定义实现。
     * 如果没有找到自定义实现，则返回默认工厂。
     *
     * @param <MethodKey> 方法键类型，用于表示方法的唯一标识
     * @return 由外部框架提供的 {@link ClassMetadataProviderFactory} 实例，若无则返回默认实现
     */
    public static <MethodKey> ClassMetadataProviderFactory<MethodKey> getClassMetadataProviderFactory() {
        return ServiceLoader.load(ClassMetadataProviderFactory.class).stream()
                .filter(provider ->
                        !DEFAULT_CLASS_METADATA_PROVIDER_FACTORY.getClass().equals(provider.type()))
                .map(Provider::get)
                .findFirst()
                .orElse(DEFAULT_CLASS_METADATA_PROVIDER_FACTORY);
    }
}
