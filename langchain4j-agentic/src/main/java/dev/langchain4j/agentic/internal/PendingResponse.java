package dev.langchain4j.agentic.internal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A {@link DeferredResponse} that blocks the calling thread until completed.
 * <p>
 * When the agentic system encounters this response via {@code readState()}, the calling thread
 * blocks on the underlying {@link java.util.concurrent.CompletableFuture} until
 * {@link #complete(Object)} is called externally. This is the default behavior for
 * human-in-the-loop agents that do not require crash-resilient suspension.
 * <p>
 * Usage with {@link dev.langchain4j.agentic.workflow.HumanInTheLoop}:
 * <pre>{@code
 * HumanInTheLoop.builder()
 *     .responseProvider(scope -> new PendingResponse<>("user-approval"))
 *     .build();
 *
 * // From another thread:
 * scope.completePendingResponse("user-approval", "approved");
 * }</pre>
 *
 * @param <T> the type of the response value
 * @see SuspendedResponse for crash-resilient suspension (exception-based)
 */
/**
 * 可由外部主动完成的延迟响应对象，无需开启后台线程。
 * <p>
 * 不同于{@link AsyncResponse}会立即在线程池中执行供给函数，
 * {@code PendingResponse}（经由基类{@link DeferredResponse}）会创建一个初始未完成的异步任务，
 * 必须通过{@link #complete(Object)}方法显式完成。
 * 该类适用于响应结果来自外部来源的场景（例如通过REST API人工介入、消息队列、外部事件等）。
 * <p>
 * 当agentic系统遇到此响应时，调用线程会阻塞等待，直到外部调用{@link #complete(Object)}完成响应。
 * 如需崩溃恢复式挂起场景，请参考{@link SuspendedResponse}（基于异常语义）。
 */
public class PendingResponse<T> extends DeferredResponse<T> {

    @JsonCreator
    public PendingResponse(@JsonProperty("responseId") String responseId) {
        super(responseId);
    }
}
