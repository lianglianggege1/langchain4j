package dev.langchain4j.agentic.internal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A {@link DelayedResponse} that can be completed externally, without spawning a background thread.
 * <p>
 * Unlike {@link AsyncResponse}, which immediately starts executing a supplier on a thread pool,
 * {@code PendingResponse} creates an initially incomplete future that must be explicitly completed
 * via {@link #complete(Object)}. This makes it suitable for scenarios where the response comes from
 * an external source (e.g., a human via a REST API, a message queue, or an external event) and the
 * workflow must survive process restarts.
 * <p>
 * After serialization/deserialization, a new incomplete {@link CompletableFuture} is created,
 * allowing an external system to reconnect and complete the response.
 * <p>
 * Usage with {@link dev.langchain4j.agentic.workflow.HumanInTheLoop}:
 * <pre>{@code
 * HumanInTheLoop.builder()
 *     .responseProvider(scope -> new PendingResponse<>("user-approval"))
 *     .build();
 *
 * // Later, from an external system (e.g., REST endpoint):
 * scope.completePendingResponse("user-approval", "approved");
 * }</pre>
 *
 * @param <T> the type of the response value
 */
/**
 * 可由外部主动完成的延迟响应对象{@link DelayedResponse}，无需开启后台线程。
 * <p>
 * 不同于{@link AsyncResponse}会立即在线程池中执行供给函数，
 * {@code PendingResponse}会创建一个初始未完成的异步任务，必须通过{@link #complete(Object)}方法显式完成。
 * 该类适用于响应结果来自外部来源的场景（例如通过REST API人工介入、消息队列、外部事件等），
 * 且对应的业务流程需要在服务重启后继续执行。
 * <p>
 * 经过序列化与反序列化后，会生成一个全新的未完成{@link CompletableFuture}，
 * 支持外部系统重连并完成响应结果。
 * <p>
 * 结合{@link dev.langchain4j.agentic.workflow.HumanInTheLoop}（人机协同流程）的使用示例：
 * <pre>{@code
 * HumanInTheLoop.builder()
 *     .responseProvider(scope -> new PendingResponse<>("user-approval"))
 *     .build();
 *
 * // 后续由外部系统（如REST接口）执行：
 * scope.completePendingResponse("user-approval", "approved");
 * }</pre>
 *
 * @param <T> 响应值的类型
 */
public class PendingResponse<T> implements DelayedResponse<T> {

    private final String responseId;

    @JsonIgnore
    private transient CompletableFuture<T> futureResponse;

    /**
     * Creates a new pending response with the given unique identifier.
     *
     * @param responseId a unique identifier for this pending response, used to locate and
     *                   complete it from external systems
     */
    /**
     * 使用指定的唯一标识创建一个新的待处理响应。
     *
     * @param responseId 该待处理响应的唯一标识，用于外部系统定位并完成该响应
     */
    @JsonCreator
    public PendingResponse(@JsonProperty("responseId") String responseId) {
        this.responseId = responseId;
        this.futureResponse = new CompletableFuture<>();
    }

    /**
     * Returns the unique identifier for this pending response.
     *
     * @return the response identifier
     */
    public String responseId() {
        return responseId;
    }

    @Override
    @JsonIgnore
    public boolean isDone() {
        return futureResponse.isDone();
    }

    @Override
    @JsonIgnore
    public T blockingGet() {
        return futureResponse.join();
    }

    /**
     * Waits for the response with a timeout.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return the response value
     * @throws TimeoutException if the wait timed out
     */
    /**
     * 等待响应结果，支持超时设置。
     *
     * @param timeout 最长等待时长
     * @param unit 时长单位
     * @return 响应结果
     * @throws TimeoutException 等待超时则抛出此异常
     */
    public T blockingGet(long timeout, TimeUnit unit) throws TimeoutException {
        try {
            return futureResponse.get(timeout, unit);
        } catch (TimeoutException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Completes this pending response with the given value.
     * Any threads blocked on {@link #blockingGet()} will be released.
     *
     * @param value the response value
     * @return {@code true} if this invocation caused the response to transition to a completed state,
     *         {@code false} if it was already completed
     */
    /**
     * 使用指定的值完成该待处理响应。
     * 所有阻塞在 {@link #blockingGet()} 方法上的线程都会被释放。
     *
     * @param value 响应结果值
     * @return 如果本次调用使响应从未完成状态切换为已完成状态，则返回 {@code true}；
     *         如果响应此前已完成，则返回 {@code false}
     */
    public boolean complete(T value) {
        return futureResponse.complete(value);
    }

    /**
     * Completes this pending response exceptionally.
     *
     * @param exception the exception
     * @return {@code true} if this invocation caused the response to transition to a completed state
     */
    /**
     * 以异常形式完成该待处理响应。
     *
     * @param exception 异常对象
     * @return 如果本次调用使响应从未完成状态切换为已完成状态，则返回 {@code true}
     */
    public boolean completeExceptionally(Throwable exception) {
        return futureResponse.completeExceptionally(exception);
    }

    @Override
    public String toString() {
        return isDone() ? String.valueOf(result()) : "<pending:" + responseId + ">";
    }
}
