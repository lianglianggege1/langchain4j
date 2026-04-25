package dev.langchain4j.chain;

import dev.langchain4j.service.AiServices;

/**
 * Represents a chain step that takes an input and produces an output.
 * 表示一个链式步骤，该步骤接受一个输入并产生一个输出。
 * <br>
 * Chains are not going to be developed further, it is recommended to use {@link AiServices} instead.
 * Chains 将不再进行开发，建议改用 {@link AiServices}。
 *
 * @param <Input>  the input type
 * @param <Output> the output type
 */
@FunctionalInterface
public interface Chain<Input, Output> {

    /**
     * Execute the chain step.
     * 执行链式步骤。
     *
     * @param input the input
     * @return the output
     */
    Output execute(Input input);
}
