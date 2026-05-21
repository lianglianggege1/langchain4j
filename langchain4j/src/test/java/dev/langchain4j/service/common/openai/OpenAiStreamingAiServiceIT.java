package dev.langchain4j.service.common.openai;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponseMetadata;
import dev.langchain4j.model.openai.OpenAiChatResponseMetadata;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiTokenUsage;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.service.common.AbstractStreamingAiServiceIT;
import java.util.List;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

// TODO move to langchain4j-open-ai module once dependency cycle is resolved
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class OpenAiStreamingAiServiceIT extends AbstractStreamingAiServiceIT {

    private static OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder defaultStreamingModelBuilder() {
        return OpenAiStreamingChatModel.builder()
                .baseUrl("https://api.minimaxi.com/v1")
                .apiKey("sk-cp-k94NcRwMEUPF_ls-fqeyN9Gk9msntO6yv1WZxwhV3joGHW8rXfeP5Xqe27hsklfARyU0YnqQW1acyFsR6nWKE85mp8HJLFoz0YiZklBukg4_LXilJ5JXbyM")
                .organizationId("MiniMax")
                .modelName("MiniMax-M2.7");
    }

    @Override
    protected List<StreamingChatModel> models() {
        return List.of(
                defaultStreamingModelBuilder().build()
                // TODO more configs?
                );
    }

    @Override
    protected Class<? extends ChatResponseMetadata> chatResponseMetadataType(StreamingChatModel streamingChatModel) {
        return OpenAiChatResponseMetadata.class;
    }

    @Override
    protected Class<? extends TokenUsage> tokenUsageType(StreamingChatModel streamingChatModel) {
        return OpenAiTokenUsage.class;
    }
}
