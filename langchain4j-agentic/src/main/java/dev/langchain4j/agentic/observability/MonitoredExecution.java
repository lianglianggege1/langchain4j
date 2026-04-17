package dev.langchain4j.agentic.observability;

import dev.langchain4j.agentic.planner.AgenticSystemTopology;
import dev.langchain4j.agentic.scope.AgenticScope;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a monitored execution of an agentic system, tracking the top-level agent invocation
 * and any other nested invocations, along with all the invocation currently in progress and
 * any errors that occur during execution.
 * 表示代理系统的受监视执行，跟踪顶级代理调用和任何其他嵌套调用，以及当前正在进行的所有调用和执行过程中发生的任何错误。
 */
public class MonitoredExecution {

    private final AgentInvocation topLevelInvocations;

    private final Map<Object, AgentInvocation> ongoingInvocations = new ConcurrentHashMap<>();

    private AgentInvocationError agentInvocationError;

    MonitoredExecution(AgentRequest firstAgentRequest) {
        this.topLevelInvocations = new AgentInvocation(firstAgentRequest);
        ongoingInvocations.put(firstAgentRequest.agentId(), this.topLevelInvocations);
    }

    // 在代理执行之前
    void beforeAgentInvocation(AgentRequest agentRequest) {
        AgentInvocation parentInvocation = ongoingInvocations.get(agentRequest.agent().parent().agentId());
        if (parentInvocation == null) {
            throw new IllegalStateException("No ongoing parent invocation found for agent ID: " + agentRequest.agent().parent().agentId());
        }
        AgentInvocation newInvocation = new AgentInvocation(agentRequest);

        if (parentInvocation.agent().topology() == AgenticSystemTopology.LOOP) {
            String agentId = agentRequest.agentId();
            int count = 0;
            for (AgentInvocation existing : parentInvocation.nestedInvocations()) {
                if (agentId.equals(existing.agent().agentId())) {
                    count++;
                }
            }
            newInvocation.setIterationIndex(count);
        }

        parentInvocation.addNestedInvocation(newInvocation);
        ongoingInvocations.put(agentRequest.agentId(), newInvocation);
    }

    // 在代理执行之后
    void afterAgentInvocation(AgentResponse agentResponse) {
        AgentInvocation finishedInvocation = ongoingInvocations.remove(agentResponse.agentId());
        if (finishedInvocation == null) {
            throw new IllegalStateException("No ongoing invocation found for agent ID: " + agentResponse.agentId());
        }
        finishedInvocation.finished(agentResponse);
    }

    // 在代理执行错误时
    void onAgentInvocationError(AgentInvocationError agentInvocationError) {
        this.agentInvocationError = agentInvocationError;
    }

    // 代理工具执行继承
    void afterToolExecution(AfterAgentToolExecution afterToolExecution) {
        String agentId = afterToolExecution.agentInstance().agentId();
        AgentInvocation invocation = ongoingInvocations.get(agentId);
        if (invocation != null) {
            invocation.addToolExecution(afterToolExecution.toolExecution());
        }
    }

    public Collection<AgentInvocation> ongoingInvocations() {
        return ongoingInvocations.values();
    }

    public boolean done() {
        return topLevelInvocations.done();
    }

    public boolean hasError() {
        return agentInvocationError != null;
    }

    public AgentInvocationError error() {
        return agentInvocationError;
    }

    public AgentInvocation topLevelInvocations() {
        return topLevelInvocations;
    }

    /**
     * Returns the memory ID that identifies the session this execution belongs to.
     */
    public Object memoryId() {
        return agenticScope().memoryId();
    }

    public AgenticScope agenticScope() {
        return topLevelInvocations.agenticScope();
    }

    @Override
    public String toString() {
        return topLevelInvocations.toString();
    }
}
