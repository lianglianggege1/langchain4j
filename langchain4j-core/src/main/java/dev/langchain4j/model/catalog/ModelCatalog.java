package dev.langchain4j.model.catalog;

import dev.langchain4j.Experimental;
import dev.langchain4j.model.ModelProvider;

import java.util.List;

/**
 * Represents a service that can discover available models from an LLM provider.
 * This allows customers to list available models and their capabilities without
 * needing to go to individual AI provider websites.
 * 表示一项可从LLM服务商发现可用模型的服务
 *
 * <p>Similar to {@link dev.langchain4j.model.chat.ChatModel} and
 * {@link dev.langchain4j.model.chat.StreamingChatModel}, each provider should
 * implement this interface to enable model listing.
 * <p>与 {@link dev.langchain4j.model.chat.ChatModel} 和
 * {@link dev.langchain4j.model.chat.StreamingChatModel} 类似，
 * 各服务商均应实现此接口，以支持模型列表查询功能。
 *
 * <p>Example usage:
 * <pre>{@code
 * OpenAiModelCatalog catalog = OpenAiModelCatalog.builder()
 *     .apiKey(apiKey)
 *     .build();
 *
 * List<ModelDescription> models = catalog.listModels();
 *
 * }</pre>
 *
 * @see ModelDescription
 * @since 1.10.0
 */
@Experimental
public interface ModelCatalog {

    /**
     * Retrieves a list of available models from the provider.
     * 获取提供者的可用模型列表
     *
     * @return A list of model descriptions
     * @throws RuntimeException if the listing operation fails
     */
    List<ModelDescription> listModels();

    /**
     * Returns the provider for this catalog service.
     * 返回此服务目录服务的提供者
     *
     * @return The model provider
     */
    ModelProvider provider();
}
