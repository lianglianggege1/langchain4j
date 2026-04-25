package dev.langchain4j.invocation;

import java.util.Map;
import dev.langchain4j.Internal;

/**
 * A marker interface for components that are managed by LangChain4j framework.
 * 由LangChain4j框架管理的组件的标记接口。
 * <p>
 * Implementing this interface indicates that the component is internally managed by LangChain4j,
 * and doesn't require to be instatiated or passed around by the user or LLM.
 * 实现此接口表示该组件由LangChain4j内部管理，不需要由用户或LLM实例化或传递。
 *
 *
 * @since 1.8.0
 */
@Internal
public interface LangChain4jManaged {

    // 当前线程的组件
    ThreadLocal<Map<Class<? extends LangChain4jManaged>, LangChain4jManaged>> CURRENT = new ThreadLocal<>();

    static void setCurrent(Map<Class<? extends LangChain4jManaged>, LangChain4jManaged> current) {
        CURRENT.set(current);
    }

    static Map<Class<? extends LangChain4jManaged>, LangChain4jManaged> current() {
        return CURRENT.get();
    }

    static <T extends LangChain4jManaged> T current(Class<T> clazz) {
        var current = CURRENT.get();
        return current != null ? clazz.cast(current.get(clazz)) : null;
    }

    static void removeCurrent() {
        CURRENT.remove();
    }
}
