package dev.langchain4j.agentic;

import static org.assertj.core.api.Assertions.assertThat;

import dev.langchain4j.agentic.internal.AgenticScopeOwner;
import dev.langchain4j.agentic.internal.PendingResponse;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.agentic.scope.AgenticScopeAccess;
import dev.langchain4j.agentic.scope.AgenticScopeKey;
import dev.langchain4j.agentic.scope.AgenticScopePersister;
import dev.langchain4j.agentic.scope.AgenticScopeRegistry;
import dev.langchain4j.agentic.scope.AgenticScopeSerializer;
import dev.langchain4j.agentic.scope.AgenticScopeStore;
import dev.langchain4j.agentic.scope.DefaultAgenticScope;
import dev.langchain4j.agentic.workflow.HumanInTheLoop;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.V;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * End-to-end integration test demonstrating workflow persistence and recovery
 * after a simulated crash, including HumanInTheLoop with PendingResponse.
 *
 * <p>Scenario: a sequential workflow runs three agents:
 * <ol>
 *   <li><b>DataProcessor</b> — processes input and writes intermediate state</li>
 *   <li><b>HumanReviewer</b> — a HumanInTheLoop that creates a {@link PendingResponse}
 *       to request human approval; the workflow blocks waiting for this response</li>
 *   <li><b>ResultFinalizer</b> — reads the human approval and produces the final output</li>
 * </ol>
 *
 * <p>The test:
 * <ol>
 *   <li>Starts the workflow — agents 1 and 2 execute; agent 3 blocks on the pending response</li>
 *   <li>Simulates a crash — clears all in-memory state</li>
 *   <li>Recovers from the file-persisted scope — provides the human response and re-invokes</li>
 *   <li>The planner resumes from the checkpoint: only agent 3 runs, using the provided response</li>
 * </ol>
 */
/**
 * 端到端集成测试，用于演示工作流在模拟崩溃后的持久化与恢复能力，包含带待处理响应的人机交互流程。
 *
 * <p>测试场景：串行工作流依次运行三个智能体：
 * <ol>
 *   <li><b>数据处理器</b> — 处理输入数据并写入中间状态</li>
 *   <li><b>人工审核器</b> — 人机交互节点，生成{@link PendingResponse}以等待人工审批，工作流在此阻塞</li>
 *   <li><b>结果生成器</b> — 读取人工审批结果并生成最终输出</li>
 * </ol>
 *
 * <p>测试流程：
 * <ol>
 *   <li>启动工作流，执行第一个和第二个智能体，第三个智能体因等待响应进入阻塞状态</li>
 *   <li>模拟系统崩溃，清空所有内存状态数据</li>
 *   <li>从文件持久化的作用域恢复数据，补充人工响应并重新触发执行</li>
 *   <li>规划器从检查点恢复运行，仅执行第三个智能体，并使用已提供的响应数据</li>
 * </ol>
 */
class RecoverabilityIT {

    // ---- Agent interface with @MemoryId for persistence ----

    public interface RecoverableWorkflow extends AgenticScopeAccess {
        @Agent
        String process(@MemoryId String sessionId, @V("input") String input);
    }

    // ---- File-based AgenticScopeStore using JSON serialization to temp files ----

    static class FileBasedAgenticScopeStore implements AgenticScopeStore {

        private final Path directory;

        FileBasedAgenticScopeStore(Path directory) {
            this.directory = directory;
        }

        @Override
        public boolean save(AgenticScopeKey key, DefaultAgenticScope agenticScope) {
            try {
                String json = AgenticScopeSerializer.toJson(agenticScope);
                Files.writeString(fileFor(key), json);
                return true;
            } catch (IOException e) {
                throw new RuntimeException("Failed to save scope to file", e);
            }
        }

        @Override
        public Optional<DefaultAgenticScope> load(AgenticScopeKey key) {
            Path file = fileFor(key);
            if (!Files.exists(file)) {
                return Optional.empty();
            }
            try {
                String json = Files.readString(file);
                return Optional.of(AgenticScopeSerializer.fromJson(json));
            } catch (IOException e) {
                throw new RuntimeException("Failed to load scope from file", e);
            }
        }

        @Override
        public boolean delete(AgenticScopeKey key) {
            try {
                return Files.deleteIfExists(fileFor(key));
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete scope file", e);
            }
        }

        @Override
        public Set<AgenticScopeKey> getAllKeys() {
            try (Stream<Path> files = Files.list(directory)) {
                return files.filter(f -> f.toString().endsWith(".json"))
                        .map(f -> {
                            String name = f.getFileName().toString().replace(".json", "");
                            String[] parts = name.split("__", 2);
                            return new AgenticScopeKey(parts[0], parts.length > 1 ? parts[1] : "");
                        })
                        .collect(Collectors.toSet());
            } catch (IOException e) {
                throw new RuntimeException("Failed to list scope files", e);
            }
        }

        private Path fileFor(AgenticScopeKey key) {
            String filename = key.agentId().replaceAll("[^a-zA-Z0-9._-]", "_")
                    + "__" + key.memoryId().toString().replaceAll("[^a-zA-Z0-9._-]", "_")
                    + ".json";
            return directory.resolve(filename);
        }
    }

    @AfterEach
    void cleanup() {
        AgenticScopePersister.setStore(null);
    }

    @Test
    void workflow_recovers_from_crash_with_human_in_the_loop(@TempDir Path tempDir) throws Exception {

        // ---- Setup persistence with file-based store ----
        // ---- 配置基于文件存储的持久化功能 ----
        FileBasedAgenticScopeStore store = new FileBasedAgenticScopeStore(tempDir);
        AgenticScopePersister.setStore(store);

        // ---- Track the PendingResponse created by HumanInTheLoop so we can unblock Phase 1 during cleanup ----
        // ---- 跟踪人工介入创建的待处理响应，以便清理阶段解除第一阶段阻塞 ----
        AtomicReference<PendingResponse<String>> phase1PendingRef = new AtomicReference<>();

        // ---- Build the workflow ----
        // ---- 构建工作流 ----
        RecoverableWorkflow workflow = buildWorkflow(phase1PendingRef);

        // ================================================================
        //  PHASE 1: Start workflow — it will block waiting for human input
        // ================================================================
        // ================================================================
        // 第一阶段：启动工作流 — 流程将阻塞，等待人工输入
        // ================================================================
        CompletableFuture<String> phase1Future = CompletableFuture.supplyAsync(
                () -> workflow.process("session-1", "raw data to process"));

        // Wait until the HumanInTheLoop agent has executed and persisted the PendingResponse
        // The per-step checkpointing saves state after each agent invocation
        // 等待人工介入智能体执行完成并持久化待处理响应
        // 分步检查点机制会在每次调用智能体后保存状态
        awaitPendingResponse(workflow, "session-1");

        // At this point:
        // - DataProcessor has run → state contains "processed_data"
        // - HumanInTheLoop has run → state contains PendingResponse("human-review") under key "approval"
        // - ResultFinalizer is blocked on readState("approval") → waiting for PendingResponse completion
        // - Per-step checkpointing has saved the scope with cursor position = 2
        // 此时状态：
        // - 数据处理器已执行 → 状态中存在"processed_data"
        // - 人工介入模块已执行 → 状态键"approval"下存有待处理响应（人工审核）
        // - 结果定稿器读取状态"approval"时阻塞 → 等待待处理响应完成
        // - 分步检查点已保存作用域，游标位置为2

        AgenticScope scopeBeforeCrash = workflow.getAgenticScope("session-1");
        assertThat(scopeBeforeCrash.readState("processed_data", "")).isEqualTo("PROCESSED: raw data to process");
        assertThat(scopeBeforeCrash.pendingResponseIds()).containsExactly("human-review");

        // Verify that the planner execution state was saved in scope state (by PlannerLoop)
        // 校验规划器执行状态已由规划循环存入作用域状态
        assertThat(scopeBeforeCrash.state().entrySet().stream()
                .anyMatch(e -> e.getKey().startsWith("__planner_state_"))).isTrue();

        // Verify the scope was persisted to file
        // 校验作用域已持久化至文件
        assertThat(store.getAllKeys()).isNotEmpty();

        // ================================================================
        //  PHASE 2: Simulate crash — clear all in-memory state
        // ================================================================
        // Note: Phase 1 thread is still blocked on PendingResponse.blockingGet() in the Finalizer.
        // We do NOT complete the Phase 1 PendingResponse here — that would cause rootCallEnded
        // to replace the PendingResponse in state with the resolved value and flush to the store.
        // Instead we simulate a hard crash by simply clearing in-memory state.
        // ================================================================
        // 第二阶段：模拟崩溃 — 清空全部内存状态
        // ================================================================
        // 注：第一阶段线程仍在定稿器中阻塞执行PendingResponse.blockingGet()
        // 此处不完成第一阶段的待处理响应——否则会触发rootCallEnded
        // 将状态内的待处理响应替换为已解析值并刷新至存储
        // 取而代之，直接清空内存状态以模拟程序强制崩溃
        AgenticScopeRegistry registry = ((AgenticScopeOwner) workflow).registry();
        registry.clearInMemory();

        // In-memory state is gone — the only surviving data is in the file store
        // 内存状态已清空——仅文件存储中留存有效数据
        assertThat(registry.getAllAgenticScopeKeysInMemory()).isEmpty();

        // ================================================================
        //  PHASE 3: Recovery — provide human response and resume workflow
        // ================================================================
        // ================================================================
        // 第三阶段：故障恢复 — 传入人工审核结果并恢复工作流
        // ================================================================

        // Load the persisted scope (via the agent's AgenticScopeAccess interface)
        // This loads the scope from the file store into the in-memory registry
        // 加载持久化作用域（通过智能体的AgenticScopeAccess接口）
        // 将文件存储中的作用域载入内存注册表
        AgenticScope recoveredScope = workflow.getAgenticScope("session-1");

        // Verify state survived the crash
        // 校验状态在崩溃后未丢失
        assertThat(recoveredScope.readState("processed_data", "")).isEqualTo("PROCESSED: raw data to process");
        // The PendingResponse was deserialized as a new incomplete future
        // 待处理响应已反序列化为全新未完成异步任务
        assertThat(recoveredScope.pendingResponseIds()).containsExactly("human-review");

        // Simulate the human providing their response (e.g., via a REST endpoint in a Quarkus extension)
        // Replace the PendingResponse with the actual value so the finalizer can read it immediately
        // 模拟人工提交审核结果（例如通过Quarkus扩展的REST接口）
        // 使用实际值替换待处理响应，使定稿器可立即读取
        recoveredScope.writeState("approval", "APPROVED by human reviewer");

        // Re-invoke the workflow with the same session ID
        // The SequentialPlanner will restore cursor=2 from state and skip DataProcessor + HumanInTheLoop
        // Only ResultFinalizer runs
        // 使用相同会话ID重新执行工作流
        // 顺序规划器将从状态恢复游标值2，跳过数据处理器与人工介入模块
        // 仅执行结果定稿器
        String finalResult = workflow.process("session-1", "raw data to process");

        // ================================================================
        //  VERIFY: the workflow completed successfully using recovered state
        // ================================================================
        // ================================================================
        // 校验：工作流依托恢复状态成功完成
        // ================================================================
        assertThat(finalResult).isEqualTo("Final result: PROCESSED: raw data to process | Approval: APPROVED by human reviewer");

        // Cleanup: unblock the Phase 1 thread (it's blocked on the OLD PendingResponse object)
        // 清理操作：解除第一阶段线程阻塞（该线程阻塞于旧的待处理响应对象）
        PendingResponse<String> phase1Pending = phase1PendingRef.get();
        if (phase1Pending != null) {
            phase1Pending.complete("cleanup");
        }
        try {
            phase1Future.get(5, TimeUnit.SECONDS);
        } catch (Exception ignored) {
            // Phase 1 result is irrelevant
        }
    }

    // ---- Workflow construction ----
    // ---- 工作流构建 ----

    @SuppressWarnings("unchecked")
    private RecoverableWorkflow buildWorkflow(AtomicReference<PendingResponse<String>> pendingRef) {
        // Agent 1: DataProcessor — transforms raw input and writes to state
        // 智能体1：数据处理器——转换原始输入并写入状态
        AgenticServices.AgenticScopeAction dataProcessor = AgenticServices.agentAction(
                scope -> {
                    String input = (String) scope.readState("input");
                    scope.writeState("processed_data", "PROCESSED: " + input);
                });

        // Agent 2: HumanInTheLoop — creates a PendingResponse to pause for human approval
        // 智能体2：人工介入模块——生成待处理响应以暂停流程等待人工审批
        HumanInTheLoop humanReviewer = AgenticServices.humanInTheLoopBuilder()
                .description("Request human approval for the processed data")
                .outputKey("approval")
                .responseProvider(scope -> {
                    PendingResponse<String> pending = new PendingResponse<>("human-review");
                    pendingRef.set(pending);
                    return pending;
                })
                .build();

        // Agent 3: ResultFinalizer — combines processed data with human approval
        // 智能体3：结果定稿器——整合处理后数据与人审结果
        AgenticServices.AgenticScopeAction resultFinalizer = AgenticServices.agentAction(
                scope -> {
                    String processedData = (String) scope.readState("processed_data");
                    String approval = (String) scope.readState("approval");
                    scope.writeState("final_result",
                            "Final result: " + processedData + " | Approval: " + approval);
                });

        return AgenticServices.sequenceBuilder(RecoverableWorkflow.class)
                .subAgents(dataProcessor, humanReviewer, resultFinalizer)
                .outputKey("final_result")
                .build();
    }

    // ---- Helpers ----

    /**
     * Polls until the HumanInTheLoop agent has executed and the PendingResponse
     * is visible in the scope state.
     */
    /**
     * 循环轮询直至人工介入智能体执行完毕，且待处理响应已在作用域状态中可见
     */
    private void awaitPendingResponse(RecoverableWorkflow workflow, String sessionId) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < deadline) {
            try {
                AgenticScope scope = workflow.getAgenticScope(sessionId);
                if (scope != null && !scope.pendingResponseIds().isEmpty()) {
                    return;
                }
            } catch (Exception ignored) {
                // Scope may not exist yet
            }
            Thread.sleep(100);
        }
        throw new AssertionError("Timed out waiting for PendingResponse to appear in scope state");
    }
}
