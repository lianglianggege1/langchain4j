package dev.langchain4j.agentic.internal;

import java.util.Map;

// 代理参数
public record AgentInvocationArguments(Map<String, Object> namedArgs, Object[] positionalArgs) {
}
