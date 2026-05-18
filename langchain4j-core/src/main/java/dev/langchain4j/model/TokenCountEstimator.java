package dev.langchain4j.model;

import dev.langchain4j.data.message.ChatMessage;

/**
 * Represents an interface for estimating the count of tokens in various text types such as a text, prompt, text segment, etc.
 * This can be useful when it's necessary to know in advance the cost of processing a specified text by the LLM.
 */
/**
 * 用于估算不同文本类型（普通文本、提示词、文本片段等）的令牌数量的接口。
 * 当需要提前知晓大语言模型（LLM）处理指定文本的成本时，该接口会非常有用。
 */
public interface TokenCountEstimator {

    /**
     * Estimates the count of tokens in the given text.
     *
     * @param text the text.
     * @return the estimated count of tokens.
     */
    /**
     * 估算给定文本中的令牌（token）数量。
     *
     * @param text 文本
     * @return 估算出的令牌数量
     */
    int estimateTokenCountInText(String text);

    /**
     * Estimates the count of tokens in the given message.
     *
     * @param message the message.
     * @return the estimated count of tokens.
     */
    /**
     * 估算给定消息中的令牌（token）数量。
     *
     * @param message 消息
     * @return 估算出的令牌数量
     */
    int estimateTokenCountInMessage(ChatMessage message);

    /**
     * Estimates the count of tokens in the given messages.
     *
     * @param messages the messages.
     * @return the estimated count of tokens.
     */
    /**
     * 估算给定多条消息中的令牌（token）总数。
     *
     * @param messages 消息列表
     * @return 估算出的令牌总数
     */
    int estimateTokenCountInMessages(Iterable<ChatMessage> messages);
}
