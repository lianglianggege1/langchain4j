package dev.langchain4j.agentic.internal;

// 延迟响应
public interface DelayedResponse<T> {

    // 是否完成
    boolean isDone();

    // 获取结果
    T blockingGet();

    // 获取结果
    default Object result() {
        return isDone() ? blockingGet() : "<pending>";
    }
}
