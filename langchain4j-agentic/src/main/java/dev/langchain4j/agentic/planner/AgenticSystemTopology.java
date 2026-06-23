package dev.langchain4j.agentic.planner;

public enum AgenticSystemTopology {
    // ai agent 人工智能代理
    AI_AGENT,
    // non-ai agent 非人工智能代理
    NON_AI_AGENT,
    // human in the loop 人机交互
    HUMAN_IN_THE_LOOP,
    // sequence 串行
    SEQUENCE,
    // parallel 并行
    PARALLEL,
    // loop 循环
    LOOP,
    // router 路由
    ROUTER,
    // star 星形
    STAR
}
