package dev.langchain4j.agentic.workflow;

import dev.langchain4j.agentic.planner.AgenticService;
import java.util.concurrent.Executor;

// 并行agent服务
public interface ParallelMapperService<T> extends AgenticService<ParallelMapperService<T>, T> {

    ParallelMapperService<T> executor(Executor executor);

    ParallelMapperService<T> itemsProvider(String itemsProvider);
}
