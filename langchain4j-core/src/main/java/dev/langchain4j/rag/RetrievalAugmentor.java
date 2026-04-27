package dev.langchain4j.rag;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.rag.content.Content;

/**
 * Augments the provided {@link ChatMessage} with retrieved {@link Content}s.
 * 用检索到的{@link Content}s增强提供的{@link ChatMessage}。
 * <br>
 * This serves as an entry point into the RAG flow in LangChain4j.
 * 这个服务作为LangChain4j中的RAG流程的入口点。
 * <br>
 * You are free to use the default implementation ({@link DefaultRetrievalAugmentor}) or to implement a custom one.
 * 您可以自由使用默认实现（{@link DefaultRetrievalAugmentor}）或实现自定义实现。
 *
 * @see DefaultRetrievalAugmentor
 */
public interface RetrievalAugmentor {

    /**
     * Augments the {@link ChatMessage} provided in the {@link AugmentationRequest} with retrieved {@link Content}s.
     * 使用检索到的{@link ChatMessage}对{@link AugmentationRequest}中提供的{@link ChatMessage}进行增强。
     *
     * @param augmentationRequest The {@code AugmentationRequest} containing the {@code ChatMessage} to augment.
     * @return The {@link AugmentationResult} containing the augmented {@code ChatMessage}.
     */
    AugmentationResult augment(AugmentationRequest augmentationRequest);
}
