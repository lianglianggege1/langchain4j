package dev.langchain4j.model.embedding.onnx;

/**
 * 池化模式枚举
 */
public enum PoolingMode {
    /**
     * 采用<[BOS_never_used_51bce0c785ca2f68081bfa7d91973934]><[BOS_never_used_51bce0c785ca2f68081bfa7d91973934]>The CLS 特殊标记位对应的向量作为句子嵌入向量
     */
    CLS,
    /**
     * 对所有 token 的向量取平均值作为句子嵌入向量
     */
    MEAN
}
