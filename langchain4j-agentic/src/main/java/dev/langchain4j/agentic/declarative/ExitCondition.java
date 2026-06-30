package dev.langchain4j.agentic.declarative;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a method as an exit predicate for a loop in a loop-based agent.
 * The method must be static and return a boolean indicating whether the loop should exit.
 * <p>
 * Example:
 * <pre>
 * {@code
 *      public interface StyleReviewLoopAgentWithCounter {
 *
 *         @LoopAgent(
 *                 description = "Review the given story to ensure it aligns with the specified style",
 *                 outputKey = "story", maxIterations = 5,
 *                 subAgents = { StyleScorer.class, StyleEditor.class })
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
/**
 * 将方法标记为循环型智能体的循环退出断言。
 * 该方法必须为静态方法，返回布尔值，用于判定是否退出循环。
 * <p>
 * 示例：
 * <pre>
 * {@code
 *      public interface StyleReviewLoopAgentWithCounter {
 *
 *         @LoopAgent(
 *                 description = "审阅指定故事，确保其符合既定风格",
 *                 outputKey = "story", maxIterations = 5,
 *                 subAgents = {
 *                     @SubAgent(type = StyleScorer.class, outputKey = "score"),
 *                     @SubAgent(type = StyleEditor.class, outputKey = "story")
 *             }
 *         )
 *         String write(@V("story") String story);
 *
 *         @ExitCondition(testExitAtLoopEnd = true, description = "得分大于0.8")
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
     * If false, the exit predicate will be tested after each sub-agent invocation.
     * Default is false.
     *
     * @return whether to test the exit predicate at the end of the loop iteration.
     * @return 是否在循环迭代结束时测试退出谓词。
     */
    /**
     * 若为 true，仅在每次循环迭代结束时校验退出断言。
     * 若为 false，每个子智能体调用完成后都会校验退出断言。
     * 默认值为 false。
     *
     * @return 是否在循环迭代结束时执行退出断言校验
     */
    boolean testExitAtLoopEnd() default false;

    /**
     * Description of the exit condition.
     * It should be clear and descriptive to allow understanding the purpose of the condition.
     *
     * @return description of the exit condition.
     */
    /**
     * 退出条件的描述信息。
     * 应清晰、详尽，便于理解该条件的作用与目的。
     *
     * @return 退出条件的描述
     */
    String description() default "<unknown>";
}
