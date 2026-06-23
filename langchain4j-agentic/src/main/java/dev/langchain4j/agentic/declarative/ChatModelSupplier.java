package dev.langchain4j.agentic.declarative;

import dev.langchain4j.model.chat.ChatModel;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a method as a supplier of the chat model to be used by an agent.
 * The method must be static and return a {@link ChatModel}.
 * <p>
 * When the method has no parameters, it is invoked once at build time to provide a fixed model.
 * When the method has parameters annotated with {@link dev.langchain4j.service.V @V},
 * they are resolved from the current {@link dev.langchain4j.agentic.scope.AgenticScope AgenticScope}
 * at each invocation, enabling dynamic model selection based on runtime state.
 * <p>
 * Example (fixed model):
 * <pre>
 * {@code
 *      public interface SupervisorBanker {
 *
 *         @SupervisorAgent(responseStrategy = SupervisorResponseStrategy.SUMMARY, subAgents = {
 *                 @SubAgent(type = WithdrawAgent.class),
 *                 @SubAgent(type = CreditAgent.class)
 *         })
 *         String invoke(@V("request") String request);
 *
 *         @ChatModelSupplier
 *         static ChatModel chatModel() {
 *             return plannerModel();
 *         }
 *     }
 * }
 * </pre>
 * <p>
 * Example (dynamic model selection):
 * <pre>
 * {@code
 *      public interface MyEditor {
 *
 *         @Agent("Edit the story based on critique")
 *         String edit(@V("story") String story, @V("critique") CritiqueResult critique);
 *
 *         @ChatModelSupplier
 *         static ChatModel chatModel(@V("critique") CritiqueResult critique) {
 *             return critique != null && critique.score() > 8.0 ? enhancedModel() : baseModel();
 *         }
 *     }
 * }
 * </pre>
 */
/**
 * 将方法标记为智能体所使用聊天模型的提供器。
 * 该方法必须为静态方法，且返回值类型为 {@link ChatModel}。
 * <p>
 * 若该方法无入参，会在构建阶段执行一次，用于提供固定的聊天模型。
 * 若方法中带有标注 {@link dev.langchain4j.service.V @V} 的参数，
 * 每次调用时都会从当前 {@link dev.langchain4j.agentic.scope.AgenticScope 智能体作用域}
 * 解析参数，支持根据运行时状态动态选择聊天模型。
 * <p>
 * 示例（固定模型）：
 * <pre>
 * {@code
 *      public interface SupervisorBanker {
 *
 *         @SupervisorAgent(responseStrategy = SupervisorResponseStrategy.SUMMARY, subAgents = {
 *                 @SubAgent(type = WithdrawAgent.class),
 *                 @SubAgent(type = CreditAgent.class)
 *         })
 *         String invoke(@V("request") String request);
 *
 *         @ChatModelSupplier
 *         static ChatModel chatModel() {
 *             return plannerModel();
 *         }
 *     }
 * }
 * </pre>
 * <p>
 * 示例（动态选择模型）：
 * <pre>
 * {@code
 *      public interface MyEditor {
 *
 *         @Agent("根据评审意见修改故事内容")
 *         String edit(@V("story") String story, @V("critique") CritiqueResult critique);
 *
 *         @ChatModelSupplier
 *         static ChatModel chatModel(@V("critique") CritiqueResult critique) {
 *             return critique != null && critique.score() > 8.0 ? enhancedModel() : baseModel();
 *         }
 *     }
 * }
 * </pre>
 */
@Retention(RUNTIME)
@Target({METHOD})
public @interface ChatModelSupplier {
}
