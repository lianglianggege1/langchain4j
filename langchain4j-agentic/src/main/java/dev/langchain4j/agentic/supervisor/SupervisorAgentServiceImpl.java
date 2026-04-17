package dev.langchain4j.agentic.supervisor;

import static dev.langchain4j.agentic.declarative.DeclarativeUtil.agenticScopeFunction;
import static dev.langchain4j.agentic.declarative.DeclarativeUtil.buildAgentFeatures;
import static dev.langchain4j.agentic.declarative.DeclarativeUtil.invokeStatic;
import static dev.langchain4j.agentic.declarative.DeclarativeUtil.selectMethod;
import static dev.langchain4j.agentic.internal.AgentUtil.validateAgentClass;

import dev.langchain4j.agentic.declarative.ChatMemoryProviderSupplier;
import dev.langchain4j.agentic.declarative.ChatModelSupplier;
import dev.langchain4j.agentic.declarative.Output;
import dev.langchain4j.agentic.declarative.SupervisorRequest;
import dev.langchain4j.agentic.internal.AbstractServiceBuilder;
import dev.langchain4j.agentic.planner.AgenticService;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import java.lang.reflect.Method;
import java.util.function.Function;

// 监督者代理服务实现
public class SupervisorAgentServiceImpl<T> extends AbstractServiceBuilder<T, SupervisorAgentServiceImpl<T>>
        implements SupervisorAgentService<T>, AgenticService<SupervisorAgentService<T>, T> {

    // 聊天模型
    private ChatModel chatModel;

    // 聊天记忆提供者
    private ChatMemoryProvider chatMemoryProvider;

    // 最大代理调用次数
    private int maxAgentsInvocations = 10;

    // 上下文策略
    private SupervisorContextStrategy contextStrategy = SupervisorContextStrategy.CHAT_MEMORY;
    // 响应主管
    private SupervisorResponseStrategy responseStrategy = SupervisorResponseStrategy.LAST;

    // 请求生成器
    private Function<AgenticScope, String> requestGenerator;
    // 主管上下文
    private String supervisorContext;

    // 主管agent服务实现
    public SupervisorAgentServiceImpl(Class<T> agentServiceClass, Method agenticMethod) {
        this(agentServiceClass, agenticMethod, null);
    }

    // 主管agent服务实现
    public SupervisorAgentServiceImpl(Class<T> agentServiceClass, Method agenticMethod, ChatModel chatModel) {
        super(agentServiceClass, agenticMethod);
        configureSupervisor(agentServiceClass, chatModel);
    }

    // 构建
    public T build() {
        // 添加主管上下文
        if (supervisorContext != null) {
            // 在call之前
            this.beforeCall(agenticScope -> {
                if (!agenticScope.hasState(SupervisorPlanner.SUPERVISOR_CONTEXT_KEY)) {
                    agenticScope.writeState(SupervisorPlanner.SUPERVISOR_CONTEXT_KEY, supervisorContext);
                }
            });
        }

        // 主管规划师
        return build(() -> new SupervisorPlanner(chatModel, chatMemoryProvider, maxAgentsInvocations,
                contextStrategy, responseStrategy, requestGenerator,
                outputKey, output));
    }

    public static SupervisorAgentService<SupervisorAgent> builder() {
        try {
            Method supervisorMethod = SupervisorAgent.class.getMethod("invoke", String.class);
            return new SupervisorAgentServiceImpl<>(SupervisorAgent.class, supervisorMethod);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> SupervisorAgentService<T> builder(Class<T> agentServiceClass) {
        return new SupervisorAgentServiceImpl<>(agentServiceClass, validateAgentClass(agentServiceClass, false));
    }

    // 对话模型
    @Override
    public SupervisorAgentServiceImpl<T> chatModel(ChatModel chatModel) {
        this.chatModel = chatModel;
        return this;
    }

    // 聊天记忆提供者
    @Override
    public SupervisorAgentServiceImpl<T> chatMemoryProvider(ChatMemoryProvider chatMemoryProvider) {
        this.chatMemoryProvider = chatMemoryProvider;
        return this;
    }

    // 请求生成器
    @Override
    public SupervisorAgentServiceImpl<T> requestGenerator(Function<AgenticScope, String> requestGenerator) {
        this.requestGenerator = requestGenerator;
        return this;
    }

    // 上下文策略
    @Override
    public SupervisorAgentServiceImpl<T> contextGenerationStrategy(SupervisorContextStrategy contextStrategy) {
        this.contextStrategy = contextStrategy;
        return this;
    }

    // 响应策略
    @Override
    public SupervisorAgentServiceImpl<T> responseStrategy(SupervisorResponseStrategy responseStrategy) {
        this.responseStrategy = responseStrategy;
        return this;
    }

    // 上下文主管
    @Override
    public SupervisorAgentServiceImpl<T> supervisorContext(String supervisorContext) {
        this.supervisorContext = supervisorContext;
        return this;
    }

    // 最大agent调用次数
    @Override
    public SupervisorAgentServiceImpl<T> maxAgentsInvocations(int maxAgentsInvocations) {
        this.maxAgentsInvocations = maxAgentsInvocations;
        return this;
    }

    // 服务类型为主管
    @Override
    public String serviceType() {
        return "Supervisor";
    }

    // 配置主管
    private void configureSupervisor(Class<T> agentServiceClass, ChatModel chatModel) {
        //选择方法
        selectMethod(
                agentServiceClass,
                method -> method.isAnnotationPresent(SupervisorRequest.class)
                        && method.getReturnType() == String.class)
                .map(m -> agenticScopeFunction(m, String.class))
                .ifPresent(this::requestGenerator);

        //选择方法
        selectMethod(
                agentServiceClass,
                method -> method.isAnnotationPresent(ChatModelSupplier.class)
                        && method.getReturnType() == ChatModel.class
                        && method.getParameterCount() == 0)
                .map(method -> (ChatModel) invokeStatic(method))
                .ifPresentOrElse(this::chatModel, () -> this.chatModel(chatModel));

        selectMethod(
                agentServiceClass,
                method -> method.isAnnotationPresent(ChatMemoryProviderSupplier.class)
                        && method.getReturnType() == ChatMemory.class
                        && method.getParameterCount() == 1)
                .map(method -> (ChatMemoryProvider) memoryId -> invokeStatic(method, memoryId))
                .ifPresent(this::chatMemoryProvider);

        selectMethod(agentServiceClass, method -> method.isAnnotationPresent(Output.class))
                .map(m -> agenticScopeFunction(m, Object.class))
                .ifPresent(this::output);

        buildAgentFeatures(agentServiceClass, this);
    }
}
