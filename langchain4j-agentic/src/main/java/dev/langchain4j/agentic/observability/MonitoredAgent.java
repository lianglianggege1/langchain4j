package dev.langchain4j.agentic.observability;

/**
 * Interface that agent service interfaces can extend to automatically register
 * an {@link AgentMonitor} as a listener. The monitor is created during agent building
 * and can be retrieved via {@link #agentMonitor()}.
 * 代理服务接口可以扩展的接口，以自动将｛@link AgentMonitor｝注册为侦听器。
 * 监视器是在代理构建过程中创建的，可以通过{@link#agentMonitor（）}进行检索。
 *
 * <p>Example usage:
 *    示例用法：
 * <pre>{@code
 * public interface MyAgent extends MonitoredAgent {
 *
 *     // 执行某些任务
 *     @Agent("Performs some task")
 *     String run(String input);
 * }
 *
 * MyAgent agent = new AgentBuilder<>(MyAgent.class)
 *         .chatModel(model)
 *         .build();
 *
 * AgentMonitor monitor = agent.agentMonitor();
 * }</pre>
 */
public interface MonitoredAgent {

    /**
     * Returns the {@link AgentMonitor} automatically registered for this agent.
     * 返回为此agent自动注册的｛@link AgentMonitor｝。
     */
    AgentMonitor agentMonitor();
}
