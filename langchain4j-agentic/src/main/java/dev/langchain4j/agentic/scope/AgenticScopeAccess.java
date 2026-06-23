package dev.langchain4j.agentic.scope;

/**
 * Allow to access the {@link AgenticScope} of any agent extending it.
 */
/**
 * 允许访问继承该类的所有智能体的{@link AgenticScope}作用域。
 */
public interface AgenticScopeAccess {

    /**
     * Returns the {@link AgenticScope} with the given id for this AI service or null if such memory doesn't exist.
     *
     * @param memoryId The id of the {@link AgenticScope}.
     * @return The {@link AgenticScope} with the given memoryId or null if such memory doesn't exist.
     */
    /**
     * 根据指定标识返回当前AI服务对应的{@link AgenticScope}，若不存在则返回null。
     *
     * @param memoryId {@link AgenticScope}的标识
     * @return 对应标识的{@link AgenticScope}，不存在则返回null
     */
    AgenticScope getAgenticScope(Object memoryId);

    /**
     * Evicts the {@link AgenticScope} with the given id.
     *
     * @param memoryId The id of the {@link AgenticScope} to be evicted.
     * @return true if {@link AgenticScope} with the given id existed, and it was successfully evicted, false otherwise.
     */
    /**
     * 移除指定标识的{@link AgenticScope}。
     *
     * @param memoryId 待移除的{@link AgenticScope}标识
     * @return 存在并成功移除返回true，否则返回false
     */
    boolean evictAgenticScope(Object memoryId);
}
