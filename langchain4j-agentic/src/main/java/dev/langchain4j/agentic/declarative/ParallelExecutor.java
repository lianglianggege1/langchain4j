package dev.langchain4j.agentic.declarative;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used in combination with {@link ParallelAgent} to specify the executor that will be used to run the sub-agents in parallel.
 * The method annotated with {@link ParallelExecutor} must be static and return an instance of {@link java.util.concurrent.Executor}.
 * 与ParallelAgent结合使用，指定将用于并行运行子代理的执行器。
 * 用ParallelExecutor注释的方法必须是静态的，并返回一个java实例{@link java.util.concurrent.Executor}.
 * 同时发生的。
 * <p>
 * Example:
 * <pre>
 * {@code
 *     public interface EveningPlannerAgent {
 *
 *         @ParallelAgent(outputKey = "plans", subAgents = {
 *                 @SubAgent(type = FoodExpert.class, outputKey = "meals"),
 *                 @SubAgent(type = MovieExpert.class, outputKey = "movies")
 *         })
 *         List<EveningPlan> plan(@V("mood") String mood);
 *
 *         @ParallelExecutor
 *         static Executor executor() {
 *             return Executors.newFixedThreadPool(2);
 *         }
 *     }
 * }
 * </pre>
 */
// 并行执行器
@Retention(RUNTIME)
@Target({METHOD})
public @interface ParallelExecutor {}
