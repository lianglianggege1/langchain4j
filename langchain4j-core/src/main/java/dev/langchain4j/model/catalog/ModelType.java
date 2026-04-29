package dev.langchain4j.model.catalog;

import dev.langchain4j.Experimental;
import dev.langchain4j.model.audio.AudioTranscriptionModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.model.scoring.ScoringModel;

/**
 * Represents the type/category of a model.
 * 表示模型的**类型/类别**。
 *
 * @since 1.10.0
 */
@Experimental
public enum ModelType {

    /**
     * Chat/conversational models (e.g., GPT-5, Claude, etc.).
     * Can be used with {@link ChatModel} or {@link StreamingChatModel}.
     * 对话/会话类模型（如 GPT-5、Claude 等），
     * 可与 {@link ChatModel} 或 {@link StreamingChatModel} 配合使用。
     */
    CHAT,

    /**
     * Text embedding models for vector representations.
     * Can be used with {@link EmbeddingModel}.
     * 用于生成向量表示的文本嵌入模型。可与 {@link EmbeddingModel} 配合使用。
     */
    EMBEDDING,

    /**
     * Image generation models (e.g., DALL-E, Stable Diffusion).
     * Can be used with {@link ImageModel}.
     * 图像生成模型（例如 DALL-E、Stable Diffusion），可与 {@link ImageModel} 配合使用。
     */
    IMAGE_GENERATION,

    /**
     * Audio transcription models (speech-to-text).
     * Can be used with {@link AudioTranscriptionModel}.
     * 音频转写模型（语音转文本）。
     * 可与 {@link AudioTranscriptionModel} 配合使用。
     */
    AUDIO_TRANSCRIPTION,

    /**
     * Audio generation models (text-to-speech).
     * 音频生成模型（文本转语音）。
     */
    AUDIO_GENERATION,

    /**
     * Content moderation models.
     * Can be used with {@link ModerationModel}.
     * 内容审核模型，可与 {@link ModerationModel} 配合使用。
     */
    MODERATION,

    /**
     * Document scoring or re-ranking models.
     * Can be used with {@link ScoringModel}.
     * 文档打分/重排序模型。
     * 可与 {@link ScoringModel} 配合使用。
     */
    SCORING,

    /**
     * Other or unclassified model types.
     * 其他或未分类的模型类型。
     */
    OTHER
}
