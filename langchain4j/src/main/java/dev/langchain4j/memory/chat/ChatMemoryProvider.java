package dev.langchain4j.memory.chat;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.service.MemoryId;

/**
 * Provides instances of {@link ChatMemory}.
 * Intended to be used with {@link dev.langchain4j.service.AiServices}.
 */
/**
 * 提供 {@link ChatMemory} 实例。
 * 旨在与 {@link dev.langchain4j.service.AiServices} 配合使用。
 */
@FunctionalInterface
public interface ChatMemoryProvider {

    /**
     * Provides an instance of {@link ChatMemory}.
     * This method is called each time an AI Service method (having a parameter annotated with {@link MemoryId})
     * is called with a previously unseen memory ID.
     * Once the {@link ChatMemory} instance is returned, it's retained in memory and managed by {@link dev.langchain4j.service.AiServices}.
     *
     * @param memoryId The ID of the chat memory.
     * @return A {@link ChatMemory} instance.
     * @see MemoryId
     */
    /**
     * 提供一个 {@link ChatMemory} 实例。
     * 每次 AI 服务方法（带有 {@link MemoryId} 注解的参数）
     * 使用之前未见过的内存 ID 进行调用时，都会触发此方法。
     * 一旦 {@link ChatMemory} 实例被返回，它将保留在内存中并由 {@link dev.langchain4j.service.AiServices} 管理。
     *
     * @param memoryId 聊天内存的 ID
     * @return {@link ChatMemory} 实例
     * @see MemoryId
     */
    ChatMemory get(Object memoryId);
}
