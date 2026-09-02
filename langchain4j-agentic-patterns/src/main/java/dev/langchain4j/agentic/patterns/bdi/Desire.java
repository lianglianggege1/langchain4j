package dev.langchain4j.agentic.patterns.bdi;

import dev.langchain4j.agentic.scope.AgenticScope;

import java.util.List;
import java.util.function.Predicate;

/**
 * A prioritized goal for the {@link BDIPlanner}. Each desire declares when it is achievable, when
 * it is satisfied, and the ordered sequence of agent types that form its intention. Higher priority
 * values take precedence; among equal priorities, declaration order wins (stable ordering).
 *
 * @param name       human-readable label, used in log messages and error diagnostics
 * @param priority   higher value = more important; strictly higher priority triggers preemption
 * @param achievable predicate on {@link dev.langchain4j.agentic.scope.AgenticScope} — can this desire be pursued now?
 * @param satisfied  predicate on {@link dev.langchain4j.agentic.scope.AgenticScope} — has this desire been achieved?
 * @param agentTypes ordered agent classes forming the intention; resolved to instances at init time
 */
/**
 * {@link BDIPlanner}使用的带优先级目标。每个愿望会定义自身何时可执行、何时已完成，以及构成其意图的有序代理类型序列。数值越大优先级越高；优先级相同时，按声明先后顺序选择（顺序稳定）。
 *
 * @param name 可读名称，用于日志与错误诊断
 * @param priority 优先级，数值越大越重要；严格更高优先级会触发抢占
 * @param achievable 基于{@link dev.langchain4j.agentic.scope.AgenticScope}的断言：当前是否可以执行该愿望
 * @param satisfied 基于{@link dev.langchain4j.agentic.scope.AgenticScope}的断言：该愿望是否已经达成
 * @param agentTypes 构成意图的有序代理类；初始化时解析为实例对象
 */
public record Desire(String name, int priority,
                     Predicate<AgenticScope> achievable,
                     Predicate<AgenticScope> satisfied,
                     List<Class<?>> agentTypes) {

    public Desire {
        if (agentTypes == null || agentTypes.isEmpty()) {
            throw new IllegalArgumentException("Desire '" + name + "' must have at least one agent type");
        }
    }

    public static Desire of(String name, int priority,
                            Predicate<AgenticScope> achievable,
                            Predicate<AgenticScope> satisfied,
                            Class<?>... agentTypes) {
        return new Desire(name, priority, achievable, satisfied, List.of(agentTypes));
    }

    public static Desire of(String name, int priority,
                            String achievableStateKey,
                            String satisfiedStateKey,
                            Class<?>... agentTypes) {
        return new Desire(name, priority,
                scope -> scope.hasState(achievableStateKey),
                scope -> scope.hasState(satisfiedStateKey),
                List.of(agentTypes));
    }
}
