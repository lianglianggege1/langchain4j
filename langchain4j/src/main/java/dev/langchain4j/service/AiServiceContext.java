package dev.langchain4j.service;

import static dev.langchain4j.spi.ServiceHelper.loadFactory;

import dev.langchain4j.Internal;
import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.observability.api.AiServiceListenerRegistrar;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.service.guardrail.GuardrailService;
import dev.langchain4j.service.memory.ChatMemoryService;
import dev.langchain4j.service.tool.ToolService;
import dev.langchain4j.spi.services.AiServiceContextFactory;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;

@Internal
public class AiServiceContext {

    // 默认的用户消息提供者
    private static final Function<Object, Optional<String>> DEFAULT_USER_MESSAGE_PROVIDER = x -> Optional.empty();
    // 默认的系统消息提供者
    private static final Function<Object, Optional<String>> DEFAULT_SYSTEM_MESSAGE_PROVIDER = x -> Optional.empty();

    // AI服务类
    public final Class<?> aiServiceClass;
    // AI服务监听器
    public final AiServiceListenerRegistrar eventListenerRegistrar = AiServiceListenerRegistrar.newInstance();

    // 返回值类型
    public Class<?> returnType;

    // 聊天模型
    public ChatModel chatModel;
    // 流式聊天模型
    public StreamingChatModel streamingChatModel;

    // 聊天内存服务
    public ChatMemoryService chatMemoryService;

    // 工具服务
    public ToolService toolService = new ToolService();

    // 验证服务
    public final GuardrailService.Builder guardrailServiceBuilder;
    // 验证服务
    private final AtomicReference<GuardrailService> guardrailService = new AtomicReference<>();

    // 模型审核服务
    public ModerationModel moderationModel;

    // 检索增强服务
    public RetrievalAugmentor retrievalAugmentor;

    // 存储检索到的内容到聊天内存里面
    public boolean storeRetrievedContentInChatMemory = true;

    // 获取用户消息
    public Function<Object, Optional<String>> userMessageProvider = DEFAULT_USER_MESSAGE_PROVIDER;
    // 获取系统消息
    public Function<Object, Optional<String>> systemMessageProvider = DEFAULT_SYSTEM_MESSAGE_PROVIDER;

    // 系统信息转换
    public BiFunction<String, InvocationContext, String> systemMessageTransformer = null;

    // 聊天请求转换
    public BiFunction<ChatRequest, Object, ChatRequest> chatRequestTransformer = (req, memId) -> req;

    protected AiServiceContext(Class<?> aiServiceClass) {
        this.aiServiceClass = aiServiceClass;
        this.guardrailServiceBuilder = GuardrailService.builder(aiServiceClass);
    }

    private static class FactoryHolder {
        private static final AiServiceContextFactory contextFactory = loadFactory(AiServiceContextFactory.class);
    }

    public static AiServiceContext create(Class<?> aiServiceClass) {
        return FactoryHolder.contextFactory != null
                ? FactoryHolder.contextFactory.create(aiServiceClass)
                : new AiServiceContext(aiServiceClass);
    }

    // 是否有聊天记忆
    public boolean hasChatMemory() {
        return chatMemoryService != null;
    }

    // 初始化聊天记忆
    public void initChatMemories(ChatMemory chatMemory) {
        chatMemoryService = new ChatMemoryService(chatMemory);
    }

    public void initChatMemories(ChatMemoryProvider chatMemoryProvider) {
        chatMemoryService = new ChatMemoryService(chatMemoryProvider);
    }

    // 是否有模型审核服务
    public boolean hasModerationModel() {
        return moderationModel != null;
    }

    // 获取验证服务
    public GuardrailService guardrailService() {
        return this.guardrailService.updateAndGet(
                service -> (service != null) ? service : guardrailServiceBuilder.build());
    }
}
