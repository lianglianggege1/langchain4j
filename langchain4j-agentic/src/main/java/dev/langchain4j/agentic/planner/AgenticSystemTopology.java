package dev.langchain4j.agentic.planner;

public enum AgenticSystemTopology {
    // ai agent
    AI_AGENT,
    // non-ai agent
    NON_AI_AGENT,
    // human in the loop
    HUMAN_IN_THE_LOOP,
    // sequence
    SEQUENCE,
    // parallel
    PARALLEL,
    // loop
    LOOP,
    // router
    ROUTER,
    // star
    STAR
}
