package dev.langchain4j.store.memory.chat;

import dev.langchain4j.data.message.ChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link ChatMemoryStore} that stores state of {@link dev.langchain4j.memory.ChatMemory} (chat messages) in-memory.
 * <p>
 * This storage mechanism is transient and does not persist data across application restarts.
 */
/**
 * {@link ChatMemoryStore} 的实现类，将 {@link dev.langchain4j.memory.ChatMemory} 的状态（聊天消息）存储在内存中。
 * <p>
 * 该存储机制属于临时存储，应用重启后数据不会持久化保留。
 */
public class InMemoryChatMemoryStore implements ChatMemoryStore {

    private final Map<Object, List<ChatMessage>> messagesByMemoryId = new ConcurrentHashMap<>();

    /**
     * Constructs a new {@link InMemoryChatMemoryStore}.
     */
    public InMemoryChatMemoryStore() {}

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        return messagesByMemoryId.computeIfAbsent(memoryId, ignored -> new ArrayList<>());
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        messagesByMemoryId.put(memoryId, messages);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        messagesByMemoryId.remove(memoryId);
    }
}
