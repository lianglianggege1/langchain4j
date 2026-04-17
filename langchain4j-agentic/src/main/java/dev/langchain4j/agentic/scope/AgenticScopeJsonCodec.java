package dev.langchain4j.agentic.scope;

import dev.langchain4j.Internal;

/**
 * A codec for serializing and deserializing {@link DefaultAgenticScope} objects to and from JSON.
 * 用于将{@link-DefaultAgentScope}对象序列化到JSON和从JSON反序列化的编解码器。
 */
@Internal
public interface AgenticScopeJsonCodec {

    /**
     * Deserializes a JSON string to a {@link DefaultAgenticScope} object.
     * 将JSON字符串反序列化为{@link DefaultAgentScope}对象。
     * @param json the JSON string.
     * @return the deserialized {@link DefaultAgenticScope} object.
     */
    DefaultAgenticScope fromJson(String json);

    /**
     * Serializes a {@link DefaultAgenticScope} object to a JSON string.
     * 将｛@link DefaultAgentScope｝对象序列化为JSON字符串。
     * @param agenticScope the {@link DefaultAgenticScope} object.
     * @return the serialized JSON string.
     */
    String toJson(DefaultAgenticScope agenticScope);
}
