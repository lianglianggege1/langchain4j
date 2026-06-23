package dev.langchain4j.agentic.declarative;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * SPI for resolving parameters of supplier methods of {@link ChatModelSupplier} and {@link StreamingChatModelSupplier}
 * from external sources such as a dependency injection container.
 */
/**
 * 服务提供者接口，用于从依赖注入容器等外部来源解析 {@link ChatModelSupplier} 与 {@link StreamingChatModelSupplier} 对应提供者方法的参数。
 */
public interface ChatSupplierParameterResolver {

    interface Context {

        Class<?> declaringAgentClass();

        Method supplierMethod();

        Parameter parameter();
    }

    /**
     * Called once per parameter at agent creation time to pre-determine which parameters this resolver handles.
     */
    /**
     * 在智能体创建时，每个参数都会调用一次，用以预先确定该解析器可处理的参数。
     */
    boolean supports(Context context);

    /**
     * Called at invocation time to obtain the actual value
     */
    /**
     * 方法调用时触发，用于获取实际参数值
     */
    Object resolve(Context context);
}
