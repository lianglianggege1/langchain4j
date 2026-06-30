package dev.langchain4j.agentic.declarative;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used in combination with {@link ParallelAgent} or {@link ParallelMapperAgent} to specify the executor that will be used
 * to run the sub-agents in parallel.
 * The method annotated with {@link ParallelExecutor} must be static and return an instance of {@link java.util.concurrent.Executor}.
 * It can optionally declare parameters resolved through {@link SupplierParameterResolver}.
 * <p>
 * Example:
 * <pre>
 * {@code
 *     public interface EveningPlannerAgent {
 *
 *         @ParallelAgent(outputKey = "plans",
 *                 subAgents = { FoodExpert.class, MovieExpert.class })
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
/**
 * 与 {@link ParallelAgent} 配合使用，用于指定并行执行子智能体时所用的执行器。
 * 被 {@link ParallelExecutor} 注解的方法必须是静态方法，且返回 {@link java.util.concurrent.Executor} 实例。
 * <p>
 * 示例：
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
@Retention(RUNTIME)
@Target({METHOD})
public @interface ParallelExecutor {}
