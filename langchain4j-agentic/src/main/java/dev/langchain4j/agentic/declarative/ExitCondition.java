package dev.langchain4j.agentic.declarative;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a method as an exit predicate for a loop in a loop-based agent.
 * 将方法标记为基于循环的代理中循环的退出谓词。
 * The method must be static and return a boolean indicating whether the loop should exit.
 * 该方法必须是静态的，并返回一个布尔值，指示循环是否应该退出。
 * <p>
 * Example:
 * <pre>
 * {@code
 *      public interface StyleReviewLoopAgentWithCounter {
 *
 *         @LoopAgent(
 *                 description = "Review the given story to ensure it aligns with the specified style",
 *                 outputKey = "story", maxIterations = 5,
 *                 subAgents = {
 *                     @SubAgent(type = StyleScorer.class, outputKey = "score"),
 *                     @SubAgent(type = StyleEditor.class, outputKey = "story")
 *             }
 *         )
 *         String write(@V("story") String story);
 *
 *         @ExitCondition(testExitAtLoopEnd = true, description = "score greater than 0.8")
 *         static boolean exit(@V("score") double score) {
 *             return score >= 0.8;
 *         }
 *     }
 * }
 * </pre>
 */
@Retention(RUNTIME)
@Target({METHOD})
public @interface ExitCondition {

    /**
     * If true, the exit predicate will be tested only at the end of each loop iteration.
     * 如果为真，则仅在每次循环迭代结束时测试退出谓词。
     * If false, the exit predicate will be tested after each sub-agent invocation.
     * 如果为false，则退出谓词将在每次子代理调用后进行测试。
     * Default is false.
     * 默认为false。
     *
     * @return whether to test the exit predicate at the end of the loop iteration.
     * @return 是否在循环迭代结束时测试退出谓词。
     */
    boolean testExitAtLoopEnd() default false;

    /**
     * Description of the exit condition.
     * 退出条件的描述。
     * It should be clear and descriptive to allow understanding the purpose of the condition.
     * 它应该是清晰和描述性的，以便理解条件的目的。
     *
     * @return description of the exit condition.
     */
    String description() default "<unknown>";
}
