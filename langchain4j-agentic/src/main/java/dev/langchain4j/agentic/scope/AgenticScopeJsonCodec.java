package dev.langchain4j.agentic.scope;

import dev.langchain4j.Internal;

/**
 * A codec for serializing and deserializing {@link DefaultAgenticScope} objects to and from JSON.
 */
/**
 * 用于实现{@link DefaultAgenticScope}对象与JSON相互序列化、反序列化的编解码器。
 */
@Internal
public interface AgenticScopeJsonCodec {

    /**
     * Deserializes a JSON string to a {@link DefaultAgenticScope} object.
     * @param json the JSON string.
     * @return the deserialized {@link DefaultAgenticScope} object.
     */
    /**
     * 将JSON字符串反序列化为{@link DefaultAgenticScope}对象。
     * @param json JSON字符串
     * @return 反序列化后的{@link DefaultAgenticScope}对象
     */
    DefaultAgenticScope fromJson(String json);

    /**
     * Serializes a {@link DefaultAgenticScope} object to a JSON string.
     * @param agenticScope the {@link DefaultAgenticScope} object.
     * @return the serialized JSON string.
     */
    /**
     * 将{@link DefaultAgenticScope}对象序列化为JSON字符串。
     * @param agenticScope {@link DefaultAgenticScope}对象
     * @return 序列化后的JSON字符串
     */
    String toJson(DefaultAgenticScope agenticScope);

    /**
     * Allows every class under the given package prefix to be deserialized as part of an
     * {@link AgenticScope}'s state.
     *
     * <p>Deserializing arbitrary types by name is how polymorphic deserialization becomes a
     * vulnerability, so a codec that supports it keeps an allowlist. A codec that does not
     * support allowlisting should return {@code false} rather than silently accept everything.
     *
     * @return whether this codec applied the registration.
     */
    default boolean allowPackagePrefix(String packagePrefix) {
        return false;
    }

    /**
     * Allows a single class to be deserialized as part of an {@link AgenticScope}'s state.
     *
     * @return whether this codec applied the registration.
     * @see #allowPackagePrefix(String)
     */
    default boolean allowType(Class<?> type) {
        return false;
    }

    /**
     * Sets the {@link ClassLoader} used to resolve the types named in a serialized
     * {@link AgenticScope}, for applications whose domain types are not visible to the class
     * loader that loaded LangChain4j.
     *
     * @return whether this codec applied the class loader.
     */
    default boolean withClassLoader(ClassLoader classLoader) {
        return false;
    }
}
