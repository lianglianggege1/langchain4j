package dev.langchain4j.service.memory;

import dev.langchain4j.memory.ChatMemory;

/**
 * Allow to access the {@link ChatMemory} of any AI service extending it.
 * 允许访问任何扩展它的AI服务的{@link ChatMemory}。
 */
public interface ChatMemoryAccess {

    /**
     * Returns the {@link ChatMemory} with the given id for this AI service or null if such memory doesn't exist.
     * 返回具有此AI服务给定id的{@link ChatMemory}，如果不存在此类内存，则返回null。
     *
     * @param memoryId The id of the chat memory.
     * @return The {@link ChatMemory} with the given memoryId or null if such memory doesn't exist.
     */
    ChatMemory getChatMemory(Object memoryId);

    /**
     * Evicts the {@link ChatMemory} with the given id.
     * 删除具有给定id的｛@link ChatMemory｝。
     *
     * @param memoryId The id of the chat memory to be evicted.
     * @return true if {@link ChatMemory} with the given id existed, and it was successfully evicted, false otherwise.
     */
    boolean evictChatMemory(Object memoryId);
}
