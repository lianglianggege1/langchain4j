package dev.langchain4j.agentic.declarative;

import dev.langchain4j.agentic.Agent;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a method as a definition of a loop agent, used to orchestrate the agentic workflow
 * by invoking a series of sub-agents in a loop until a certain predicate is met or a maximum number of iterations is reached.
 * <p>
 * Example:
 * <pre>
 * {@code
 *     public interface StyleReviewLoopAgentWithCounter {
 *
 *         @LoopAgent(
 *                 description = "Review the given story to ensure it aligns with the specified style",
 *                 outputKey = "story", maxIterations = 5,
 *                 subAgents = { StyleScorer.class, StyleEditor.class }
 *         )
 *         String write(@V("story") String story);
 *     }
 * }
 * </pre>
 */
/**
 * 将方法标记为循环智能体定义，用于编排智能体工作流。
 * 会循环调用一系列子智能体，直到满足指定退出断言或达到最大迭代次数为止。
 * <p>
 * 示例：
 * <pre>
 * {@code
 *     public interface StyleReviewLoopAgentWithCounter {
 *
 *         @LoopAgent(
 *                 description = "审阅给定故事，确保其符合指定风格",
 *                 outputKey = "story", maxIterations = 5,
 *                 subAgents = { StyleScorer.class, StyleEditor.class }
 *         )
 *         String write(@V("story") String story);
 *     }
 * }
 * </pre>
 */
@Retention(RUNTIME)
@Target({METHOD})
public @interface LoopAgent {

    /**
     * Name of the agent. If not provided, method name will be used.
     *
     * @return name of the agent.
     */
    String name() default "";

    /**
     * Description of the agent.
     * It should be clear and descriptive to allow language model to understand the agent's purpose and its intended use.
     *
     * @return description of the agent.
     */
    String description() default "";

    /**
     * Key of the output variable that will be used to store the result of the agent's invocation.
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
     * Array of sub-agents that will be invoked in parallel.
     *
     * @return array of sub-agents.
     */
    Class<?>[] subAgents();

    /**
     * Maximum number of iterations the loop will execute.
     * 循环将执行的最大迭代次数。
     * If the exit predicate is not met within this number of iterations, the loop will terminate.
     * 如果在此迭代次数内未满足退出谓词，则循环将终止。
     *
     * @return maximum number of iterations.
     */
    int maxIterations() default 10;
}
