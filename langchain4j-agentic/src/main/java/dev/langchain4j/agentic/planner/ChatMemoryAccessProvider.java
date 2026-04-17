package dev.langchain4j.agentic.planner;

import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.service.memory.ChatMemoryAccess;

// 聊天内存访问提供者
public interface ChatMemoryAccessProvider {
    ChatMemoryAccess chatMemoryAccess(AgenticScope agenticScope);
}
