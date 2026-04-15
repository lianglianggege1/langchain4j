package dev.langchain4j.agentic.declarative;

import dev.langchain4j.agentic.Agent;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a method as a human-in-the-loop agent. The method can be invoked to get input or feedback from a human during the execution of a workflow.
 * 将方法标记为循环中的人工代理。在工作流执行过程中，可以调用该方法来获取人类的输入或反馈。
 * The annotated method can have any number of parameters, which will be provided by the workflow context when the method is invoked.
 * 带注释的方法可以有任意数量的参数，这些参数将在调用该方法时由工作流上下文提供。
 * <p>
 * Example:
 * <pre>
 * {@code
 *     public interface AudienceRetriever {
 *
 *         @HumanInTheLoop(description = "Generate a story based on the given topic", outputKey = "audience", async = true)
 *         static String request(@V("topic") String topic) {
 *             request.set("Which audience for topic " + topic + "?");
 *             return System.console().readLine();
 *         }
 *     }
 * }
 * </pre>
 */
@Retention(RUNTIME)
@Target({METHOD})
public @interface HumanInTheLoop {

    /**
     * Name of the agent. If not provided, method name will be used.
     * 代理人的姓名。如果没有提供，将使用方法名称。
     *
     * @return name of the agent.
     */
    String name() default "";

    /**
     * Description of the agent. This is an alias of the {@code description} attribute, and it is possible to use either.
     * It should be clear and descriptive to allow language model to understand the agent's purpose and its intended use.
     *
     * @return description of the agent.
     */
    String value() default "";

    /**
     * Description of the agent. This is an alias of the {@code value} attribute, and it is possible to use either.
     * It should be clear and descriptive to allow language model to understand the agent's purpose and its intended use.
     *
     * @return description of the agent.
     */
    String description() default "";

    /**
     * Key of the output variable that will be used to store the result of the agent's invocation.
     * 用于存储代理调用结果的输出变量的键。
     *
     * @return name of the output variable.
     */
    String outputKey() default "";

    /**
     * Strongly typed key of the output variable that will be used to store the result of the agent's invocation.
     * It enforces type safety when retrieving the output from the agent's state and can be used in alternative
     * to the {@code outputKey()} attribute. Note that only one of those two attributes can be used at a time.
     *
     * @return class representing the typed output variable.
     */
    Class<? extends TypedKey<?>> typedOutputKey() default Agent.NoTypedKey.class;

    /**
     * If true, the agent will be invoked in an asynchronous manner, allowing the workflow to continue without waiting for the agent's result.
     *
     * @return true if the agent should be invoked in an asynchronous manner, false otherwise.
     */
    boolean async() default false;
}
