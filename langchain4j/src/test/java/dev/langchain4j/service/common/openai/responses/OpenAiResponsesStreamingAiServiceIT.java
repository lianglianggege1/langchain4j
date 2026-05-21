package dev.langchain4j.service.common.openai.responses;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponseMetadata;
import dev.langchain4j.model.openai.OpenAiResponsesChatResponseMetadata;
import dev.langchain4j.model.openai.OpenAiResponsesStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiTokenUsage;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.service.common.AbstractStreamingAiServiceIT;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;

// TODO move to langchain4j-open-ai module once dependency cycle is resolved
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class OpenAiResponsesStreamingAiServiceIT extends AbstractStreamingAiServiceIT {

    @Override
    protected List<StreamingChatModel> models() {
        return List.of(
                OpenAiResponsesStreamingChatModel.builder()
                        .baseUrl("https://api.minimaxi.com/v1")
                        .apiKey("sk-cp-k94NcRwMEUPF_ls-fqeyN9Gk9msntO6yv1WZxwhV3joGHW8rXfeP5Xqe27hsklfARyU0YnqQW1acyFsR6nWKE85mp8HJLFoz0YiZklBukg4_LXilJ5JXbyM")
                        .organizationId("MiniMax")
                        .modelName("MiniMax-M2.7")
                        .temperature(0.0)
                        .logRequests(true)
                        .logResponses(true)
                        .build()
        );
    }

    @Override
    protected Class<? extends ChatResponseMetadata> chatResponseMetadataType(StreamingChatModel model) {
        return OpenAiResponsesChatResponseMetadata.class;
    }

    @Override
    protected Class<? extends TokenUsage> tokenUsageType(StreamingChatModel streamingChatModel) {
        return OpenAiTokenUsage.class;
    }
}
