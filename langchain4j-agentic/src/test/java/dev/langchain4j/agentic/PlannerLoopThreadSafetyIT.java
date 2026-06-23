package dev.langchain4j.agentic;

import dev.langchain4j.agentic.planner.Action;
import dev.langchain4j.agentic.planner.AgentInstance;
import dev.langchain4j.agentic.planner.InitPlanningContext;
import dev.langchain4j.agentic.planner.Planner;
import dev.langchain4j.agentic.planner.PlanningContext;
import dev.langchain4j.agentic.scope.AgentInvocation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class PlannerLoopThreadSafetyIT {

    interface CountingAgent {
        @Agent
        int count();
    }

    /**
     * A planner that tracks per-agent follow-ups to expose the lost-update race
     * in {@code composeActions}:
     *
     * Phase 1: Launch all agents in parallel (firstAction).
     *          As each completes, the planner checks by agent ID whether it
     *          already triggered a follow-up. If not, it schedules one.
     *          All N callbacks race on the unsynchronized {@code nextAction} field
     *          in {@code onSubagentInvoked}.
     *
     * Phase 2: Execute whatever follow-ups survived the race.
     *          Each follow-up completion is tracked. Once all agents that
     *          need a follow-up have completed both phases, return done().
     *
     * The per-agent tracking ensures the planner always terminates (no hang
     * on lost actions) and allows precise counting of dropped follow-ups.
     */
    /**
     * 该规划器会跟踪每个智能体的后续任务，用于暴露 `composeActions` 方法中因更新丢失引发的竞态问题：
     *
     * 阶段一：并行启动所有智能体（执行初始动作）。
     * 每个智能体执行完成后，规划器会根据智能体ID检查其是否已触发后续任务。若未触发，则为其安排后续任务。
     * 所有N个回调会在 `onSubagentInvoked` 方法的非同步字段 `nextAction` 上产生竞态。
     *
     * 阶段二：执行竞态后保留下来的所有后续任务。
     * 系统会记录每个后续任务的完成状态。当所有需要执行后续任务的智能体都完成两个阶段后，返回完成状态。
     *
     * 针对单个智能体的状态跟踪机制，可保障规划器正常终止（不会因动作丢失而卡死），同时能够精准统计被丢弃的后续任务数量。
     */
    public static class ParallelBurstPlanner implements Planner {

        private Map<String, AgentInstance> agentsById;

        private final Set<String> phase1Completed = ConcurrentHashMap.newKeySet();

        @Override
        public void init(InitPlanningContext ctx) {
            this.agentsById = ctx.subagents().stream()
                    .collect(Collectors.toMap(AgentInstance::agentId, Function.identity()));
        }

        @Override
        public Action firstAction(PlanningContext ctx) {
            return call(new ArrayList<>(agentsById.values()));
        }

        @Override
        public Action nextAction(PlanningContext ctx) {
            AgentInvocation prev = ctx.previousAgentInvocation();
            String agentId = prev.agentId();

            if (phase1Completed.add(agentId)) {
                // First time this agent completed — phase 1 done, schedule follow-up.
                // All N phase-1 callbacks arrive concurrently from parallel threads.
                // Each returns a call() action that composeActions should merge,
                // but the lost-update race silently drops some.
                // 该智能体首次完成任务——第一阶段结束，安排后续流程。
                // 所有第一阶段的回调由并行线程同时触发。
                // 每个回调都会返回一个call操作，本应由composeActions方法合并，
                // 但**更新丢失**的竞态问题导致部分操作被静默丢弃。
                return call(agentsById.get(agentId));
            }

            // This agent already completed phase 1, so this is a follow-up
            // completion. All surviving follow-ups run in a single
            // parallelExecution batch. Returning done() from each is safe:
            // composeActions(done(), done()) = done(), so the loop terminates
            // after the batch completes regardless of how many survived.
            // 该智能体已完成第一阶段，本次为后续任务完成回调
            // 所有剩余后续任务会在同一个并行执行批次中运行。每个回调返回done()是安全的：
            // composeActions(done(), done()) 结果仍为 done()，因此无论剩余任务数量多少，批次执行完毕后循环都会终止
            return done();
        }
    }

    /**
     * Launches 8 agents in parallel. Each completion schedules a follow-up
     * for that specific agent via {@code onSubagentInvoked → composeActions}.
     * All 8 callbacks race on the unsynchronized {@code nextAction} field.
     */
    /**
     * 并行启动8个智能体。每个智能体完成后，都会通过 {@code onSubagentInvoked → composeActions}
     * 为自身安排后续任务。所有8个回调会对未做同步处理的 {@code nextAction} 字段产生竞态条件。
     */
    @Test
    void parallel_burst_should_execute_all_followups() {
        int batchSize = 8;
        AtomicInteger executionCount = new AtomicInteger(0);

        Object[] subAgents = new Object[batchSize];
        for (int i = 0; i < batchSize; i++) {
            subAgents[i] = AgenticServices.agentAction(agenticScope -> {
                // Random sleep to increase interleaving and widen the race window
                Thread.sleep(ThreadLocalRandom.current().nextInt(1, 5));
                executionCount.incrementAndGet();
            });
        }

        CountingAgent agent = AgenticServices.plannerBuilder(CountingAgent.class)
                .subAgents(subAgents)
                .planner(ParallelBurstPlanner::new)
                .output(scope -> executionCount.get())
                .build();

        int result = agent.count();

        // Each of the N agents should run once in phase 1, then once more as
        // a follow-up in phase 2, for a total of 2*N executions.
        // If the race condition triggers, some follow-ups are lost and result < 2*batchSize.
        // N个智能体均需在第一阶段执行一次，第二阶段作为后续任务再执行一次，总计执行 2*N 次。
        // 若触发竞态条件，部分后续任务会丢失，最终执行次数将小于 2*批次大小。
        assertThat(result)
                .as("All %d phase-1 agents and their %d follow-ups should have executed", batchSize, batchSize)
                .isEqualTo(2 * batchSize);
    }
}
