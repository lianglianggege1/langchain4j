package dev.langchain4j.agentic.declarative;

import dev.langchain4j.model.chat.ChatModel;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a method as a supplier of the chat model to be used by an agent.
 * The method must be static and return an instance of {@link ChatModel}.
 * 将方法标记为agent要使用的聊天模型的供应商。该方法必须是静态的，并返回｛@link ChatModel｝的实例。
 * <p>
 * Example:
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
 */
@Retention(RUNTIME)
@Target({METHOD})
// 聊天模型供应商
public @interface ChatModelSupplier {
}
