package dev.langchain4j.agentic.scope;

/**
 * Allow to access the {@link AgenticScope} of any agent extending it.
 * 允许访问任何扩展它的代理的｛@link AgentScope｝。
 */
public interface AgenticScopeAccess {

    /**
     * Returns the {@link AgenticScope} with the given id for this AI service or null if such memory doesn't exist.
     * 返回具有此AI服务给定id的{@link AgentScope}，如果不存在此类内存，则返回null。
     *
     * @param memoryId The id of the {@link AgenticScope}.
     * @return The {@link AgenticScope} with the given memoryId or null if such memory doesn't exist.
     */
    AgenticScope getAgenticScope(Object memoryId);

    /**
     * Evicts the {@link AgenticScope} with the given id.
     * 删除具有给定id的｛@link AgentScope｝。
     *
     * @param memoryId The id of the {@link AgenticScope} to be evicted.
     * @return true if {@link AgenticScope} with the given id existed, and it was successfully evicted, false otherwise.
     */
    boolean evictAgenticScope(Object memoryId);
}
