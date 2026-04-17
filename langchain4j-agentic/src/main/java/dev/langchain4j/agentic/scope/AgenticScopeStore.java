package dev.langchain4j.agentic.scope;

import java.util.Optional;
import java.util.Set;

/**
 * Service Provider Interface for AgenticScope persistence.
 * Implementations must provide ways to store and retrieve AgenticScope instances.
 * 用于AgentScope持久化的服务提供程序接口。
 * 实现必须提供存储和检索AgentScope实例的方法。
 */
public interface AgenticScopeStore {

    /**
     * Saves or updates a AgenticScope instance.
     * 保存或更新AgentScope实例。
     *
     * @param agenticScope the AgenticScope to persist
     * @return true if the operation was successful
     */
    boolean save(AgenticScopeKey key, DefaultAgenticScope agenticScope);

    /**
     * Loads a AgenticScope by its ID.
     * 按ID加载AgentScope。
     *
     * @param key the ID of the AgenticScope to load
     * @return an Optional containing the AgenticScope if found, empty otherwise
     */
    Optional<DefaultAgenticScope> load(AgenticScopeKey key);

    /**
     * Deletes a AgenticScope by its ID.
     * 按ID删除AgentScope。
     *
     * @param key the ID of the AgenticScope to delete
     * @return true if the AgenticScope was found and deleted
     */
    boolean delete(AgenticScopeKey key);

    /**
     * Gets all available AgenticScope .
     * 获取所有可用的AgentScope。
     *
     * @return a Set of all AgenticScope keys in the persistence store
     */
    Set<AgenticScopeKey> getAllKeys();
}
