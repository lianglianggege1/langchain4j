package dev.langchain4j.agentic.declarative;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import dev.langchain4j.service.tool.ToolProvider;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a method as a supplier of a tool provider that an agent can utilize during its operation.
 * 将一种方法标记为工具提供商的供应商，代理可以在其操作过程中使用该供应商。
 * The annotated method must be static, with no arguments, and return an instance of {@link ToolProvider}.
 * 带注释的方法必须是静态的，没有参数，并返回｛@link ToolProvider｝的实例。
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
@Retention(RUNTIME)
@Target({METHOD})
public @interface ToolProviderSupplier {
}
