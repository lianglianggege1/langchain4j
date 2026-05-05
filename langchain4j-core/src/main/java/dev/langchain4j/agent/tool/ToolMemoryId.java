package dev.langchain4j.agent.tool;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * If a {@link Tool} method parameter is annotated with this annotation,
 * memory id (parameter annotated with @MemoryId in AI Service) will be injected automatically.
 */
/**
 * 如果{@link Tool}工具方法的参数上标注了此注解，
 * 内存ID（AI服务中用@MemoryId标注的参数）将被自动注入。
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface ToolMemoryId {
}
