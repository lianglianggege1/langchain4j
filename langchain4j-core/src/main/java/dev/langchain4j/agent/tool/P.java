package dev.langchain4j.agent.tool;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for parameters of a {@link Tool}-annotated method.
 *
 * <h2>Description</h2>
 * {@link #value()} and {@link #description()} are aliases for the same thing: the parameter's description
 * that the LLM will see. Use one or the other, but not both at the same time.
 * <p>When only a description is needed, {@code value} can be used as a shorthand:
 * <pre>{@code
 * @Tool
 * void getWeather(@P("The city name") String city) { ... }
 * }</pre>
 * <p>When both a name and a description are needed, use named attributes:
 * <pre>{@code
 * @Tool
 * void getWeather(@P(name = "city", description = "The city name") String city) { ... }
 * }</pre>
 *
 * <h2>Name</h2>
 * The {@link #name()} attribute overrides the parameter name that the LLM will see.
 * This is useful in two cases:
 * <ol>
 *   <li><b>Missing {@code -parameters} javac option.</b>
 *       Without it (common when not using frameworks like Quarkus or Spring, which enable it by default),
 *       Java reflection returns generic names such as {@code arg0}, {@code arg1}, etc.
 *       The semantic meaning of the parameter is lost, which may confuse the LLM.
 *       Setting {@code name} restores a meaningful name.</li>
 *   <li><b>Custom name for the LLM.</b>
 *       When you want the LLM to see a different parameter name than the one the developer uses in the source code
 *       (for example, to match a specific API contract or to provide a more descriptive name).</li>
 * </ol>
 */
/**
 * 用于标记被{@link Tool}注解的方法的参数的注解。
 *
 * <h2>描述</h2>
 * {@link #value()} 和 {@link #description()} 是同一个属性的别名：表示大语言模型可见的参数描述。
 * 使用其中一个即可，不可同时使用。
 * <p>当仅需要描述时，可使用 {@code value} 作为简写：
 * <pre>{@code
 * @Tool
 * void getWeather(@P("城市名称") String city) { ... }
 * }</pre>
 * <p>当同时需要名称和描述时，使用具名属性：
 * <pre>{@code
 * @Tool
 * void getWeather(@P(name = "city", description = "城市名称") String city) { ... }
 * }</pre>
 *
 * <h2>名称</h2>
 * {@link #name()} 属性会覆盖大语言模型看到的参数名称。
 * 该属性在两种场景下非常实用：
 * <ol>
 *   <li><b>未配置 {@code -parameters} 编译参数。</b>
 *       若未启用该参数（在不使用Quarkus、Spring等默认开启该参数的框架时很常见），
 *       Java反射会返回 {@code arg0}、{@code arg1} 等通用参数名。
 *       这会丢失参数的语义含义，可能导致大语言模型混淆。
 *       设置 {@code name} 可以恢复有意义的参数名。</li>
 *   <li><b>为大语言模型自定义参数名。</b>
 *       当你希望大语言模型看到的参数名与开发者在源代码中使用的名称不同时
 *       （例如，匹配特定的API约定或提供更具描述性的名称）。</li>
 * </ol>
 */
@Retention(RUNTIME)
@Target({PARAMETER})
public @interface P {

    /**
     * Name of the parameter as seen by the LLM.
     * <p>If not specified, the actual method parameter name is used (requires the {@code -parameters} javac option;
     * otherwise the name defaults to {@code arg0}, {@code arg1}, etc.).
     * <p>Setting this is useful when:
     * <ul>
     *   <li>The {@code -parameters} javac option is not enabled and you want to avoid
     *       generic {@code arg0}/{@code arg1} names.
     *       Note that frameworks like Quarkus and Spring enable {@code -parameters} by default,
     *       so you typically do not need to set {@code name} when using those frameworks.</li>
     *   <li>You want the LLM to see a different name than the one in the source code.</li>
     * </ul>
     *
     * @return the name of the parameter
     */
    String name() default "";

    /**
     * Description of the parameter. This is an alias for {@link #value()}.
     * Use either {@code value} or {@code description}, but not both.
     *
     * @return the description of the parameter
     */
    String description() default "";

    /**
     * Description of the parameter. This is an alias for {@link #description()}.
     * Use either {@code value} or {@code description}, but not both.
     * <p>Convenient for the shorthand form: {@code @P("description here")}.
     *
     * @return the description of the parameter
     */
    String value() default "";

    /**
     * Whether the parameter is required.
     * Default is {@code true}.
     *
     * @return {@code true} if the parameter is required, {@code false} otherwise
     */
    boolean required() default true;
}
