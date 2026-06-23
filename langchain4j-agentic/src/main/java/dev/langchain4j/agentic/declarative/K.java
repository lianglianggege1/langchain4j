package dev.langchain4j.agentic.declarative;

import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.function.Function;

/**
 * A parameter of an agentic method is annotated with {@code @K} to indicate that it is populated with
 * the value of a key representing a specific typed state in the agentic system.
 * That parameter also becomes a prompt template variable, so its value will be injected into prompt templates defined
 * via @{@link UserMessage}, @{@link SystemMessage} and {@link AiServices#systemMessageProvider(Function)}.
 * The variable name to be used in the prompt template corresponds to the one returned by the {@link TypedKey#name()}
 * method of the class specified as the value of this annotation, which by default is the simple name of the class
 * implementing the {@link TypedKey} interface.
 * 代理方法的参数用@K注释，表示它填充了表示代理系统中特定类型状态的键的值。
 * 该参数也成为提示模板变量，因此其值将被注入到通过@UserMessage、@SystemMessage和AiServices定义的提示模板中。
 * systemMessageProvider（函数）。
 * 提示模板中使用的变量名与TypedKey返回的变量名相对应。
 * 指定为此注释值的类的name（）方法，
 * 默认情况下，该方法是实现TypedKey接口的类的简单名称。
 * <p>
 * Example:
 * <pre>
 * {@code @UserMessage("Hello, my name is {{UserName}}. I am {{UserAge}} years old.")
 * String chat(@K(UserName.class) String name, @K(UserAge.class) int age);}
 * </pre>
 * <p>
 * where:
 * <pre>
 * {@code public class UserName implements AgentState<String> {}"
 * public class UserAge implements AgentState<Integer> {}"}
 * </pre>
 *
 * @see UserMessage
 * @see SystemMessage
 * @see PromptTemplate
 */
/**
 * 智能体方法的参数使用 {@code @K} 注解标注，用于表示该参数会从智能体系统中
 * 读取对应类型键的特定状态值进行填充。
 * 该参数同时会成为提示模板变量，其值会自动注入通过 @{@link UserMessage}、@{@link SystemMessage}
 * 以及 {@link AiServices#systemMessageProvider(Function)} 定义的提示模板中。
 * 提示模板中使用的变量名，与作为该注解值指定的类所调用的 {@link TypedKey#name()} 方法返回值一致；
 * 默认情况下，该名称为实现 {@link TypedKey} 接口的类的简单类名。
 * <p>
 * 示例：
 * <pre>
 * {@code @UserMessage("你好，我叫 {{UserName}}。我今年 {{UserAge}} 岁。")
 * String chat(@K(UserName.class) String name, @K(UserAge.class) int age);}
 * </pre>
 * <p>
 * 其中：
 * <pre>
 * {@code public class UserName implements AgentState<String> {}
 * public class UserAge implements AgentState<Integer> {}"}
 * </pre>
 *
 * @see UserMessage
 * @see SystemMessage
 * @see PromptTemplate
 */
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface K {

    Class<? extends TypedKey<?>> value();
}
