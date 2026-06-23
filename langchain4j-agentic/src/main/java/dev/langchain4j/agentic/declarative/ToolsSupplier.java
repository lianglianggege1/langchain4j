package dev.langchain4j.agentic.declarative;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a method as a supplier of tools that an agent can utilize during its operation.
 * The annotated method must be static, with no arguments, and return a single Object, an array of Objects,
 * or a Map&lt;ToolSpecifications, ToolExecutors&gt;.
 * <p>
 * Example:
 * <pre>
 * {@code
 *     public interface BankAgent {
 *         @Agent("A banker agent")
 *         String credit(@V("user") String user, @V("amountInUSD") Double amount);
 *
 *         @ToolsSupplier
 *         static Object[] tools() {
 *             return new Object[] { bankTool, currencyConverterTool };
 *         }
 *     }
 * }
 * </pre>
 */
/**
 * 将方法标记为工具提供器，智能体运行过程中可使用这些工具。
 * 被该注解修饰的方法必须为无参静态方法，返回值可以是单个对象、对象数组，
 * 或 Map&lt;ToolSpecifications, ToolExecutors&gt; 类型。
 * <p>
 * 示例：
 * <pre>
 * {@code
 *     public interface BankAgent {
 *         @Agent("银行智能体")
 *         String credit(@V("user") String user, @V("amountInUSD") Double amount);
 *
 *         @ToolsSupplier
 *         static Object[] tools() {
 *             return new Object[] { bankTool, currencyConverterTool };
 *         }
 *     }
 * }
 * </pre>
 */
@Retention(RUNTIME)
@Target({METHOD})
public @interface ToolsSupplier {
}
