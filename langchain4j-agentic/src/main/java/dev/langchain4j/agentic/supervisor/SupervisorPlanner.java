package dev.langchain4j.agentic.supervisor;

import static java.util.stream.Collectors.toMap;

import dev.langchain4j.agentic.internal.Context;
import dev.langchain4j.agentic.planner.Action;
import dev.langchain4j.agentic.planner.AgentArgument;
import dev.langchain4j.agentic.planner.AgentInstance;
import dev.langchain4j.agentic.planner.ChatMemoryAccessProvider;
import dev.langchain4j.agentic.planner.InitPlanningContext;
import dev.langchain4j.agentic.planner.Planner;
import dev.langchain4j.agentic.planner.PlanningContext;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.agentic.scope.DefaultAgenticScope;
import dev.langchain4j.invocation.LangChain4jManaged;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.ParameterNameResolver;
import dev.langchain4j.service.memory.ChatMemoryAccess;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toMap;

/**
 graph TD
 A["SupervisorAgentServiceImpl.build()"] --> B["创建 SupervisorPlanner"]
 B --> C["PlannerBasedInvocationHandler.invoke()"]
 C --> D["PlannerLoop.loop()"]
 D --> E["planner.firstAction()"]
 E --> F["SupervisorPlanner.nextAction()"]
 F --> G{"loopCount >= max?"}
 G -- Yes --> H["doneAction() → 返回结果"]
 G -- No --> I["nextSubagent()"]
 I --> J["PlannerAgent.plan() → LLM决策"]
 J --> K{"agentName == done?"}
 K -- Yes --> L["doneAction() → result()策略选取"]
 K -- No --> M["findAgentByName()"]
 M --> N["参数写入AgenticScope"]
 N --> O["call(agent) → 执行子Agent"]
 O --> P["子Agent返回响应"]
 P --> F
 */
public class SupervisorPlanner implements Planner, ChatMemoryAccessProvider {

    private static final Logger LOG = LoggerFactory.getLogger(SupervisorPlanner.class);
    public static final String SUPERVISOR_CONTEXT_KEY = "supervisorContext";
    // 结合规划方案时，请参考以下主控上下文，以便充分理解相关约束、规则及偏好要求。
    public static final String SUPERVISOR_CONTEXT_PREFIX = "Use the following supervisor context to better understand "
            + "constraints, policies or preferences when creating the plan ";

    private final ChatModel chatModel;

    private final ChatMemoryProvider chatMemoryProvider;

    private final int maxAgentsInvocations;
    private int loopCount = 0;

    private ResponseAgent responseAgent;

    private final SupervisorContextStrategy contextStrategy;
    private final SupervisorResponseStrategy responseStrategy;

    private final Function<AgenticScope, String> requestGenerator;

    private final String outputKey;

    private final Function<AgenticScope, Object> output;

    private Map<String, AgentInstance> agents;
    private String agentsList;

    private String request;

    public SupervisorPlanner(
            ChatModel chatModel,
            ChatMemoryProvider chatMemoryProvider,
            int maxAgentsInvocations,
            SupervisorContextStrategy contextStrategy,
            SupervisorResponseStrategy responseStrategy,
            Function<AgenticScope, String> requestGenerator,
            String outputKey,
            Function<AgenticScope, Object> output) {
        this.chatModel = chatModel;
        this.chatMemoryProvider = chatMemoryProvider;
        this.maxAgentsInvocations = maxAgentsInvocations;
        this.contextStrategy = contextStrategy;
        this.responseStrategy = responseStrategy;
        this.requestGenerator = requestGenerator;
        this.outputKey = outputKey;
        this.output = output;
    }

    @Override
    public void init(final InitPlanningContext initPlanningContext) {
        // 1. 将所有子Agent注册到 Map 中（按 agentId 索引）
        this.agents =
                initPlanningContext.subagents().stream().collect(toMap(AgentInstance::agentId, Function.identity()));
        // 2. 生成子Agent的"名片"列表，供LLM识别
        this.agentsList = initPlanningContext.subagents().stream()
                .map(SupervisorPlanner::toCard)
                .collect(Collectors.joining(", "));

        this.request = requestGenerator != null
                ? requestGenerator.apply(initPlanningContext.agenticScope())
                : initPlanningContext.agenticScope().readState("request", "");
        // 3. 获取用户请求（自定义生成器 或 从Scope读取）
        this.request = requestGenerator != null ? requestGenerator.apply(initPlanningContext.agenticScope()) : initPlanningContext.agenticScope().readState("request", "");
        // 4. 如果是 SCORED 策略，创建打分用的 ResponseAgent
        if (responseStrategy == SupervisorResponseStrategy.SCORED) {
            this.responseAgent =
                    AiServices.builder(ResponseAgent.class).chatModel(chatModel).build();
        }
    }

    @Override
    public Action nextAction(PlanningContext planningContext) {
        String lastResponse = planningContext.previousAgentInvocation() == null
                || planningContext.previousAgentInvocation().output() == null
                ? ""
                : planningContext.previousAgentInvocation().output().toString();
        // 防无限循环：超过最大调用次数则终止
        if (loopCount++ >= maxAgentsInvocations) {
            return doneAction(planningContext.agenticScope(), lastResponse, null);
        }
        // 询问 LLM：下一步该调用哪个子Agent？
        return nextSubagent(planningContext.agenticScope(), lastResponse);
    }

    private static String toCard(AgentInstance agent) {
        List<String> agentArguments = agent.arguments().stream()
                .filter(a -> !a.name().equals("@MemoryId"))
                .map(SupervisorPlanner::argumentDescription)
                .toList();
        return "{'" + agent.agentId() + "', '" + agent.description() + "', " + agentArguments + "}";
    }

    private static String argumentDescription(AgentArgument arg) {
        return argumentDescription(arg.rawType(), arg.name());
    }

    private static String argumentDescription(Class<?> type, String name) {
        if (name == null) {
            return "";
        }

        if (type.isPrimitive()
                || type.isEnum()
                || type == String.class
                || type == Boolean.class
                || Number.class.isAssignableFrom(type)) {
            return name + ": " + type.getSimpleName();
        }

        String fieldsDescription = type.isRecord()
                ? Stream.of(type.getDeclaredConstructors()[0].getParameters())
                        .map(p -> argumentDescription(p.getType(), ParameterNameResolver.name(p)))
                        .collect(Collectors.joining(", "))
                : Stream.of(type.getDeclaredFields())
                        .map(f -> argumentDescription(f.getType(), f.getName()))
                        .collect(Collectors.joining(", "));

        return name + ": {" + fieldsDescription + "}";
    }

    private Action nextSubagent(AgenticScope agenticScope, String lastResponse) {
        // 1. 获取主管上下文（可选的约束/策略说明）
        String supervisorContext = agenticScope.hasState(SUPERVISOR_CONTEXT_KEY)
                ? SUPERVISOR_CONTEXT_PREFIX + "'" + agenticScope.readState(SUPERVISOR_CONTEXT_KEY, "") + "'."
                : "";

        // 2. 调用 PlannerAgent（LLM）进行规划决策
        AgentInvocation agentInvocation = withAgenticScope(
                agenticScope,
                () -> planner(agenticScope)
                        .plan(agenticScope.memoryId(), agentsList, request, lastResponse, supervisorContext));
        LOG.info("Agent Invocation: {}", agentInvocation);

        // 3. LLM 返回 "done" → 任务完成
        if (agentInvocation.getAgentName().equalsIgnoreCase("done")) {
            return doneAction(agenticScope, lastResponse, agentInvocation);
        }

        // 4. 根据 LLM 返回的 agentName 查找对应子Agent
        AgentInstance agent = findAgentByName(agentInvocation.getAgentName());

        // 5. 将 LLM 生成的参数写入 AgenticScope（状态共享）
        agentInvocation.getArguments().entrySet().stream()
                .filter(entry -> writeArgumentToScope(agenticScope, agent, entry.getKey(), entry.getValue()))
                .forEach(entry -> agenticScope.writeState(entry.getKey(), entry.getValue()));
        // 6. 返回"调用该子Agent"的 Action
        return call(agent);
    }

    // 将当前 AgenticScope 注入到 LangChain4jManaged 线程局部变量中
    private static <T> T withAgenticScope(AgenticScope agenticScope, Supplier<T> supplier) {
        LangChain4jManaged.setCurrent(Map.of(AgenticScope.class, agenticScope));
        try {
            return supplier.get();
        } finally {
            LangChain4jManaged.removeCurrent();
        }
    }

    private AgentInstance findAgentByName(String agentName) {
        AgentInstance agent = agents.get(agentName);
        if (agent == null) {
            List<AgentInstance> candidateAgents = agents.values().stream()
                    .filter(a -> a.name().equals(agentName))
                    .toList();
            if (candidateAgents.size() == 1) {
                agent = candidateAgents.get(0);
            }
        }
        if (agent == null) {
            throw new IllegalStateException("No agent found with name: " + agentName);
        }
        return agent;
    }

    // 防止 LLM 生成的非结构化参数覆盖已有的结构化状态
    private boolean writeArgumentToScope(AgenticScope agenticScope, AgentInstance agent, String key, Object value) {
        if (agenticScope.hasState(key)) {
            Class<?> argType = agent.arguments().stream()
                    .filter(arg -> arg.name().equals(key))
                    .findFirst()
                    .map(AgentArgument::rawType)
                    .orElse(null);
            if (argType != null) {
                Object existingValue = agenticScope.readState(key);
                // avoid overwriting a structured state with an unstructured argument generated from supervisor's LLM response
                // 避免用LLM生成的非结构化值覆盖结构化状态
                // response
                return !argType.isAssignableFrom(existingValue.getClass())
                        || argType.isAssignableFrom(value.getClass());
            }
        }
        return true;
    }

    private Action doneAction(AgenticScope agenticScope, String lastResponse, AgentInvocation done) {
        Object result = result(agenticScope, request, lastResponse, done);
        if (outputKey != null) {
            agenticScope.writeState(outputKey, result);
        }
        return done(result);
    }

    private PlannerAgent planner(AgenticScope agenticScope) {
        return ((DefaultAgenticScope) agenticScope).getOrCreateAgent(agentId(), this::buildPlannerAgent);
    }

    private Object result(AgenticScope agenticScope, String request, String lastResponse, AgentInvocation done) {
        if (output != null) {
            return output.apply(agenticScope);  // 自定义输出函数优先
        }
        if (done == null || done.getArguments() == null || done.getArguments().get("response") == null) {
            return lastResponse; // 退化场景
        }
        String doneResponse = done.getArguments().get("response").toString();

        return switch (responseStrategy) {
            case LAST -> lastResponse;  // 最后一个子Agent的输出
            case SUMMARY -> doneResponse; // LLM 的总结
            case SCORED -> { // 用另一个LLM对两者打分
                ResponseScore score = withAgenticScope(
                        agenticScope, () -> responseAgent.scoreResponses(request, lastResponse, doneResponse));
                LOG.info("Response scores: {}", score);
                yield score.getScore2() > score.getScore1() ? doneResponse : lastResponse;
            }
        };
    }

    private PlannerAgent buildPlannerAgent(AgenticScope agenticScope) {
        var builder = AiServices.builder(PlannerAgent.class).chatModel(chatModel);
        configureMemoryAndContext(agenticScope, builder);
        return builder.build();
    }

    private void configureMemoryAndContext(AgenticScope agenticScope, AiServices<PlannerAgent> builder) {
        if (chatMemoryProvider != null) {
            builder.chatMemoryProvider(chatMemoryProvider);
            if (contextStrategy != SupervisorContextStrategy.CHAT_MEMORY) {
                builder.chatRequestTransformer(new Context.Summarizer(agenticScope, chatModel));
            }
        } else {
            switch (contextStrategy) {
                //仅靠聊天记忆保持上下文
                case CHAT_MEMORY:
                    builder.chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20));
                    break;
//                    仅靠摘要压缩上下文
                case SUMMARIZATION:
                    builder.chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(2))
                            .chatRequestTransformer(new Context.Summarizer(agenticScope, chatModel));
                    break;
//                    两者兼用，上下文最丰富
                case CHAT_MEMORY_AND_SUMMARIZATION:
                    builder.chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                            .chatRequestTransformer(new Context.Summarizer(agenticScope, chatModel));
                    break;
            }
        }
    }

    private String agentId() {
        return outputKey + "@Supervisor";
    }

    // 保存当前循环计数
    @Override
    public Map<String, Object> executionState() {
        return Map.of("loopCount", loopCount);
    }

    // 从崩溃中恢复
    @Override
    public void restoreExecutionState(Map<String, Object> state) {
        Object savedLoopCount = state.get("loopCount");
        if (savedLoopCount instanceof Number n) {
            this.loopCount = n.intValue();
        }
    }

    @Override
    public ChatMemoryAccess chatMemoryAccess(AgenticScope agenticScope) {
        return planner(agenticScope);
    }
}
