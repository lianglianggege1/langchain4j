package dev.langchain4j.agentic.planner;

import java.lang.reflect.Type;
import java.util.List;

// agent 实例
public interface AgentInstance {

    // 类型
    Class<?> type();

    // 规划器类型
    Class<? extends Planner> plannerType();

    //agent名称
    String name();

    //agent id
    String agentId();

    //描述
    String description();

    //输出类型
    Type outputType();

    //输出key
    String outputKey();

    //是否异步
    boolean async();

    default boolean optional() {
        return false;
    }

    //参数
    List<AgentArgument> arguments();

    //父级
    AgentInstance parent();

    //子级
    List<AgentInstance> subagents();

    //叶子
    default boolean leaf() {
        return subagents().isEmpty();
    }

    //agent系统拓扑结构
    AgenticSystemTopology topology();

    default <T extends AgentInstance> T as(Class<T> agentInstanceClass) {
        throw new ClassCastException("Cannot cast to " + agentInstanceClass.getName() + ": incompatible type");
    }
}
