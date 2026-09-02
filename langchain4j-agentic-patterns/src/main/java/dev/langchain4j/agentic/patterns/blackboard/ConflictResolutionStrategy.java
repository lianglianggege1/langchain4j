package dev.langchain4j.agentic.patterns.blackboard;

import dev.langchain4j.agentic.planner.AgentInstance;
import dev.langchain4j.agentic.scope.AgenticScope;

import java.util.List;
import java.util.function.Predicate;

/**
 * Strategy for resolving conflicts when multiple agents can fire simultaneously on the blackboard.
 * The strategy receives the current scope state and all candidate agents that are ready to fire,
 * and returns which one should be activated.
 */
/**
 * 冲突解决策略，用于黑板模式下多个代理同时就绪可执行时的冲突处理。
 * 该策略接收当前作用域状态以及所有就绪待执行的候选代理，返回应当被激活的代理。
 */
@FunctionalInterface
public interface ConflictResolutionStrategy {

    /**
     * Selects the first candidate, preserving the declaration order used in the {@code subAgents} method.
     */
    /**
     * 选择第一个候选代理，保持 {@code subAgents} 方法中定义的声明顺序。
     */
    ConflictResolutionStrategy DECLARATION_ORDER = (scope, candidates) -> candidates.get(0);

    AgentInstance resolve(AgenticScope scope, List<AgentInstance> candidates);

    /**
     * Returns a strategy that selects the first candidate in declaration order.
     * This is the default strategy used by {@link BlackboardPlanner} if no strategy is provided.
     */
    /**
     * 返回按声明顺序选取第一个候选代理的策略。
     * 如果未指定策略，该策略即为 {@link BlackboardPlanner} 的默认策略。
     */
    static ConflictResolutionStrategy declarationOrder() {
        return DECLARATION_ORDER;
    }

    /**
     * Returns a strategy that selects the candidate matching {@code agentType} only when {@code condition}
     * is satisfied, or {@code null} otherwise. Intended to be chained with {@link #or(ConflictResolutionStrategy)}.
     */
    /**
     * 返回一种策略：仅当 {@code condition} 条件成立时，选取匹配 {@code agentType} 的候选代理；否则返回 {@code null}。
     * 该策略设计用于与 {@link #or(ConflictResolutionStrategy)} 进行链式组合。
     */
    static ConflictResolutionStrategy agentOfType(Class<?> agentType, Predicate<AgenticScope> condition) {
        return selectAgent(a -> a.type() == agentType, condition);
    }

    /**
     * Returns a strategy that unconditionally selects the candidate matching {@code agentType},
     * or {@code null} if no candidate of that type is present.
     */
    /**
     * 返回一种策略：无条件选取匹配 {@code agentType} 的候选代理；
     * 如果不存在该类型的候选代理，则返回 {@code null}。
     */
    static ConflictResolutionStrategy agentOfType(Class<?> agentType) {
        return selectAgent(a -> a.type() == agentType);
    }

    /**
     * Returns a strategy that selects the candidate matching {@code agentName} only when {@code condition}
     * is satisfied, or {@code null} otherwise. Intended to be chained with {@link #or(ConflictResolutionStrategy)}.
     */
    /**
     * 返回一种策略：仅当 {@code condition} 条件成立时，选取匹配 {@code agentName} 的候选代理；否则返回 {@code null}。
     * 该策略设计用于与 {@link #or(ConflictResolutionStrategy)} 进行链式组合。
     */
    static ConflictResolutionStrategy agentWithName(String agentName, Predicate<AgenticScope> condition) {
        return selectAgent(a -> agentName.equals(a.name()), condition);
    }

    /**
     * Returns a strategy that unconditionally selects the candidate matching {@code agentName},
     * or {@code null} if no candidate with that name is present.
     */
    /**
     * 返回一种策略：无条件选取匹配 {@code agentName} 的候选代理；
     * 如果不存在该名称的候选代理，则返回 {@code null}。
     */
    static ConflictResolutionStrategy agentWithName(String agentName) {
        return selectAgent(a -> agentName.equals(a.name()));
    }

    /**
     * Returns a strategy that selects the first candidate matching {@code agentFilter} only when
     * {@code condition} is satisfied, or {@code null} otherwise.
     * Intended to be chained with {@link #or(ConflictResolutionStrategy)}.
     */
    /**
     * 返回一种策略：仅当 {@code condition} 条件成立时，选取第一个匹配 {@code agentFilter} 的候选代理；否则返回 {@code null}。
     * 该策略设计用于与 {@link #or(ConflictResolutionStrategy)} 进行链式组合。
     */
    static ConflictResolutionStrategy selectAgent(Predicate<AgentInstance> agentFilter, Predicate<AgenticScope> condition) {
        return (scope, candidates) -> {
            if (condition.test(scope)) {
                return selectAgent(agentFilter).resolve(scope, candidates);
            }
            return null;
        };
    }

    /**
     * Returns a strategy that unconditionally selects the first candidate matching {@code agentFilter},
     * or {@code null} if no candidate matches.
     */
    /**
     * 返回一种策略：无条件选取第一个匹配 {@code agentFilter} 的候选代理；
     * 若无匹配的候选代理，则返回 {@code null}。
     */
    static ConflictResolutionStrategy selectAgent(Predicate<AgentInstance> agentFilter) {
        return (scope, candidates) -> candidates.stream()
                .filter(agentFilter)
                .findFirst()
                .orElse(null);
    }

    /**
     * Chains this strategy with a fallback: if this strategy returns {@code null},
     * the {@code other} strategy is applied instead.
     */
    /**
     * 将当前策略与备用策略进行链式组合：若当前策略返回 {@code null}，则改用 {@code other} 策略执行。
     */
    default ConflictResolutionStrategy or(ConflictResolutionStrategy other) {
        return (scope, candidates) -> {
            AgentInstance result = this.resolve(scope, candidates);
            if (result != null) {
                return result;
            }
            return other.resolve(scope, candidates);
        };
    }
}
