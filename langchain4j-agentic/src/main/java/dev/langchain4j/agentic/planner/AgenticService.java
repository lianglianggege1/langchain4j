package dev.langchain4j.agentic.planner;

import dev.langchain4j.agentic.agent.ErrorContext;
import dev.langchain4j.agentic.agent.ErrorRecoveryResult;
import dev.langchain4j.agentic.declarative.TypedKey;
import dev.langchain4j.agentic.internal.AgentExecutor;
import dev.langchain4j.agentic.observability.AgentListener;
import dev.langchain4j.agentic.scope.AgenticScope;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

// agentic服务
public interface AgenticService<T, A> {

    // 构建
    A build();

    // 子代理
    T subAgents(Object... agents);

    // 子代理们
    T subAgents(List<AgentExecutor> agentExecutors);

    // 在agent call之前
    T beforeCall(Consumer<AgenticScope> beforeCall);

    // 名字
    T name(String name);

    // 描述
    T description(String description);

    // 输出建
    T outputKey(String outputKey);
    T outputKey(Class<? extends TypedKey<?>> outputKey);

    // 输出
    T output(Function<AgenticScope, Object> output);

    // 错误处理
    T errorHandler(Function<ErrorContext, ErrorRecoveryResult> errorHandler);

    // 监听
    T listener(AgentListener listeners);
}
