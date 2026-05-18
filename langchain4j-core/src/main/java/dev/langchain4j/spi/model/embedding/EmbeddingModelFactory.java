package dev.langchain4j.spi.model.embedding;

import dev.langchain4j.Internal;
import dev.langchain4j.model.embedding.EmbeddingModel;

/**
 * A factory for creating {@link EmbeddingModel} instances through SPI.
 * <br>
 * For the "Easy RAG", import {@code langchain4j-easy-rag} module,
 * which contains a {@code EmbeddingModelFactory} implementation.
 */
/**
 * 通过服务提供者接口（SPI）创建 {@link EmbeddingModel} 实例的工厂类。
 * <br>
 * 若使用“简易RAG”，请导入 {@code langchain4j-easy-rag} 模块，
 * 该模块包含了 {@code EmbeddingModelFactory} 的具体实现。
 */
@Internal
public interface EmbeddingModelFactory {

    EmbeddingModel create();
}
