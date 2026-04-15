package dev.langchain4j.agentic.declarative;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a parameter that will receive the current loop iteration count in a loop-based agent.
 * 标记一个参数，该参数将在基于循环的代理中接收当前循环迭代计数。
 * The parameter must be of type int or Integer.
 * 参数的类型必须为int或Integer。
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
 *         String write(@V("story") String story, @LoopCounter int iteration);
 *
 *         @ExitCondition
 *         static boolean exit(@V("score") double score, @LoopCounter int loopCounter) {
 *             return loopCounter <= 3 ? score >= 0.8 : score >= 0.6;
 *         }
 *     }
 * }
 * </pre>
 */
@Retention(RUNTIME)
@Target({PARAMETER})
public @interface LoopCounter {}
