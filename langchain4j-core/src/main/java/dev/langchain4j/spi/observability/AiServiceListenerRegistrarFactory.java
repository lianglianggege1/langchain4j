package dev.langchain4j.spi.observability;

import dev.langchain4j.observability.api.AiServiceListenerRegistrar;
import java.util.function.Supplier;

/**
 * A factory for creating {@link AiServiceListenerRegistrar} instances.
 * 一个创建{@link AiServiceListenerRegistrar}实例的工厂。
 */
public interface AiServiceListenerRegistrarFactory extends Supplier<AiServiceListenerRegistrar> {}
