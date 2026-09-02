package dev.langchain4j.agentic.patterns.debate;

import dev.langchain4j.agentic.planner.Action;
import dev.langchain4j.agentic.planner.AgentInstance;
import dev.langchain4j.agentic.planner.AgenticSystemTopology;
import dev.langchain4j.agentic.planner.InitPlanningContext;
import dev.langchain4j.agentic.planner.PlanningContext;
import dev.langchain4j.agentic.planner.Planner;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.agentic.scope.DefaultAgenticScope;
import dev.langchain4j.data.message.AiMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A debate planner that orchestrates iterative adversarial rounds among debater agents.
 * <p>
 * Debaters generate independent positions in parallel, then enter critique rounds where they see
 * each other's reasoning (via a {@code "debateContext"} scope key) and can revise their positions.
 * Rounds continue until the {@link ConvergenceStrategy} reports convergence or {@code maxRounds}
 * is reached, at which point the judge agent (the last registered subagent) renders a final verdict.
 */
/**
 * 辩论规划器，用于调度辩论代理之间多轮对抗迭代。
 * <p>
 * 辩论者并行生成各自独立立场，随后进入辩驳轮次：
 * 各方通过作用域键 {@code "debateContext"} 获取对方推理内容，并可以修正自身立场。
 * 轮次持续执行，直到 {@link ConvergenceStrategy} 判断收敛，或达到 {@code maxRounds} 最大轮数；
 * 此时由裁判代理（最后注册的子代理）给出最终裁决。
 */
public class DebatePlanner implements Planner {

    private static final Logger LOG = LoggerFactory.getLogger(DebatePlanner.class);

    public static final String DEBATE_CONTEXT_KEY = "debateContext";

    private static final int DEFAULT_MAX_ROUNDS = 3;

    private final int maxRounds;
    private final ConvergenceStrategy convergenceStrategy;

    private List<AgentInstance> debaters;
    private AgentInstance judge;

    private int currentRound = 1;
    private int completedInRound = 0;
    private final Map<String, Object> lastDebatersMessages = new HashMap<>();
    private boolean judgePhase = false;

    public DebatePlanner() {
        this(DEFAULT_MAX_ROUNDS, ConvergenceStrategy.unanimous());
    }

    public DebatePlanner(int maxRounds) {
        this(maxRounds, ConvergenceStrategy.unanimous());
    }

    public DebatePlanner(ConvergenceStrategy convergenceStrategy) {
        this(DEFAULT_MAX_ROUNDS, convergenceStrategy);
    }

    public DebatePlanner(int maxRounds, ConvergenceStrategy convergenceStrategy) {
        this.maxRounds = maxRounds;
        this.convergenceStrategy = convergenceStrategy;
    }

    @Override
    public void init(InitPlanningContext initPlanningContext) {
        List<AgentInstance> subagents = initPlanningContext.subagents();
        if (subagents.size() < 3) {
            throw new IllegalArgumentException(
                    "DebatePlanner requires at least 3 subagents (2 debaters + 1 judge), got " + subagents.size());
        }
        this.debaters = new ArrayList<>(subagents.subList(0, subagents.size() - 1));
        this.judge = subagents.get(subagents.size() - 1);
    }

    @Override
    public Action firstAction(PlanningContext planningContext) {
        planningContext.agenticScope().writeState(DEBATE_CONTEXT_KEY, "");
        LOG.info("Starting debate round 1 with {} debaters", debaters.size());
        return call(debaters);
    }

    @Override
    public Action nextAction(PlanningContext planningContext) {
        if (judgePhase) {
            return done(planningContext.previousAgentInvocation().output());
        }

        lastDebatersMessages.put(planningContext.previousAgentInvocation().agentName(), planningContext.previousAgentInvocation().output());
        completedInRound++;

        if (completedInRound < debaters.size()) {
            return noOp();
        }

        AgenticScope scope = planningContext.agenticScope();

        if (convergenceStrategy.hasConverged(lastDebatersMessages.values())) {
            LOG.info("Convergence reached after {} rounds", currentRound);
            judgePhase = true;
        }
        if (currentRound >= maxRounds) {
            LOG.info("Max rounds ({}) reached without convergence, invoking judge", maxRounds);
            judgePhase = true;
        }

        if (judgePhase) {
            writeDebateContext(scope);
            return call(judge);
        }

        currentRound++;
        completedInRound = 0;
        writeDebateContext(scope);
        LOG.info("Starting debate round {}", currentRound);
        return call(debaters);
    }

    private void writeDebateContext(AgenticScope scope) {
        String debatersContext = lastDebatersMessages.entrySet().stream()
                .map(e -> e.getKey() + ": \"" + e.getValue() + "\"")
                .collect(Collectors.joining("\n"));
        scope.writeState(DEBATE_CONTEXT_KEY, debatersContext);
    }

    @Override
    public Map<String, Object> executionState() {
        return Map.of("currentRound", currentRound, "lastDebatersMessages", lastDebatersMessages);
    }

    @Override
    public void restoreExecutionState(Map<String, Object> state) {
        Object savedRound = state.get("currentRound");
        if (savedRound instanceof Number n) {
            this.currentRound = n.intValue();
        }
    }

    @Override
    public AgenticSystemTopology topology() {
        return AgenticSystemTopology.STAR;
    }
}
