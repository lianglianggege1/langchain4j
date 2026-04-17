package dev.langchain4j.agentic.observability;

import dev.langchain4j.Internal;
import dev.langchain4j.agentic.planner.AgentInstance;
import dev.langchain4j.agentic.scope.AgenticScope;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Monitors agent executions and provides observability for the LangChain4j Agentic system.
 * Generates a self-contained HTML report visualizing the static topology of an agentic system
 * and the dynamic execution traces.
 * 监控代理执行并为LangChain4j代理系统提供可观察性。
 * 生成一个自包含的HTML报告，可视化代理系统的静态拓扑和动态执行跟踪。
 *
 * <p>The report includes:
 *    报告包括：
 * <ul>
 *   <li>A visual tree chart of the agent hierarchy showing topology types, names, and properties</li>
 *   代理层次结构的可视化树形图，显示拓扑类型、名称和属性
 *   <li>A waterfall timeline of execution traces grouped by memory/session ID</li>
 *   按内存/会话ID分组的执行跟踪瀑布时间线
 * </ul>
 */
public class AgentMonitor implements AgentListener {

    private AgentInstance rootAgent;

    // 跟踪执行
    private final Map<Object, List<MonitoredExecution>> successfulExecutions = new ConcurrentHashMap<>();
    // 跟踪错误
    private final Map<Object, List<MonitoredExecution>> failedExecutions = new ConcurrentHashMap<>();
    // 正在执行
    private final Map<Object, MonitoredExecution> ongoingExecutions = new ConcurrentHashMap<>();

    @Internal
    public void setRootAgent(AgentInstance rootAgent) {
        this.rootAgent = rootAgent;
    }

    AgentInstance rootAgent() {
        return rootAgent;
    }

    // 在代理执行之前
    @Override
    public void beforeAgentInvocation(AgentRequest agentRequest) {
        Object memoryId = agentRequest.agenticScope().memoryId();
        MonitoredExecution currentExecution = ongoingExecutions.get(memoryId);
        if (currentExecution == null) {
            currentExecution = new MonitoredExecution(agentRequest);
            ongoingExecutions.put(memoryId, currentExecution);
        } else {
            currentExecution.beforeAgentInvocation(agentRequest);
        }
    }

    // 在代理执行之后
    @Override
    public void afterAgentInvocation(AgentResponse agentResponse) {
        Object memoryId = agentResponse.agenticScope().memoryId();
        MonitoredExecution execution = ongoingExecutions.get(memoryId);
        execution.afterAgentInvocation(agentResponse);
        if (execution.done()) {
            ongoingExecutions.remove(memoryId);
            successfulExecutions.computeIfAbsent(memoryId, k -> new ArrayList<>()).add(execution);
        }
    }

    // 在代理执行错误时
    @Override
    public void onAgentInvocationError(AgentInvocationError agentInvocationError) {
        Object memoryId = agentInvocationError.agenticScope().memoryId();
        MonitoredExecution execution = ongoingExecutions.remove(memoryId);
        if (execution != null) {
            execution.onAgentInvocationError(agentInvocationError);
            failedExecutions.computeIfAbsent(memoryId, k -> new ArrayList<>()).add(execution);
        }
    }

    // 在代理工具执行之后
    @Override
    public void afterAgentToolExecution(AfterAgentToolExecution afterAgentToolExecution) {
        Object memoryId = afterAgentToolExecution.agenticScope().memoryId();
        MonitoredExecution execution = ongoingExecutions.get(memoryId);
        if (execution != null) {
            execution.afterToolExecution(afterAgentToolExecution);
        }
    }

    // 代理执行继承
    @Override
    public boolean inheritedBySubagents() {
        return true;
    }

    // 正在执行
    public Map<Object, MonitoredExecution> ongoingExecutions() {
        return ongoingExecutions;
    }

    // 正在执行
    public MonitoredExecution ongoingExecutionFor(AgenticScope agenticScope) {
        return ongoingExecutionFor(agenticScope.memoryId());
    }

    // 正在执行
    public MonitoredExecution ongoingExecutionFor(Object memoryId) {
        return ongoingExecutions.get(memoryId);
    }

    // 成功执行
    public List<MonitoredExecution> successfulExecutions() {
        return successfulExecutions.values().stream().flatMap(List::stream).toList();
    }

    // 成功执行
    public List<MonitoredExecution> successfulExecutionsFor(AgenticScope agenticScope) {
        return successfulExecutionsFor(agenticScope.memoryId());
    }

    // 成功执行
    public List<MonitoredExecution> successfulExecutionsFor(Object memoryId) {
        return successfulExecutions.getOrDefault(memoryId, List.of());
    }

    // 失败执行
    public List<MonitoredExecution> failedExecutions() {
        return failedExecutions.values().stream().flatMap(List::stream).toList();
    }

    // 失败执行
    public List<MonitoredExecution> failedExecutionsFor(AgenticScope agenticScope) {
        return failedExecutionsFor(agenticScope.memoryId());
    }

    // 失败执行
    public List<MonitoredExecution> failedExecutionsFor(Object memoryId) {
        return failedExecutions.getOrDefault(memoryId, List.of());
    }

    /**
     * Returns the set of all memory IDs that have been tracked by this monitor,
     * including successful, failed, and ongoing executions.
     * 返回此监视器跟踪的所有内存ID的集合，包括成功、失败和正在进行的执行。
     */
    public Set<Object> allMemoryIds() {
        Set<Object> ids = new LinkedHashSet<>();
        ids.addAll(successfulExecutions.keySet());
        ids.addAll(failedExecutions.keySet());
        ids.addAll(ongoingExecutions.keySet());
        return Collections.unmodifiableSet(ids);
    }

    /**
     * Returns all executions (successful, failed, and ongoing) for a given memory ID.
     * 返回给定内存ID的所有执行（成功、失败和正在进行）。
     */
    public List<MonitoredExecution> allExecutionsFor(AgenticScope agenticScope) {
        return allExecutionsFor(agenticScope.memoryId());
    }

    /**
     * Returns all executions (successful, failed, and ongoing) for a given memory ID.
     * 返回给定内存ID的所有执行（成功、失败和正在进行）。
     */
    public List<MonitoredExecution> allExecutionsFor(Object memoryId) {
        List<MonitoredExecution> all = new ArrayList<>(successfulExecutionsFor(memoryId));
        all.addAll(failedExecutionsFor(memoryId));
        MonitoredExecution ongoing = ongoingExecutionFor(memoryId);
        if (ongoing != null) {
            all.add(ongoing);
        }
        return all;
    }
}
