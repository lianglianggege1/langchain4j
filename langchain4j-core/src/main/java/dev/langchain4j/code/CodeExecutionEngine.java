package dev.langchain4j.code;

/**
 * Interface for executing code.
 * 执行代码的接口
 */
public interface CodeExecutionEngine {

    /**
     * Execute the given code.
     * 执行给定代码。
     *
     * @param code The code to execute.
     * @return The result of the execution.
     */
    String execute(String code);
}
