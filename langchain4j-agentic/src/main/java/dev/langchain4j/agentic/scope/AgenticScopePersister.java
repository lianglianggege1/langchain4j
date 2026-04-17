package dev.langchain4j.agentic.scope;

import java.util.ServiceLoader;

// agent作用域持久化
public enum AgenticScopePersister {

    // 实例
    INSTANCE;

    // 存储
    static AgenticScopeStore store;

    AgenticScopePersister() {
        setStore(loadStore());
    }

    // 加载存储
    private static AgenticScopeStore loadStore() {
        ServiceLoader<AgenticScopeStore> loader =
                ServiceLoader.load(AgenticScopeStore.class);

        for (AgenticScopeStore provider : loader) {
            return provider; // Return the first provider found
        }
        return null; // No provider found
    }

    /**
     * Explicitly set a persistence provider.
     */
    public static void setStore(AgenticScopeStore store) {
        AgenticScopePersister.store = store;
    }
}
