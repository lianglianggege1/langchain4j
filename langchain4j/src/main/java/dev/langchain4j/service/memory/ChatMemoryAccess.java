package dev.langchain4j.service.memory;

import dev.langchain4j.memory.ChatMemory;

/**
 * Allow to access the {@link ChatMemory} of any AI service extending it.
 */
/**
 * 允许访问继承此类的所有AI服务的{@link ChatMemory}。
 */
public interface ChatMemoryAccess {

    /**
     * Returns the {@link ChatMemory} with the given id for this AI service or null if such memory doesn't exist.
     *
     * @param memoryId The id of the chat memory.
     * @return The {@link ChatMemory} with the given memoryId or null if such memory doesn't exist.
     */
    /**
     * 根据指定ID返回当前AI服务对应的{@link ChatMemory}，若不存在则返回null。
     *
     * @param memoryId 对话内存的ID
     * @return 对应ID的{@link ChatMemory}，不存在则返回null
     */
    ChatMemory getChatMemory(Object memoryId);

    /**
     * Evicts the {@link ChatMemory} with the given id.
     *
     * @param memoryId The id of the chat memory to be evicted.
     * @return true if {@link ChatMemory} with the given id existed, and it was successfully evicted, false otherwise.
     */
    /**
     * 移除指定ID的{@link ChatMemory}。
     *
     * @param memoryId 待移除的对话内存ID
     * @return 存在且成功移除返回true，否则返回false
     */
    boolean evictChatMemory(Object memoryId);
}
