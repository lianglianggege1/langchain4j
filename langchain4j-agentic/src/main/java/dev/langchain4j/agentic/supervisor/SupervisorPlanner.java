package dev.langchain4j.agentic.supervisor;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.memory.ChatMemoryAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toMap;

// 主管计划器-聊天内存访问提供商
public class SupervisorPlanner implements Planner, ChatMemoryAccessProvider {

    private static final Logger LOG = LoggerFactory.getLogger(SupervisorPlanner.class);
    // 主管上下文
    public static final String SUPERVISOR_CONTEXT_KEY = "supervisorContext";
    // 在制定计划时，请参考以下主管背景信息，以便更好地了解限制条件、政策或偏好。
    public static final String SUPERVISOR_CONTEXT_PREFIX = "Use the following supervisor context to better understand "
            + "constraints, policies or preferences when creating the plan ";

    // 聊天模型
    private final ChatModel chatModel;

    // 聊天内存提供者
    private final ChatMemoryProvider chatMemoryProvider;

    // 最大agents调用次数
    private final int maxAgentsInvocations;
    // 循环次数
    private int loopCount = 0;

    // 响应agent
    private ResponseAgent responseAgent;

    // 上下文策略
    private final SupervisorContextStrategy contextStrategy;
    // 响应策略
    private final SupervisorResponseStrategy responseStrategy;

    // 请求生成器
    private final Function<AgenticScope, String> requestGenerator;

    // 输出key
    private final String outputKey;

    // 输出
    private final Function<AgenticScope, Object> output;

    // agents
    private Map<String, AgentInstance> agents;
    private String agentsList;

    // 请求
    private String request;

    public SupervisorPlanner(ChatModel chatModel, ChatMemoryProvider chatMemoryProvider, int maxAgentsInvocations,
                             SupervisorContextStrategy contextStrategy, SupervisorResponseStrategy responseStrategy,
                             Function<AgenticScope, String> requestGenerator, String outputKey, Function<AgenticScope, Object> output) {
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
        //  agents
        this.agents = initPlanningContext.subagents().stream().collect(toMap(AgentInstance::agentId, Function.identity()));
        // agents list
        this.agentsList = initPlanningContext.subagents().stream()
                .map(SupervisorPlanner::toCard)
                .collect(Collectors.joining(", "));

        this.request = requestGenerator != null ? requestGenerator.apply(initPlanningContext.agenticScope()) : initPlanningContext.agenticScope().readState("request", "");
        if (responseStrategy == SupervisorResponseStrategy.SCORED) {
            this.responseAgent = AiServices.builder(ResponseAgent.class).chatModel(chatModel).build();
        }
    }

    // 下一个动作
    @Override
    public Action nextAction(PlanningContext planningContext) {
        String lastResponse = planningContext.previousAgentInvocation() == null ?
                "" :
                planningContext.previousAgentInvocation().output().toString();
        if (loopCount++ >= maxAgentsInvocations) {
            return doneAction(planningContext.agenticScope(), lastResponse, null);
        }
        return nextSubagent(planningContext.agenticScope(), lastResponse);
    }

    // 卡片信息
    private static String toCard(AgentInstance agent) {
        List<String> agentArguments = agent.arguments().stream()
                .filter(a -> !a.name().equals("@MemoryId"))
                .map(a -> a.name() + ": " + a.rawType().getSimpleName())
                .toList();
        return "{'" + agent.agentId() + "', '" + agent.description() + "', " + agentArguments + "}";
    }

    // 下一个子agent
    private Action nextSubagent(AgenticScope agenticScope, String lastResponse) {
        // 在制定计划时，请参考以下主管背景信息，以便更好地了解限制条件、政策或偏好。
        String supervisorContext = agenticScope.hasState(SUPERVISOR_CONTEXT_KEY)
                ? SUPERVISOR_CONTEXT_PREFIX + "'" + agenticScope.readState(SUPERVISOR_CONTEXT_KEY, "") + "'."
                : "";

        AgentInvocation agentInvocation = planner(agenticScope).plan(agenticScope.memoryId(), agentsList, request, lastResponse, supervisorContext);
        LOG.info("Agent Invocation: {}", agentInvocation);

        // agent的名字是否为done
        if (agentInvocation.getAgentName().equalsIgnoreCase("done")) {
            return doneAction(agenticScope, lastResponse, agentInvocation);
        }

        // agent实例
        AgentInstance agent = findAgentByName(agentInvocation.getAgentName());

        agentInvocation.getArguments().entrySet().stream()
                .filter(entry -> writeArgumentToScope(agenticScope, agent, entry.getKey(), entry.getValue()))
                .forEach(entry -> agenticScope.writeState(entry.getKey(), entry.getValue()));
        return call(agent);
    }

    // 通过名字查找agent
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

    // 写入参数到scope
    private boolean writeArgumentToScope(AgenticScope agenticScope, AgentInstance agent, String key, Object value) {
        if (agenticScope.hasState(key)) {
            Class<?> argType = agent.arguments().stream()
                    .filter(arg -> arg.name().equals(key))
                    .findFirst().map(AgentArgument::rawType).orElse(null);
            if (argType != null) {
                Object existingValue = agenticScope.readState(key);
                // avoid overwriting a structured state with an unstructured argument generated from supervisor's LLM response
                return !argType.isAssignableFrom(existingValue.getClass()) || argType.isAssignableFrom(value.getClass());
            }
        }
        return true;
    }

    // 执行完成
    private Action doneAction(AgenticScope agenticScope, String lastResponse, AgentInvocation done) {
        Object result = result(agenticScope, request, lastResponse, done);
        if (outputKey != null) {
            agenticScope.writeState(outputKey, result);
        }
        return done(result);
    }

    // 构建计划agent
    private PlannerAgent planner(AgenticScope agenticScope) {
        return ((DefaultAgenticScope) agenticScope).getOrCreateAgent(agentId(), this::buildPlannerAgent);
    }

    // 结果
    private Object result(AgenticScope agenticScope, String request, String lastResponse, AgentInvocation done) {
        if (output != null) {
            return output.apply(agenticScope);
        }
        if (done == null || done.getArguments() == null || done.getArguments().get("response") == null) {
            return lastResponse;
        }
        String doneResponse = done.getArguments().get("response").toString();

        return switch (responseStrategy) {
            //  最后
            case LAST -> lastResponse;
            //  摘要
            case SUMMARY -> doneResponse;
            //  评分
            case SCORED -> {
                ResponseScore score = responseAgent.scoreResponses(request, lastResponse, doneResponse);
                LOG.info("Response scores: {}", score);
                yield score.getScore2() > score.getScore1() ? doneResponse : lastResponse;
            }
        };
    }

    // 构建计划agent
    private PlannerAgent buildPlannerAgent(AgenticScope agenticScope) {
        var builder = AiServices.builder(PlannerAgent.class).chatModel(chatModel);
        configureMemoryAndContext(agenticScope, builder);
        return builder.build();
    }

    // 配置内存和上下文
    private void configureMemoryAndContext(AgenticScope agenticScope, AiServices<PlannerAgent> builder) {
        if (chatMemoryProvider != null) {
            builder.chatMemoryProvider(chatMemoryProvider);
            if (contextStrategy != SupervisorContextStrategy.CHAT_MEMORY) {
                builder.chatRequestTransformer(new Context.Summarizer(agenticScope, chatModel));
            }
        } else {
            switch (contextStrategy) {
                // 聊天记忆
                case CHAT_MEMORY:
                    builder.chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20));
                    break;
                    // 摘要
                case SUMMARIZATION:
                    builder.chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(2))
                            .chatRequestTransformer(new Context.Summarizer(agenticScope, chatModel));
                    break;
                    // 聊天记忆和摘要
                case CHAT_MEMORY_AND_SUMMARIZATION:
                    builder.chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                            .chatRequestTransformer(new Context.Summarizer(agenticScope, chatModel));
                    break;
            }
        }
    }

   //  agent id
    private String agentId() {
        return outputKey + "@Supervisor";
    }

    // 聊天内存访问
    @Override
    public Map<String, Object> executionState() {
        return Map.of("loopCount", loopCount);
    }

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
