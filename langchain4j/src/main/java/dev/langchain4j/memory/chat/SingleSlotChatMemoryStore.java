package dev.langchain4j.memory.chat;

import dev.langchain4j.Internal;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link ChatMemoryStore} that stores state of {@link ChatMemory} (chat messages) in-memory.
 * <p>
 * This storage mechanism is transient and does not persist data across application restarts.
 */
/**
 * {@link ChatMemoryStore} 的实现类，将 {@link ChatMemory} 的状态（聊天消息）存储在内存中。
 * <p>
 * 该存储机制为临时存储，不会在应用重启后保留数据。
 */
@Internal
class SingleSlotChatMemoryStore implements ChatMemoryStore {

    private List<ChatMessage> messages = new ArrayList<>();

    private final Object memoryId;

    public SingleSlotChatMemoryStore(final Object memoryId) {
        this.memoryId = memoryId;
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        checkMemoryId(memoryId);
        return messages;
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        checkMemoryId(memoryId);
        this.messages = messages;
    }

    @Override
    public void deleteMessages(Object memoryId) {
        checkMemoryId(memoryId);
        this.messages = new ArrayList<>();
    }

    private void checkMemoryId(Object memoryId) {
        if (!this.memoryId.equals(memoryId)) {
            throw new IllegalStateException("This chat memory has id: " + this.memoryId +
                    " but an operation has been requested on a memory with id: " + memoryId);
        }
    }
}
