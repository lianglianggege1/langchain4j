package dev.langchain4j.rag;

import dev.langchain4j.Experimental;
import dev.langchain4j.exception.AsyncNotSupportedException;
import dev.langchain4j.internal.AsyncNotSupported;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.rag.content.Content;

import java.util.concurrent.CompletableFuture;

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

    /**
     * Non-blocking counterpart of {@link #augment(AugmentationRequest)}, invoked by the asynchronous
     * ({@link java.util.concurrent.CompletableFuture}/{@link java.util.concurrent.CompletionStage}) and reactive
     * ({@link java.util.concurrent.Flow.Publisher}) AI Service modes so the RAG flow never blocks the calling thread.
     * <p>
     * The default returns a failed future carrying {@link AsyncNotSupportedException}: an augmentor that is not genuinely asynchronous does
     * not pretend to be. {@link DefaultRetrievalAugmentor} overrides this to compose its steps into a real future. A
     * custom augmentor that has not opted in is still usable from the non-blocking modes: the AI Service offloads its
     * blocking {@link #augment(AugmentationRequest)} to a shared virtual-thread executor, rather than the augmentor
     * being silently offloaded on every call.
     * <p>
     * An implementation that honors cancellation should abort in-flight work when the returned future is cancelled
     * (best-effort).
     *
     * @param augmentationRequest The {@code AugmentationRequest} containing the {@code ChatMessage} to augment.
     * @return A {@link CompletableFuture} of the {@link AugmentationResult} containing the augmented {@code ChatMessage}.
     * @since 1.20.0
     */
    @Experimental
    default CompletableFuture<AugmentationResult> augmentAsync(AugmentationRequest augmentationRequest) {
        return AsyncNotSupported.failedFuture(getClass(), "augmentAsync");
    }
}
