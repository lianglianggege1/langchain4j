package dev.langchain4j.invocation;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.rag.query.Metadata;
import dev.langchain4j.rag.query.Query;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents arbitrary parameters available during a single AI Service invocation.
 * 表示单次AI服务调用过程中可用的任意参数。
 * {@code InvocationParameters} can be specified when invoking the AI Service:
 * 调用AI服务时可以指定{@code InvocationParameters}
 *
 * <pre>
 * interface Assistant {
 *     String chat(@UserMessage String userMessage, InvocationParameters parameters);
 * }
 *
 * InvocationParameters parameters = InvocationParameters.from(Map.of("userId", "12345"));
 * String response = assistant.chat("What is the weather in London?", parameters);
 * </pre>
 * <p>
 * {@code InvocationParameters} can be accessed within the {@link Tool}-annotated method:
 * <pre>
 * class Tools {
 *     <code>@Tool</code>
 *     String getWeather(String city, InvocationParameters parameters) {
 *         String userId = parameters.get("userId");
 *         UserPreferences preferences = getUserPreferences(userId);
 *         return weatherService.getWeather(city, preferences.temperatureUnits());
 *     }
 * }
 * </pre>
 * <p>
 * In this case, the LLM is not aware of these parameters; they are only visible to LangChain4j and user code.
 * 这这种情况下，LLM不会感知这些参数，它们仅对LangChain4j和用户代码可见。
 * <p>
 * {@code InvocationParameters} can also be accessed within other AI Service components, such as:
 * 也可在其他AI服务组件中访问{@code InvocationParameters}，例如：
 * <pre>
 * - ToolProvider: inside the ToolProviderRequest
 * - ToolProvider: 在工具提供者请求内部
 * - ToolArgumentsErrorHandler and ToolExecutionErrorHandler: inside the ToolErrorContext
 * - ToolArgumentsErrorHandler and ToolExecutionErrorHandler: 在工具错误上下文内部
 * - RAG components: inside the {@link Query} -> {@link Metadata}
 * - RAG components: 在{@link Query}至{@link Metadata}内部
 * </pre>
 * <p>
 * Parameters are stored in a mutable, thread safe {@link Map}.
 * 参数存储在一个**可变且线程安全**的 {@link Map} 中。
 *
 * @since 1.6.0
 */
public class InvocationParameters {

    private final ConcurrentHashMap<String, Object> map;

    public InvocationParameters() {
        this.map = new ConcurrentHashMap<>();
    }

    public InvocationParameters(Map<String, Object> map) {
        ensureNotNull(map, "map");
        this.map = new ConcurrentHashMap<>(map);
    }

    public Map<String, Object> asMap() {
        return map;
    }

    public <T> T get(String key) {
        return (T) map.get(key);
    }

    public <T> T getOrDefault(String key, T defaultValue) {
        return (T) map.getOrDefault(key, defaultValue);
    }

    public <T> void put(String key, T value) {
        map.put(key, value);
    }

    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        InvocationParameters that = (InvocationParameters) object;
        return Objects.equals(map, that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(map);
    }

    @Override
    public String toString() {
        return "InvocationParameters{" + "map=" + map + '}';
    }

    public static InvocationParameters from(String key, Object value) {
        return new InvocationParameters(Map.of(key, value));
    }

    public static InvocationParameters from(Map<String, Object> map) {
        return new InvocationParameters(map);
    }
}
