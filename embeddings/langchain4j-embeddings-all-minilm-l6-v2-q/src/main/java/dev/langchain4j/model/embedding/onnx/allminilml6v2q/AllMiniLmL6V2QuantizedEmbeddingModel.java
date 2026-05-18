package dev.langchain4j.model.embedding.onnx.allminilml6v2q;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

import dev.langchain4j.model.embedding.onnx.AbstractInProcessEmbeddingModel;
import dev.langchain4j.model.embedding.onnx.OnnxBertBiEncoder;
import dev.langchain4j.model.embedding.onnx.PoolingMode;
import java.util.concurrent.Executor;

/**
 * Quantized SentenceTransformers all-MiniLM-L6-v2 embedding model that runs within your Java application's process.
 * <p>
 * Maximum length of text (in tokens) that can be embedded at once: unlimited.
 * However, while you can embed very long texts, the quality of the embedding degrades as the text lengthens.
 * It is recommended to embed segments of no more than 256 tokens.
 * <p>
 * Embedding dimensions: 384
 * <p>
 * Uses an {@link Executor} to parallelize the embedding process.
 * By default, uses a cached thread pool with the number of threads equal to the number of available processors.
 * Threads are cached for 1 second.
 * <p>
 * More details
 * <a href="https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2">here</a> and
 * <a href="https://www.sbert.net/docs/pretrained_models.html">here</a>
 */
/**
 * 可在 Java 应用程序进程内运行的量化版 SentenceTransformers all-MiniLM-L6-v2 嵌入模型。
 * <p>
 * 单次可嵌入文本的最大长度（以 token 为单位）：无限制。
 * 但是，尽管可以嵌入极长文本，但嵌入质量会随着文本长度的增加而下降。
 * 建议分段嵌入，每段不超过 256 个 token。
 * <p>
 * 嵌入向量维度：384
 * <p>
 * 使用 {@link Executor} 对嵌入过程进行并行化处理。
 * 默认使用缓存线程池，线程数量等于可用处理器核心数。
 * 线程缓存时长为 1 秒。
 * <p>
 * 更多详情请查看
 * <a href="https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2">此处</a> 和
 * <a href="https://www.sbert.net/docs/pretrained_models.html">此处</a>
 */
public class AllMiniLmL6V2QuantizedEmbeddingModel extends AbstractInProcessEmbeddingModel {

    private static final OnnxBertBiEncoder MODEL =
            loadFromJar("all-minilm-l6-v2-q.onnx", "all-minilm-l6-v2-q-tokenizer.json", PoolingMode.MEAN);

    /**
     * Creates an instance of an {@code AllMiniLmL6V2QuantizedEmbeddingModel}.
     * Uses a cached thread pool with the number of threads equal to the number of available processors.
     * Threads are cached for 1 second.
     */
    /**
     * 创建 {@code AllMiniLmL6V2QuantizedEmbeddingModel} 实例。
     * 使用缓存线程池，线程数量等于可用处理器核心数。
     * 线程缓存时长为 1 秒。
     */
    public AllMiniLmL6V2QuantizedEmbeddingModel() {
        super(null);
    }

    /**
     * Creates an instance of an {@code AllMiniLmL6V2QuantizedEmbeddingModel}.
     *
     * @param executor The executor to use to parallelize the embedding process.
     */
    /**
     * 创建 {@code AllMiniLmL6V2QuantizedEmbeddingModel} 实例。
     *
     * @param executor 用于对嵌入过程进行并行化处理的执行器。
     */
    public AllMiniLmL6V2QuantizedEmbeddingModel(Executor executor) {
        super(ensureNotNull(executor, "executor"));
    }

    @Override
    protected OnnxBertBiEncoder model() {
        return MODEL;
    }

    @Override
    protected Integer knownDimension() {
        return 384;
    }
}
