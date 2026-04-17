package dev.langchain4j.agentic.scope;

/**
 * Holds the result of an agent invocation along with its associated {@link AgenticScope}.
 * This is useful for returning results from agents while also providing access to the cognitive
 * scope through which that result has been generated.
 * 保存代理调用的结果及其关联的AgentScope。
 * 这对于从代理返回结果非常有用，同时也提供了对生成结果的认知范围的访问。
 *
 * @param <T> The type of the result.
 */
public record ResultWithAgenticScope<T>(AgenticScope agenticScope, T result) { }
