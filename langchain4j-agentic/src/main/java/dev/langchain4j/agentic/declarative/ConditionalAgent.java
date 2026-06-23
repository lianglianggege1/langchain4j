package dev.langchain4j.agentic.declarative;

import dev.langchain4j.agentic.Agent;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a method as a definition of a conditional agent, generally used to route the agentic workflow toward
 * one or more sub-agents according to the verification of their activation conditions.
 * Each sub-agent has its own activation predicate, a static method annotated with {@link ActivationCondition} that
 * determines when it should be invoked.
 * 将方法标记为条件代理的定义，通常用于根据其激活条件的验证将代理工作流路由到一个或多个子代理。
 * 每个子代理都有自己的激活谓词，这是一个用{@link ActivationCondition}注释的静态方法，用于确定何时调用它。
 * <p>
 * Example:
 * 例子：
 * <pre>
 * {@code
 *     public interface ExpertsAgent {
 *
 *         @ConditionalAgent(outputKey = "response",
 *                           subAgents = { MedicalExpert.class, TechnicalExpert.class, LegalExpert.class } )
 *         String askExpert(@V("request") String request);
 *
 *         @ActivationCondition(MedicalExpert.class)
 *         static boolean activateMedical(@V("category") RequestCategory category) {
 *             return category == RequestCategory.MEDICAL;
 *         }
 *
 *         @ActivationCondition(TechnicalExpert.class)
 *         static boolean activateTechnical(@V("category") RequestCategory category) {
 *             return category == RequestCategory.TECHNICAL;
 *         }
 *
 *         @ActivationCondition(LegalExpert.class)
 *         static boolean activateLegal(AgenticScope agenticScope) {
 *             return agenticScope.readState("category", RequestCategory.UNKNOWN) == RequestCategory.LEGAL;
 *         }
 *     }
 * }
 * </pre>
 */
/**
 * 将方法标记为条件智能体定义。该注解一般用于依据激活条件的判定结果，
 * 将智能体工作流路由至一个或多个子智能体。
 * 每个子智能体均配备专属激活断言，该断言为被 {@link ActivationCondition} 注解修饰的静态方法，
 * 用以判定当前子智能体的执行时机。
 * <p>
 * 示例：
 * <pre>
 * {@code
 *     public interface ExpertsAgent {
 *
 *         @ConditionalAgent(outputKey = "response",
 *                           subAgents = { MedicalExpert.class, TechnicalExpert.class, LegalExpert.class } )
 *         String askExpert(@V("request") String request);
 *
 *         @ActivationCondition(MedicalExpert.class)
 *         static boolean activateMedical(@V("category") RequestCategory category) {
 *             return category == RequestCategory.MEDICAL;
 *         }
 *
 *         @ActivationCondition(TechnicalExpert.class)
 *         static boolean activateTechnical(@V("category") RequestCategory category) {
 *             return category == RequestCategory.TECHNICAL;
 *         }
 *
 *         @ActivationCondition(LegalExpert.class)
 *         static boolean activateLegal(AgenticScope agenticScope) {
 *             return agenticScope.readState("category", RequestCategory.UNKNOWN) == RequestCategory.LEGAL;
 *         }
 *     }
 * }
 * </pre>
 */
@Retention(RUNTIME)
@Target({METHOD})
public @interface ConditionalAgent {

    /**
     * Name of the agent. If not provided, method name will be used.
     * agent的姓名。如果没有提供，将使用方法名称。
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
     * 用于存储代理调用结果的输出变量的键。
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
    /**
     * 用于存储智能体调用结果的输出变量强类型键。
     * 从智能体状态中读取输出时可保证类型安全，可替代 {@code outputKey()} 属性使用。
     * 注意：同一时刻仅能使用二者之一。
     *
     * @return 表示强类型输出变量的类对象
     */
    Class<? extends TypedKey<?>> typedOutputKey() default Agent.NoTypedKey.class;

    /**
     * Sub-agents that can be conditionally activated by this agent.
     * 可由该代理有条件激活的子代理。
     *
     * @return array of sub-agents.
     */
    Class<?>[] subAgents();
}
