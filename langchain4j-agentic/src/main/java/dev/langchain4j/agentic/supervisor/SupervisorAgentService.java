package dev.langchain4j.agentic.supervisor;

import dev.langchain4j.agentic.agent.ErrorContext;
import dev.langchain4j.agentic.agent.ErrorRecoveryResult;
import dev.langchain4j.agentic.observability.AgentListener;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import java.util.Collection;
import java.util.function.Function;

// 监控Agent服务
public interface SupervisorAgentService<T> {

    // 构建
    T build();

    // 聊天模型
    SupervisorAgentService<T> chatModel(ChatModel chatModel);

    // 会话记忆提供者
    SupervisorAgentService<T> chatMemoryProvider(ChatMemoryProvider chatMemoryProvider);

    // 名称
    SupervisorAgentService<T> name(String name);

    // 描述
    SupervisorAgentService<T> description(String description);

    // 输出键
    SupervisorAgentService<T> outputKey(String outputKey);

    // 请求生成器
    SupervisorAgentService<T> requestGenerator(Function<AgenticScope, String> requestGenerator);

    // 上下文生成策略
    SupervisorAgentService<T> contextGenerationStrategy(SupervisorContextStrategy contextStrategy);

    // 响应策略
    SupervisorAgentService<T> responseStrategy(SupervisorResponseStrategy responseStrategy);

    // 监控上下文
    SupervisorAgentService<T> supervisorContext(String supervisorContext);

    // 子代理
    SupervisorAgentService<T> subAgents(Object... agents);

    // 子代理
    SupervisorAgentService<T> subAgents(Collection<?> agents);

    // 最大代理调用次数
    SupervisorAgentService<T> maxAgentsInvocations(int maxAgentsInvocations);

    // 输出
    SupervisorAgentService<T> output(Function<AgenticScope, Object> output);

    // 错误处理
    SupervisorAgentService<T> errorHandler(Function<ErrorContext, ErrorRecoveryResult> errorHandler);

    // 监听器
    SupervisorAgentService<T> listener(AgentListener agentListener);
}
