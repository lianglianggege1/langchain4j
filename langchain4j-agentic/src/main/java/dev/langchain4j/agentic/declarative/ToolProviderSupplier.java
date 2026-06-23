package dev.langchain4j.agentic.declarative;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import dev.langchain4j.service.tool.ToolProvider;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a method as a supplier of a tool provider that an agent can utilize during its operation.
 * The annotated method must be static, with no arguments, and return an instance of {@link ToolProvider}.
 * <p>
 * Example:
 * <pre>
 * {@code
 *     public interface BankAgent {
 *         @Agent("A banker agent")
 *         String credit(@V("user") String user, @V("amountInUSD") Double amount);
 *
 *         @ToolProviderSupplier
 *         static ToolProvider toolProvider() {
 *             return ToolProviders.from(bankTool, currencyConverterTool);
 *         }
 *     }
 * }
 * </pre>
 */
/**
 * 将方法标记为工具提供器的提供器，智能体运行过程中可使用该工具提供器。
 * 被该注解修饰的方法必须为无参静态方法，且返回 {@link ToolProvider} 实例。
 * <p>
 * 示例：
 * <pre>
 * {@code
 *     public interface BankAgent {
 *         @Agent("银行智能体")
 *         String credit(@V("user") String user, @V("amountInUSD") Double amount);
 *
 *         @ToolProviderSupplier
 *         static ToolProvider toolProvider() {
 *             return ToolProviders.from(bankTool, currencyConverterTool);
 *         }
 *     }
 * }
 * </pre>
 */
@Retention(RUNTIME)
@Target({METHOD})
public @interface ToolProviderSupplier {
}
