package dev.langchain4j.agentic.scope;

import dev.langchain4j.agentic.declarative.TypedKey;
import dev.langchain4j.agentic.internal.PendingResponse;
import dev.langchain4j.invocation.LangChain4jManaged;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The AgenticScope class represents a common environment where agents belonging to the same
 * agentic system can share their state.
 * It maintains the state of the computation, tracks agent invocations, and provides
 * methods to allow agents to interact with the shared state.
 * AgentScope类表示一个公共环境，属于同一代理系统的代理可以在其中共享其状态。
 * 它维护计算的状态，跟踪代理调用，并提供允许代理与共享状态交互的方法。
 * <p>
 * Agents can register their calls, and the context of interactions is stored for later retrieval.
 * The class also provides methods to read and write state, manage agent invocations, and retrieve
 * the context as a conversation.
 * 代理可以注册他们的调用，并存储交互的上下文以供以后检索。
 * 该类还提供了读取和写入状态、管理代理调用以及作为对话检索上下文的方法。
 */
public interface AgenticScope extends LangChain4jManaged {

    /**
     * Returns the unique memory identifier for this scope. This ID is used to associate the scope
     * with a specific conversation or session, and to look up persisted scopes from a store.
     *
     * @return the memory identifier
     */
    /**
     * 获取当前作用域唯一内存标识。该标识用于关联作用域与指定对话/会话，
     * 并从存储介质中查询已持久化的作用域数据。
     *
     * @return 内存标识
     */
    Object memoryId();

    /**
     * Writes a value into the shared state under the given key.
     * If the value is {@code null}, the key is removed from the state.
     *
     * @param key   the state key
     * @param value the value to store, or {@code null} to remove the key
     */
    /**
     * 向共享状态中写入一个值，使用给定的键。
     * 如果值为 {@code null}，则从状态中移除该键。
     *
     * @param key   状态键
     * @param value 要存储的值，或 {@code null} 以移除该键
     */
    void writeState(String key, Object value);

    /**
     * Writes a value into the shared state using a strongly typed key.
     * The key's name is derived from the {@link TypedKey} class.
     *
     * @param key   the typed key class
     * @param value the value to store
     * @param <T>   the type of the value
     */
    /**
     * 向共享状态中写入一个值，使用强类型键。
     * 键的名称从 {@link TypedKey} 类派生。
     *
     * @param key   键类型类
     * @param value 要存储的值
     * @param <T>   值的类型
     */
    <T> void writeState(Class<? extends TypedKey<T>> key, T value);

    /**
     * Writes multiple key-value pairs into the shared state at once.
     *
     * @param newState a map of key-value pairs to store
     */
    /**
     * 一次写入共享状态中的多个键值对。
     *
     * @param newState 要存储的键值对映射
     */
    void writeStates(Map<String, Object> newState);

    /**
     * Checks whether the shared state contains a non-blank value for the given key.
     *
     * @param key the state key
     * @return {@code true} if the key exists and its value is non-null (and non-blank for strings)
     */
    /**
     * 检查共享状态中是否存在给定键的非空值。
     *
     * @param key 状态键
     * @return 如果键存在且其值非空（对于字符串，非空白），则返回 {@code true}
     */
    boolean hasState(String key);

    /**
     * Checks whether the shared state contains a non-blank value for the given typed key.
     *
     * @param key the typed key class
     * @return {@code true} if the key exists and its value is non-null (and non-blank for strings)
     */
    /**
     * 检查共享状态中是否存在给定强类型键的非空值。
     *
     * @param key 强类型键类
     * @return 如果键存在且其值非空（对于字符串，非空白），则返回 {@code true}
     */
    boolean hasState(Class<? extends TypedKey<?>> key);

    /**
     * Reads the value associated with the given key from the shared state.
     * If the value is a {@link dev.langchain4j.agentic.internal.DelayedResponse}, this method
     * blocks until the response is available.
     *
     * @param key the state key
     * @return the value, or {@code null} if the key is not present
     */
    /**
     * 从共享状态中读取与指定键关联的值。
     * 如果该值为 {@link dev.langchain4j.agentic.internal.DelayedResponse} 类型，
     * 此方法会阻塞等待，直到响应结果可用。
     *
     * @param key 状态键
     * @return 对应的值；若键不存在，则返回 {@code null}
     */
    Object readState(String key);

    /**
     * Reads the value associated with the given key from the shared state,
     * returning a default value if the key is not present.
     * If the value is a {@link dev.langchain4j.agentic.internal.DelayedResponse}, this method
     * blocks until the response is available.
     *
     * @param key          the state key
     * @param defaultValue the value to return if the key is not present
     * @param <T>          the type of the value
     * @return the value, or {@code defaultValue} if the key is not present
     */
    /**
     * 从共享状态中读取与指定键关联的值，如果键不存在则返回默认值。
     * 如果该值为 {@link dev.langchain4j.agentic.internal.DelayedResponse} 类型，
     * 此方法会阻塞等待，直到响应结果可用。
     *
     * @param key          状态键
     * @param defaultValue 默认值
     * @param <T>          值的类型
     * @return 对应的值；若键不存在，则返回 {@code defaultValue}
     */
    <T> T readState(String key, T defaultValue);

    /**
     * Reads the value associated with the given typed key from the shared state.
     * The key's name and default value are derived from the {@link TypedKey} class.
     * If the value is a {@link dev.langchain4j.agentic.internal.DelayedResponse}, this method
     * blocks until the response is available.
     *
     * @param key the typed key class
     * @param <T> the type of the value
     * @return the value, or the key's default value if not present
     */
    /**
     * 从共享状态中读取与指定强类型键关联的值。
     * 键的名称和默认值从 {@link TypedKey} 类派生。
     * 如果该值为 {@link dev.langchain4j.agentic.internal.DelayedResponse} 类型，
     * 此方法会阻塞等待，直到响应结果可用。
     *
     * @param key 强类型键类
     * @param <T> 值的类型
     * @return 对应的值；若键不存在，则返回 {@code null}
     */
    <T> T readState(Class<? extends TypedKey<T>> key);

    /**
     * Returns a live view of the entire shared state map.
     * Modifications to this map are reflected in the scope's state.
     *
     * @return the mutable state map
     */
    /**
     * 返回共享状态映射的实时视图。
     * 对此映射的修改将反映在作用域的状态中。
     *
     * @return 可修改的状态映射
     */
    Map<String, Object> state();

    /**
     * Returns the conversation context as a human-readable string, optionally filtered
     * by agent names. Each entry shows the user message and the agent's response.
     *
     * @param agentNames the names of the agents to include; if empty, all agents are included
     * @return the conversation context as a formatted string
     */
    /**
     * 返回代理调用的对话上下文作为人类可读的字符串，可选地按代理名称进行过滤。
     * 每个条目显示用户消息和代理的响应。
     *
     * @param agentNames 要包含的代理名称；为空则包含所有代理
     * @return 作为格式化字符串的对话上下文
     */
    String contextAsConversation(String... agentNames);

    /**
     * Returns the conversation context as a human-readable string, optionally filtered
     * by agent instances. Each entry shows the user message and the agent's response.
     *
     * @param agents the agent instances to include; if empty, all agents are included
     * @return the conversation context as a formatted string
     */
    /**
     * 返回代理调用的对话上下文作为人类可读的字符串，可选地按代理实例进行过滤。
     * 每个条目显示用户消息和代理的响应。
     *
     * @param agents 要包含的代理实例；为空则包含所有代理
     * @return 作为格式化字符串的对话上下文
     */
    String contextAsConversation(Object... agents);

    /**
     * Returns all agent invocations recorded in this scope, in execution order.
     *
     * @return an unmodifiable list of all agent invocations
     */
    /**
     * 返回此作用域中记录的所有代理调用，按执行顺序排列。
     *
     * @return 一个不可修改的代理调用列表
     */
    List<AgentInvocation> agentInvocations();

    /**
     * 返回指定代理名称的所有代理调用。
     *
     * @param agentName 代理名称
     * @return 匹配代理名称的代理调用列表
     */
    /**
     * Returns all agent invocations for the agent with the given name.
     *
     * @param agentName the name of the agent
     * @return a list of invocations matching the agent name
     */
    List<AgentInvocation> agentInvocations(String agentName);

    /**
     * Returns all agent invocations for agents of the given type.
     *
     * @param agentType the class of the agent
     * @return a list of invocations matching the agent type
     */
    /**
     * 返回指定类型智能体的全部调用记录。
     *
     * @param agentType 智能体类型Class对象
     * @return 匹配该智能体类型的调用记录列表
     */
    List<AgentInvocation> agentInvocations(Class<?> agentType);

    /**
     * Completes a {@link PendingResponse} stored in this scope's state.
     * This is typically called by an external system (e.g., a REST endpoint) to provide
     * a human's response after a process restart or when using a polling/event-driven model.
     *
     * @param responseId the unique identifier of the pending response
     * @param value the value to complete the response with
     * @return {@code true} if a matching pending response was found and completed
     */
    /**
     * 完成此作用域状态中存储的 {@link PendingResponse}。
     * 这通常由外部系统（例如，REST端点）调用，以提供人类的响应，
     * 在进程重启后或使用轮询/事件驱动模型时。
     *
     * @param responseId 唯一标识待处理响应的字符串
     * @param value      要完成响应的值
     * @return {@code true} 如果找到并完成匹配的待处理响应
     */
    default boolean completePendingResponse(String responseId, Object value) {
        return false;
    }

    /**
     * Returns the identifiers of all {@link PendingResponse} instances stored in this scope's state
     * that have not yet been completed.
     *
     * @return a set of pending response identifiers
     */
    /**
     * 返回此作用域状态中存储的所有 {@link PendingResponse} 实例的标识符，
     * 这些实例尚未完成。
     *
     * @return 唯一标识符的集合
     */
    default Set<String> pendingResponseIds() {
        return Set.of();
    }

    /**
     * Stores non-serializable execution context in this scope, keyed by name.
     * <p>
     * This allows custom {@link dev.langchain4j.agentic.planner.Planner} implementations to store
     * runtime objects that are needed during execution but should not be persisted.
     * The execution context is stored in a {@code transient} map and will not be serialized.
     * <p>
     * This is distinct from {@link #writeState(String, Object)}, which is for serializable
     * agent interaction data that forms part of the conversation state.
     *
     * @param key     the key to use for this context (must not be {@code null})
     * @param context the execution context instance to store (must not be {@code null})
     * @throws IllegalArgumentException if {@code key} or {@code context} is {@code null}
     */
    /**
     * 以名称为键，将不可序列化的执行上下文存储到当前作用域中。
     * <p>
     * 允许自定义 {@link dev.langchain4j.agentic.planner.Planner} 实现类存储
     * 执行时所需、但无需持久化的运行时对象。
     * 执行上下文存储在 {@code transient} 映射中，**不会被序列化**。
     * <p>
     * 该方法与 {@link #writeState(String, Object)} 不同：
     * writeState 用于存储可序列化、构成对话状态的智能体交互数据。
     *
     * @param key     上下文键（不能为 {@code null}）
     * @param context 待存储的执行上下文实例（不能为 {@code null}）
     * @throws IllegalArgumentException 当 {@code key} 或 {@code context} 为 {@code null} 时抛出
     */
    void writeExecutionContext(String key, Object context);

    /**
     * Stores non-serializable execution context in this scope, using the class name as the key.
     * <p>
     * This is a convenience method that delegates to {@link #writeExecutionContext(String, Object)}
     * using the fully qualified class name as the key.
     *
     * @param type    the class type to use as the key for this context
     * @param context the execution context instance to store
     * @throws IllegalArgumentException if {@code context} is {@code null}
     */
    /**
     * 使用类名作为键，将不可序列化的执行上下文存储到当前作用域中。
     * <p>
     * 这是一个便捷方法，会使用类的全限定名作为键，委托调用 {@link #writeExecutionContext(String, Object)}。
     *
     * @param type    用作上下文键的类类型
     * @param context 待存储的执行上下文实例
     * @throws IllegalArgumentException 当 {@code context} 为 {@code null} 时抛出
     */
    default void writeExecutionContext(Class<?> type, Object context) {
        writeExecutionContext(type.getName(), context);
    }

    /**
     * Retrieves non-serializable execution context from this scope by key.
     * <p>
     * Returns execution context previously stored via {@link #writeExecutionContext(String, Object)}.
     *
     * @param key the key used to store the execution context
     * @return the execution context instance previously stored for this key, or {@code null} if none exists
     */
    /**
     * 根据键从当前作用域中获取不可序列化的执行上下文。
     * <p>
     * 返回通过 {@link #writeExecutionContext(String, Object)} 预先存储的执行上下文。
     *
     * @param key 存储执行上下文时使用的键
     * @return 该键对应的执行上下文实例；若不存在则返回 {@code null}
     */
    Object executionContext(String key);

    /**
     * Retrieves non-serializable execution context from this scope by key with type-safe casting.
     * <p>
     * Returns execution context previously stored via {@link #writeExecutionContext(String, Object)}.
     *
     * @param key  the key used to store the execution context
     * @param type the expected type of the execution context (for type-safe casting)
     * @param <T>  the type of the execution context
     * @return the execution context instance previously stored for this key, or {@code null} if none exists
     * @throws ClassCastException if the stored context cannot be cast to the expected type
     */
    /**
     * 根据键从当前作用域中获取不可序列化的执行上下文，并进行类型安全的转换。
     * <p>
     * 返回通过 {@link #writeExecutionContext(String, Object)} 预先存储的执行上下文。
     *
     * @param key  存储执行上下文时使用的键
     * @param type 预期的执行上下文类型（用于类型安全转换）
     * @param <T>  执行上下文类型
     * @return 该键对应的执行上下文实例；若不存在则返回 {@code null}
     * @throws ClassCastException 如果存储的上下文不能转换为预期类型
     */
    <T> T executionContextAs(String key, Class<T> type);

    /**
     * Retrieves non-serializable execution context from this scope by type.
     * <p>
     * This is a convenience method that delegates to {@link #executionContextAs(String, Class)}
     * using the fully qualified class name as the key.
     *
     * @param type the class type used as the key for the execution context
     * @param <T>  the type of the execution context
     * @return the execution context instance previously stored for this type, or {@code null} if none exists
     */
    /**
     * 根据类类型从当前作用域中获取不可序列化的执行上下文，并进行类型安全的转换。
     * <p>
     * 这是一个便捷方法，会使用类的全限定名作为键，委托调用 {@link #executionContextAs(String, Class)}。
     *
     * @param type 用作键的类类型
     * @param <T>  执行上下文类型
     * @return 该类型对应的执行上下文实例；若不存在则返回 {@code null}
     */
    default <T> T executionContextAs(Class<T> type) {
        return executionContextAs(type.getName(), type);
    }
}
