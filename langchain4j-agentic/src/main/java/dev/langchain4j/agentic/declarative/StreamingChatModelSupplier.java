package dev.langchain4j.agentic.declarative;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import dev.langchain4j.model.chat.StreamingChatModel;

/**
 * Marks a method as a supplier of the streaming chat model to be used by an agent.
 * 将方法标记为代理使用的流式聊天模型的供应商。
 * The method must be static and return an instance of {@link StreamingChatModel}.
 * 该方法必须是静态的，并返回{@link StreamingChatModel}的实例。
 * <p>
 * When the method has no parameters, it is invoked once at build time to provide a fixed model.
 * When the method has parameters annotated with {@link dev.langchain4j.service.V @V},
 * they are resolved from the current {@link dev.langchain4j.agentic.scope.AgenticScope AgenticScope}
 * at each invocation, enabling dynamic model selection based on runtime state.
 * <p>
 * Example:
 * <pre>
 * {@code
 *     public interface StreamingCreativeWriter {
 *
 *         @UserMessage("""
 *                 You are a creative writer.
 *                 Generate a draft of a story long no more than 3 sentence around the given topic.
 *                 Return only the story and nothing else.
 *                 The topic is {{topic}}.
 *                 """)
 *         @Agent(description = "Generate a story based on the given topic", outputKey = "story")
 *         TokenStream generateStory(@V("topic") String topic);
 *
 *         @StreamingChatModelSupplier
 *         static StreamingChatModel chatModel() {
 *             return OpenAiStreamingChatModel.builder()
 *                     .baseUrl(System.getenv("OPENAI_BASE_URL"))
 *                     .apiKey(System.getenv("OPENAI_API_KEY"))
 *                     .modelName(OpenAiChatModelName.GPT_4_O_MINI)
 *                     .temperature(0.0)
 *                     .logRequests(true)
 *                     .build();
 *         }
 *     }
 * }
 * </pre>
 */
/**
 * 将方法标记为智能体所需流式聊天模型的提供器。
 * 该方法必须为静态方法，且返回 {@link StreamingChatModel} 实例。
 * <p>
 * 若方法无参数，将在构建时调用一次以提供固定模型；
 * 若方法包含被 {@link dev.langchain4j.service.V @V} 注解的参数，
 * 则会在每次调用时从当前 {@link dev.langchain4j.agentic.scope.AgenticScope 智能体作用域} 中解析参数，
 * 支持基于运行时状态动态选择模型。
 * <p>
 * 示例：
 * <pre>
 * {@code
 *     public interface StreamingCreativeWriter {
 *
 *         @UserMessage("""
 *                 你是一名创意作家。
 *                 根据给定主题生成一段不超过3句话的故事草稿。
 *                 只返回故事内容，不要其他信息。
 *                 主题是 {{topic}}。
 *                 """)
 *         @Agent(description = "根据给定主题生成故事", outputKey = "story")
 *         TokenStream generateStory(@V("topic") String topic);
 *
 *         @StreamingChatModelSupplier
 *         static StreamingChatModel chatModel() {
 *             return OpenAiStreamingChatModel.builder()
 *                     .baseUrl(System.getenv("OPENAI_BASE_URL"))
 *                     .apiKey(System.getenv("OPENAI_API_KEY"))
 *                     .modelName(OpenAiChatModelName.GPT_4_O_MINI)
 *                     .temperature(0.0)
 *                     .logRequests(true)
 *                     .build();
 *         }
 *     }
 * }
 * </pre>
 */
@Retention(RUNTIME)
@Target({METHOD})
public @interface StreamingChatModelSupplier {
}
