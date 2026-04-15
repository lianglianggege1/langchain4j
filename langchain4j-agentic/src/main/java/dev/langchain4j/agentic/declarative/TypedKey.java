package dev.langchain4j.agentic.declarative;

/**
 * A class implementing this interface represents the input or output key of an agent in a strongly typed way.
 * 实现此接口的类以强类型方式表示代理的输入或输出键。
 * This corresponds to a state of the agentic system that can be used to store and retrieve information during the system's operation.
 * 这对应于代理系统的状态，可用于在系统运行期间存储和检索信息。
 * @param <T> The type of the state value.
 */
public interface TypedKey<T> {

    /**
     * Returns the default value for this state.
     * 返回此状态的默认值。
     * This method can be overridden to provide a specific default value.
     * 可以重写此方法以提供特定的默认值。
     *
     * @return the default value of type T, or null if not overridden.
     */
    default T defaultValue() {
        return null;
    }

    /**
     * Returns the name of this state used inside the agentic system and for prompt templating.
     * 返回在代理系统内用于提示模板的此状态的名称。
     * By default, it is the simple name of the implementing class.
     * 默认情况下，它是实现类的简单名称。
     *
     * @return the name of the state.
     */
    default String name() {
        return this.getClass().getSimpleName();
    }
}
