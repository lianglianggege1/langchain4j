package dev.langchain4j.agentic.planner;

import java.util.function.Supplier;

// 基于规划的服务
public interface PlannerBasedService<T> extends AgenticService<PlannerBasedService<T>, T> {

    // 规划
    PlannerBasedService<T> planner(Supplier<Planner> plannerSupplier);

}
