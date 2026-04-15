package dev.langchain4j.agentic.declarative;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a method as a supplier of the {@link ChatMemoryProvider} that an agent can utilize during its operation.
 * The annotated method must be static, takes the memoryId as argument, and return an instance of {@link ChatMemory}.
 * 将一种方法标记为ChatMemoryProvider的供应商，代理可以在其操作过程中使用该供应商。
 * 带注释的方法必须是静态的，接受memoryId作为参数，并返回ChatMemory的实例。
 * <p>
 * Example:
 * <pre>
 * {@code
 *     public interface SupportAgent {
 *         @Agent("A customer support agent")
 *         String assist(@V("customerQuery") String customerQuery);
 *
 *         @ChatMemoryProviderSupplier
 *         static ChatMemory chatMemory(Object memoryId) {
 *             return MessageWindowChatMemory.withMaxMessages(10);
 *         }
 *     }
 * }
 * </pre>
 */
// 聊天记忆提供者
@Retention(RUNTIME)
@Target({METHOD})
public @interface ChatMemoryProviderSupplier {
}
