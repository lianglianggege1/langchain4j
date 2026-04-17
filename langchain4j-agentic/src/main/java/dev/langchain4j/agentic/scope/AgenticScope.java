package dev.langchain4j.agentic.scope;

import dev.langchain4j.agentic.declarative.TypedKey;
import dev.langchain4j.invocation.LangChain4jManaged;
import java.util.List;
import java.util.Map;

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

    // 获取内存id
    Object memoryId();

    // 写入状态
    void writeState(String key, Object value);
    // 写入状态
    <T> void writeState(Class<? extends TypedKey<T>> key, T value);

    // 写入多个状态
    void writeStates(Map<String, Object> newState);

    // 读取状态
    boolean hasState(String key);

    // 读取状态
    boolean hasState(Class<? extends TypedKey<?>> key);

    // 读取状态
    Object readState(String key);

    // 读取状态
    <T> T readState(String key, T defaultValue);

    // 读取状态
    <T> T readState(Class<? extends TypedKey<T>> key);

    // 获取状态
    Map<String, Object> state();

    // 获取上下文
    String contextAsConversation(String... agentNames);

    // 获取上下文
    String contextAsConversation(Object... agents);

    // 获取调用
    List<AgentInvocation> agentInvocations();

    // 获取调用
    List<AgentInvocation> agentInvocations(String agentName);

    // 获取调用
    List<AgentInvocation> agentInvocations(Class<?> agentType);
}
