package dev.langchain4j.agentic.scope;

import java.util.ServiceLoader;

/**
 * Utility class for serializing AgenticScope objects to JSON format.
 * 用于将AgentScope对象序列化为JSON格式的实用程序类。
 */
public class AgenticScopeSerializer {

    static final AgenticScopeJsonCodec CODEC = loadCodec();

    private AgenticScopeSerializer() { }

    private static AgenticScopeJsonCodec loadCodec() {
        for (AgenticScopeJsonCodec codec : ServiceLoader.load(AgenticScopeJsonCodec.class)) {
            return codec;
        }
        return new JacksonAgenticScopeJsonCodec();
    }

    /**
     * Serializes a AgenticScope into a JSON string.
     * 将AgentScope序列化为JSON字符串。
     *
     * @param agenticScope AgenticScope to be serialized.
     * @return A JSON string with the contents of the AgenticScope.
     * @see AgenticScopeSerializer For details on deserialization.
     */
    public static String toJson(DefaultAgenticScope agenticScope) {
        return CODEC.toJson(agenticScope);
    }

    /**
     * Deserializes a JSON string into a AgenticScope object.
     * 将JSON字符串反序列化为AgentScope对象。
     *
     * @param json JSON string to be deserialized.
     * @return A AgenticScope object constructed from the JSON.
     * @see AgenticScopeSerializer For details on serialization.
     */
    public static DefaultAgenticScope fromJson(String json) {
        return CODEC.fromJson(json);
    }
}
