package dev.langchain4j.service.tool;

import dev.langchain4j.Internal;
import dev.langchain4j.agent.tool.ToolSpecification;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

/**
 * Represents a tool managed by an AI Service, combining:
 * 表示由AI Service管理的工具，组合:
 * <ul>
 *     <li>{@link ToolSpecification} — what is sent to the LLM</li>
 *         {@link ToolSpecification} - 发生给大模型的东西
 *     <li>{@link ToolExecutor} — what is called when the LLM invokes the tool</li>
 *         {@link ToolExecutor} — 当大模型执行工具时调用的东西
 *     <li>Metadata that controls how the AI Service handles this tool (e.g., immediate return)</li>
 *         控制AI服务如何处理此工具的元数据（例如，立即返回）
 * </ul>
 *
 * @since 1.13.0
 */
@Internal
public class AiServiceTool {

    // 工具规范
    private final ToolSpecification toolSpecification;
    // 工具执行
    private final ToolExecutor toolExecutor;
    // 立刻返回
    private final boolean immediateReturn;

    private AiServiceTool(Builder builder) {
        this.toolSpecification = ensureNotNull(builder.toolSpecification, "toolSpecification");
        this.toolExecutor = ensureNotNull(builder.toolExecutor, "toolExecutor");
        this.immediateReturn = builder.immediateReturn;
    }

    public ToolSpecification toolSpecification() {
        return toolSpecification;
    }

    public ToolExecutor toolExecutor() {
        return toolExecutor;
    }

    public boolean immediateReturn() {
        return immediateReturn;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private ToolSpecification toolSpecification;
        private ToolExecutor toolExecutor;
        private boolean immediateReturn;

        public Builder toolSpecification(ToolSpecification toolSpecification) {
            this.toolSpecification = toolSpecification;
            return this;
        }

        public Builder toolExecutor(ToolExecutor toolExecutor) {
            this.toolExecutor = toolExecutor;
            return this;
        }

        public Builder immediateReturn(boolean immediateReturn) {
            this.immediateReturn = immediateReturn;
            return this;
        }

        public AiServiceTool build() {
            return new AiServiceTool(this);
        }
    }
}
