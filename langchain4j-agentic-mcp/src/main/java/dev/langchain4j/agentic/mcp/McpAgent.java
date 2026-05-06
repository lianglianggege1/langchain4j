package dev.langchain4j.agentic.mcp;

import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.internal.McpClientBuilder;
import dev.langchain4j.mcp.client.McpClient;

/**
 * Provides type-safe factory methods to create MCP client agent builders.
 * <p>
 * An MCP client agent wraps a single MCP tool as a non-AI agent, allowing it to be composed
 * with other agents in sequences, loops, supervisors, and other workflow patterns.
 *
 * <pre>{@code
 * UntypedAgent agent = McpAgentBuilder
 *         .mcpBuilder(mcpClient)
 *         .toolName("my_tool")
 *         .inputKeys("arg1", "arg2")
 *         .outputKey("result")
 *         .build();
 * }</pre>
 */
/**
 * 提供类型安全的工厂方法，用于创建 MCP 客户端智能体构建器。
 * <p>
 * MCP 客户端智能体将单个 MCP 工具封装为非AI智能体，使其能够与其他智能体
 * 组合成序列、循环、监督器及其他工作流模式。
 *
 * <pre>{@code
 * UntypedAgent agent = McpAgentBuilder
 *         .mcpBuilder(mcpClient)
 *         .toolName("my_tool")
 *         .inputKeys("arg1", "arg2")
 *         .outputKey("result")
 *         .build();
 * }</pre>
 */
public class McpAgent {

    private McpAgent() {}

    /**
     * Creates a builder for an untyped MCP client agent.
     * 创建一个未类型化的 MCP 客户端智能体构建器。
     *
     * @param mcpClient the MCP client instance used to discover and execute the tool
     * @return a new McpClientBuilder instance
     */
    public static McpClientBuilder<UntypedAgent> builder(McpClient mcpClient) {
        return builder(mcpClient, UntypedAgent.class);
    }

    /**
     * Creates a builder for a typed MCP client agent implementing the given agent service interface.
     * 创建一个构建器，用于生成**类型安全的 MCP 客户端智能体**，该智能体实现指定的智能体服务接口。
     *
     * @param mcpClient the MCP client instance used to discover and execute the tool
     * @param agentServiceClass the class of the agent service interface
     * @return a new McpClientBuilder instance
     */
    public static <T> McpClientBuilder<T> builder(McpClient mcpClient, Class<T> agentServiceClass) {
        return new DefaultMcpClientBuilder<>(mcpClient, agentServiceClass);
    }
}
