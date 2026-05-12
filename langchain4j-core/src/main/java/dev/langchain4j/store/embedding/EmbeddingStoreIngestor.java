package dev.langchain4j.store.embedding;

import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;
import static dev.langchain4j.spi.ServiceHelper.loadFactories;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.DocumentTransformer;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.segment.TextSegmentTransformer;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.spi.data.document.splitter.DocumentSplitterFactory;
import dev.langchain4j.spi.model.embedding.EmbeddingModelFactory;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code EmbeddingStoreIngestor} represents an ingestion pipeline and is responsible
 * for ingesting {@link Document}s into an {@link EmbeddingStore}.
 * <br>
 * <br>
 * In the simplest configuration, {@code EmbeddingStoreIngestor} embeds provided documents
 * using a provided {@link EmbeddingModel} and stores them, along with their {@link Embedding}s
 * in an {@code EmbeddingStore}.
 * <br>
 * <br>
 * Optionally, the {@code EmbeddingStoreIngestor} can transform documents using a provided {@link DocumentTransformer}.
 * This can be useful if you want to clean, enrich, or format documents before embedding them.
 * <br>
 * <br>
 * Optionally, the {@code EmbeddingStoreIngestor} can split documents into {@link TextSegment}s
 * using a provided {@link DocumentSplitter}.
 * This can be useful if documents are big, and you want to split them into smaller segments to improve the quality
 * of similarity searches and reduce the size and cost of a prompt sent to the LLM.
 * <br>
 * <br>
 * Optionally, the {@code EmbeddingStoreIngestor} can transform {@code TextSegment}s using a {@link TextSegmentTransformer}.
 * This can be useful if you want to clean, enrich, or format {@code TextSegment}s before embedding them.
 * <br>
 * Including a document title or a short summary in each {@code TextSegment} is a common technique
 * to improve the quality of similarity searches.
 */
/**
 * {@code EmbeddingStoreIngestor} 表示一个数据导入管道，负责将 {@link Document}（文档）
 * 导入并存储到 {@link EmbeddingStore}（嵌入存储）中。
 * <br>
 * <br>
 * 在最简配置下，{@code EmbeddingStoreIngestor} 会使用提供的 {@link EmbeddingModel}（嵌入模型）
 * 对传入的文档进行嵌入处理，并将文档及其对应的 {@link Embedding}（嵌入向量）一同存储到
 * {@code EmbeddingStore} 中。
 * <br>
 * <br>
 * 可选功能：{@code EmbeddingStoreIngestor} 可通过提供的 {@link DocumentTransformer}（文档转换器）
 * 对文档进行转换处理。该功能适用于在嵌入前对文档进行清理、增强或格式化操作。
 * <br>
 * <br>
 * 可选功能：{@code EmbeddingStoreIngestor} 可通过提供的 {@link DocumentSplitter}（文档分割器）
 * 将文档分割为 {@link TextSegment}（文本片段）。
 * 当文档体积较大时，分割为更小的片段可提升相似度搜索质量，并减少发送给大语言模型的提示词体积与成本。
 * <br>
 * <br>
 * 可选功能：{@code EmbeddingStoreIngestor} 可通过 {@link TextSegmentTransformer}（文本片段转换器）
 * 对 {@code TextSegment} 进行转换处理。
 * 该功能适用于在嵌入前对文本片段进行清理、增强或格式化操作。
 * <br>
 * 在每个文本片段中加入文档标题或简短摘要，是提升相似度搜索质量的常用优化手段。
 */
public class EmbeddingStoreIngestor {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingStoreIngestor.class);

    private final DocumentTransformer documentTransformer;
    private final DocumentSplitter documentSplitter;
    private final TextSegmentTransformer textSegmentTransformer;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    /**
     * Creates an instance of an {@code EmbeddingStoreIngestor}.
     *
     * @param documentTransformer    The {@link DocumentTransformer} to use. Optional.
     * @param documentSplitter       The {@link DocumentSplitter} to use. Optional.
     *                               If none is specified, it tries to load one through SPI (see {@link DocumentSplitterFactory}).
     * @param textSegmentTransformer The {@link TextSegmentTransformer} to use. Optional.
     * @param embeddingModel         The {@link EmbeddingModel} to use. Mandatory.
     *                               If none is specified, it tries to load one through SPI (see {@link EmbeddingModelFactory}).
     * @param embeddingStore         The {@link EmbeddingStore} to use. Mandatory.
     */
    /**
     * 创建 {@code EmbeddingStoreIngestor}（嵌入存储导入器）的实例。
     *
     * @param documentTransformer    要使用的 {@link DocumentTransformer}（文档转换器）。可选。
     * @param documentSplitter       要使用的 {@link DocumentSplitter}（文档分割器）。可选。
     *                               如果未指定，会尝试通过 SPI 机制加载（详见 {@link DocumentSplitterFactory}）。
     * @param textSegmentTransformer 要使用的 {@link TextSegmentTransformer}（文本片段转换器）。可选。
     * @param embeddingModel         要使用的 {@link EmbeddingModel}（嵌入模型）。必填。
     *                               如果未指定，会尝试通过 SPI 机制加载（详见 {@link EmbeddingModelFactory}）。
     * @param embeddingStore         要使用的 {@link EmbeddingStore}（嵌入存储）。必填。
     */
    public EmbeddingStoreIngestor(
            DocumentTransformer documentTransformer,
            DocumentSplitter documentSplitter,
            TextSegmentTransformer textSegmentTransformer,
            EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore) {
        this.documentTransformer = documentTransformer;
        this.documentSplitter = getOrDefault(documentSplitter, EmbeddingStoreIngestor::loadDocumentSplitter);
        this.textSegmentTransformer = textSegmentTransformer;
        this.embeddingModel = ensureNotNull(
                getOrDefault(embeddingModel, EmbeddingStoreIngestor::loadEmbeddingModel), "embeddingModel");
        this.embeddingStore = ensureNotNull(embeddingStore, "embeddingStore");
    }

    private static DocumentSplitter loadDocumentSplitter() {
        Collection<DocumentSplitterFactory> factories = loadFactories(DocumentSplitterFactory.class);
        if (factories.size() > 1) {
            throw new RuntimeException("Conflict: multiple document splitters have been found in the classpath. "
                    + "Please explicitly specify the one you wish to use.");
        }

        for (DocumentSplitterFactory factory : factories) {
            DocumentSplitter documentSplitter = factory.create();
            log.debug("Loaded the following document splitter through SPI: {}", documentSplitter);
            return documentSplitter;
        }

        return null;
    }

    private static EmbeddingModel loadEmbeddingModel() {
        Collection<EmbeddingModelFactory> factories = loadFactories(EmbeddingModelFactory.class);
        if (factories.size() > 1) {
            throw new RuntimeException("Conflict: multiple embedding models have been found in the classpath. "
                    + "Please explicitly specify the one you wish to use.");
        }

        for (EmbeddingModelFactory factory : factories) {
            EmbeddingModel embeddingModel = factory.create();
            log.debug("Loaded the following embedding model through SPI: {}", embeddingModel);
            return embeddingModel;
        }

        return null;
    }

    /**
     * Ingests a specified {@link Document} into a specified {@link EmbeddingStore}.
     * <br>
     * Uses {@link DocumentSplitter} and {@link EmbeddingModel} found through SPIs
     * (see {@link DocumentSplitterFactory} and {@link EmbeddingModelFactory}).
     * <br>
     * For the "Easy RAG", import {@code langchain4j-easy-rag} module,
     * which contains a {@code DocumentSplitterFactory} and {@code EmbeddingModelFactory} implementations.
     *
     * @return result including information related to ingestion process.
     */
    /**
     * 将指定的 {@link Document}（文档）导入到指定的 {@link EmbeddingStore}（嵌入存储）中。
     * <br>
     * 通过 SPI 机制自动加载 {@link DocumentSplitter}（文档分割器）和 {@link EmbeddingModel}（嵌入模型）
     * （详见 {@link DocumentSplitterFactory} 和 {@link EmbeddingModelFactory}）。
     * <br>
     * 若使用“简易 RAG”，请引入 {@code langchain4j-easy-rag} 模块，
     * 该模块包含了 {@code DocumentSplitterFactory} 和 {@code EmbeddingModelFactory} 的实现。
     *
     * @return 包含导入过程相关信息的结果。
     */
    public static IngestionResult ingest(Document document, EmbeddingStore<TextSegment> embeddingStore) {
        return builder().embeddingStore(embeddingStore).build().ingest(document);
    }

    /**
     * Ingests specified {@link Document}s into a specified {@link EmbeddingStore}.
     * <br>
     * Uses {@link DocumentSplitter} and {@link EmbeddingModel} found through SPIs
     * (see {@link DocumentSplitterFactory} and {@link EmbeddingModelFactory}).
     * <br>
     * For the "Easy RAG", import {@code langchain4j-easy-rag} module,
     * which contains a {@code DocumentSplitterFactory} and {@code EmbeddingModelFactory} implementations.
     *
     * @return result including information related to ingestion process.
     */
    public static IngestionResult ingest(List<Document> documents, EmbeddingStore<TextSegment> embeddingStore) {
        return builder().embeddingStore(embeddingStore).build().ingest(documents);
    }

    /**
     * Ingests a specified document into an {@link EmbeddingStore} that was specified
     * during the creation of this {@code EmbeddingStoreIngestor}.
     *
     * @param document the document to ingest.
     * @return result including information related to ingestion process.
     */
    public IngestionResult ingest(Document document) {
        return ingest(singletonList(document));
    }

    /**
     * Ingests specified documents into an {@link EmbeddingStore} that was specified
     * during the creation of this {@code EmbeddingStoreIngestor}.
     *
     * @param documents the documents to ingest.
     * @return result including information related to ingestion process.
     */
    public IngestionResult ingest(Document... documents) {
        return ingest(asList(documents));
    }

    /**
     * Ingests specified documents into an {@link EmbeddingStore} that was specified
     * during the creation of this {@code EmbeddingStoreIngestor}.
     *
     * @param documents the documents to ingest.
     * @return result including information related to ingestion process.
     */
    /**
     * 将指定的文档导入到创建此 {@code EmbeddingStoreIngestor} 时指定的 {@link EmbeddingStore}（嵌入存储）中。
     *
     * @param documents 待导入的文档。
     * @return 包含导入过程相关信息的结果。
     */
    public IngestionResult ingest(List<Document> documents) {

        log.debug("Starting to ingest {} documents", documents.size());

        if (documentTransformer != null) {
            documents = documentTransformer.transformAll(documents);
            log.debug("Documents were transformed into {} documents", documents.size());
        }
        List<TextSegment> segments;
        if (documentSplitter != null) {
            segments = documentSplitter.splitAll(documents);
            log.debug("Documents were split into {} text segments", segments.size());
        } else {
            segments = documents.stream().map(Document::toTextSegment).collect(toList());
        }
        if (textSegmentTransformer != null) {
            segments = textSegmentTransformer.transformAll(segments);
            log.debug("{} documents were transformed into {} text segments", documents.size(), segments.size());
        }

        log.debug("Starting to embed {} text segments", segments.size());
        Response<List<Embedding>> embeddingsResponse = embeddingModel.embedAll(segments);
        log.debug("Finished embedding {} text segments", segments.size());

        log.debug("Starting to store {} text segments into the embedding store", segments.size());
        embeddingStore.addAll(embeddingsResponse.content(), segments);
        log.debug("Finished storing {} text segments into the embedding store", segments.size());

        return new IngestionResult(embeddingsResponse.tokenUsage());
    }

    /**
     * Creates a new EmbeddingStoreIngestor builder.
     *
     * @return the builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * EmbeddingStoreIngestor builder.
     */
    public static class Builder {

        private DocumentTransformer documentTransformer;
        private DocumentSplitter documentSplitter;
        private TextSegmentTransformer textSegmentTransformer;
        private EmbeddingModel embeddingModel;
        private EmbeddingStore<TextSegment> embeddingStore;

        /**
         * Creates a new EmbeddingStoreIngestor builder.
         */
        public Builder() {}

        /**
         * Sets the document transformer. Optional.
         *
         * @param documentTransformer the document transformer.
         * @return {@code this}
         */
        public Builder documentTransformer(DocumentTransformer documentTransformer) {
            this.documentTransformer = documentTransformer;
            return this;
        }

        /**
         * Sets the document splitter. Optional.
         * If none is specified, it tries to load one through SPI (see {@link DocumentSplitterFactory}).
         * <br>
         * {@code DocumentSplitters.recursive()} from main ({@code langchain4j}) module is a good starting point.
         *
         * @param documentSplitter the document splitter.
         * @return {@code this}
         */
        public Builder documentSplitter(DocumentSplitter documentSplitter) {
            this.documentSplitter = documentSplitter;
            return this;
        }

        /**
         * Sets the text segment transformer. Optional.
         *
         * @param textSegmentTransformer the text segment transformer.
         * @return {@code this}
         */
        public Builder textSegmentTransformer(TextSegmentTransformer textSegmentTransformer) {
            this.textSegmentTransformer = textSegmentTransformer;
            return this;
        }

        /**
         * Sets the embedding model. Mandatory.
         * If none is specified, it tries to load one through SPI (see {@link EmbeddingModelFactory}).
         *
         * @param embeddingModel the embedding model.
         * @return {@code this}
         */
        public Builder embeddingModel(EmbeddingModel embeddingModel) {
            this.embeddingModel = embeddingModel;
            return this;
        }

        /**
         * Sets the embedding store. Mandatory.
         *
         * @param embeddingStore the embedding store.
         * @return {@code this}
         */
        public Builder embeddingStore(EmbeddingStore<TextSegment> embeddingStore) {
            this.embeddingStore = embeddingStore;
            return this;
        }

        /**
         * Builds the EmbeddingStoreIngestor.
         *
         * @return the EmbeddingStoreIngestor.
         */
        public EmbeddingStoreIngestor build() {
            return new EmbeddingStoreIngestor(
                    documentTransformer, documentSplitter, textSegmentTransformer, embeddingModel, embeddingStore);
        }
    }
}
