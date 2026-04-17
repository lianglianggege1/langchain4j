package dev.langchain4j.agentic.internal;

import dev.langchain4j.agentic.agent.MissingArgumentException;
import dev.langchain4j.agentic.scope.AgenticScope;
import java.util.Map;

/**
 * Wraps an existing {@link AgentInvoker} to inject a specific item from a collection
 * into the agent's invocation arguments. Each instance represents one element of a
 * parallel mapper execution, with a unique name, agentId, and outputKey.
 * 包装现有的AgentInvoker，将集合中的特定项注入代理的调用参数中。
 * 每个实例代表并行映射器执行的一个元素，具有唯一的名称、agentId和outputKey。
 */
public class MapperAgentInvoker extends AbstractAgentInvoker {

    // 项目
    private final Object item;
    // 注入的键
    private final String injectionKey;
    // 实例名称
    private final String instanceName;
    // 实例ID
    private final String instanceAgentId;
    // 输出键
    private final String instanceOutputKey;

    public MapperAgentInvoker(AgentInvoker delegate, Object item, int instanceIndex) {
        super(delegate.method(), delegate);
        this.item = item;
        this.injectionKey = delegate.arguments().isEmpty()
                ? null
                : delegate.arguments().get(0).name();
        this.instanceName = delegate.name() + "_" + instanceIndex;
        this.instanceAgentId = delegate.agentId() + "_" + instanceIndex;
        this.instanceOutputKey =
                delegate.outputKey() != null && !delegate.outputKey().isBlank()
                        ? delegate.outputKey() + "_" + instanceIndex
                        : null;
    }

    @Override
    public String name() {
        return instanceName;
    }

    @Override
    public String agentId() {
        return instanceAgentId;
    }

    @Override
    public String outputKey() {
        return instanceOutputKey;
    }

    @Override
    public AgentInvocationArguments toInvocationArguments(AgenticScope agenticScope) throws MissingArgumentException {
        if (injectionKey == null) {
            return AgentUtil.agentInvocationArguments(agenticScope, arguments());
        }
        return AgentUtil.agentInvocationArguments(agenticScope, arguments(), Map.of(injectionKey, item));
    }
}
