package dev.langchain4j.agentic;

import dev.langchain4j.agentic.declarative.TypedKey;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Java methods annotated with {@code @Agent} are considered agents that other agents can invoke.
 * 用{@code@Agent}注释的Java方法被认为是其他代理可以调用的代理。
 */
@Retention(RUNTIME)
@Target({METHOD})
public @interface Agent {

    /**
     * Name of the agent. If not provided, method name will be used.
     * 一个agent的名字，如果未提供，将使用方法名称
     *
     * @return name of the agent.
     */
    String name() default "";

    /**
     * Description of the agent. This is an alias of the {@code description} attribute, and it is possible to use either.
     * It should be clear and descriptive to allow language model to understand the agent's purpose and its intended use.
     * agent的描述。
     * 这是描述属性的别名，可以使用其中任何一个。
     * 它应该是清晰和描述性的，以便语言模型理解agent的目的及其预期用途。
     *
     * @return description of the agent.
     */
    String value() default "";

    /**
     * Description of the agent. This is an alias of the {@code value} attribute, and it is possible to use either.
     * It should be clear and descriptive to allow language model to understand the agent's purpose and its intended use.
     * agent的描述。这是value属性的别名，可以使用其中任何一个。
     * 它应该是清晰和描述性的，以便语言模型理解代理的目的及其预期用途。
     * @return description of the agent.
     */
    String description() default "";

    /**
     * Key of the output variable that will be used to store the result of the agent's invocation.
     * 用于存储agent调用结果的输出变量的键。
     *
     * @return name of the output variable.
     */
    String outputKey() default "";

    Class<? extends TypedKey<?>> typedOutputKey() default NoTypedKey.class;

    /**
     * If true, the agent will be invoked in an asynchronous manner, allowing the workflow to continue without waiting for the agent's result.
     * 如果为true，则agent将采用异步方式调用，允许工作流继续而不等待代理的结果。
     *
     * @return true if the agent should be invoked in an asynchronous manner, false otherwise.
     */
    boolean async() default false;

    /**
     * If true, the agent's execution will be silently skipped when any of its arguments is missing in the agentic scope,
     * instead of making the agentic system's execution fail.
     *
     * @return true if the agent is optional, false otherwise.
     */
    boolean optional() default false;

    /**
     * Names of other agents participating in the definition of the context of this agent.
     * 参与此agent上下文定义的其他代理的名称。
     *
     * @return array of names of other agents participating in the definition of the context of this agent.
     */
    String[] summarizedContext() default {};

    class NoTypedKey implements TypedKey<Void> { }
}
