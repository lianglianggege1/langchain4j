package dev.langchain4j.agentic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.langchain4j.agentic.internal.AgenticScopeOwner;
import dev.langchain4j.agentic.internal.SuspendedResponse;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.agentic.scope.AgenticScopeAccess;
import dev.langchain4j.agentic.scope.AgenticScopeKey;
import dev.langchain4j.agentic.scope.AgenticScopePersister;
import dev.langchain4j.agentic.scope.AgenticScopeRegistry;
import dev.langchain4j.agentic.scope.AgenticScopeSerializer;
import dev.langchain4j.agentic.scope.AgenticScopeStore;
import dev.langchain4j.agentic.scope.AgenticSystemSuspendedException;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * End-to-end integration test demonstrating workflow suspension and recovery
 * after a simulated crash, including HumanInTheLoop with PendingResponse.
 *
 * <p>Scenario: a sequential workflow runs three agents:
 * <ol>
 *   <li><b>DataProcessor</b> — processes input and writes intermediate state</li>
 *   <li><b>HumanReviewer</b> — a HumanInTheLoop that creates a {@link PendingResponse}
 *       to request human approval; the workflow suspends here</li>
 *   <li><b>ResultFinalizer</b> — reads the human approval and produces the final output</li>
 * </ol>
 *
 * <p>The test:
 * <ol>
 *   <li>Starts the workflow — agents 1 and 2 execute; the workflow suspends
 *       (throws {@link AgenticSystemSuspendedException}) instead of blocking</li>
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

    public interface RecoverableWorkflow extends AgenticScopeAccess {
        @Agent
        String process(@MemoryId String sessionId, @V("input") String input);
    }

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
    void workflow_suspends_and_recovers_from_crash_with_human_in_the_loop(@TempDir Path tempDir) {

        // ---- Setup persistence with file-based store ----
        // ---- 配置基于文件存储的持久化功能 ----
        FileBasedAgenticScopeStore store = new FileBasedAgenticScopeStore(tempDir);
        AgenticScopePersister.setStore(store);

        RecoverableWorkflow workflow = buildWorkflow();

        // ================================================================
        //  PHASE 1: Start workflow — it will suspend (not block!)
        // ================================================================
        // 第一阶段：启动工作流 — 流程将挂起（抛出异常），等待人工输入
        // ================================================================
        AgenticSystemSuspendedException suspended = assertThrows(
                AgenticSystemSuspendedException.class,
                () -> workflow.process("session-1", "raw data to process"));

        assertThat(suspended.scope().pendingResponseIds()).containsExactly("human-review");

        AgenticScope scopeBeforeCrash = suspended.scope();
        assertThat(scopeBeforeCrash.readState("processed_data", "")).isEqualTo("PROCESSED: raw data to process");

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
        // 第二阶段：模拟崩溃 — 清空全部内存状态
        // ================================================================
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
        // 挂起响应已在恢复的作用域中登记，等待外部提供结果
        assertThat(recoveredScope.pendingResponseIds()).containsExactly("human-review");

        // Provide the human response by replacing the PendingResponse with the actual value
        recoveredScope.writeState("approval", "APPROVED by human reviewer");

        // Re-invoke the workflow with the same session ID
        // The SequentialPlanner restores cursor and skips DataProcessor + HumanReviewer
        String finalResult = workflow.process("session-1", "raw data to process");

        // ================================================================
        //  VERIFY: the workflow completed successfully using recovered state
        // ================================================================
        // ================================================================
        // 校验：工作流依托恢复状态成功完成
        // ================================================================
        assertThat(finalResult).isEqualTo("Final result: PROCESSED: raw data to process | Approval: APPROVED by human reviewer");
    }

    // ---- Workflow construction ----
    // ---- 工作流构建 ----

    private RecoverableWorkflow buildWorkflow() {
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
                .responseProvider(scope -> new SuspendedResponse<>("human-review"))
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
}
