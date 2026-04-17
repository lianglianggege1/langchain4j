package dev.langchain4j.agentic.workflow;

import dev.langchain4j.agentic.planner.AgenticService;
import dev.langchain4j.agentic.scope.AgenticScope;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

// 循环agent服务
public interface LoopAgentService<T> extends AgenticService<LoopAgentService<T>, T> {

    // 循环次数
    LoopAgentService<T> maxIterations(int maxIterations);

    // 退出条件
    LoopAgentService<T> exitCondition(Predicate<AgenticScope> exitCondition);
    // 退出条件
    LoopAgentService<T> exitCondition(BiPredicate<AgenticScope, Integer> exitCondition);

    LoopAgentService<T> exitCondition(String exitConditionDescription, Predicate<AgenticScope> exitCondition);
    LoopAgentService<T> exitCondition(String exitConditionDescription, BiPredicate<AgenticScope, Integer> exitCondition);

    LoopAgentService<T> testExitAtLoopEnd(boolean checkExitConditionAtLoopEnd);
}
