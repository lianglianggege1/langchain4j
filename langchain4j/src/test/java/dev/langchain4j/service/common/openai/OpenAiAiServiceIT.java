package dev.langchain4j.service.common.openai;

import static dev.langchain4j.model.chat.Capability.RESPONSE_FORMAT_JSON_SCHEMA;
import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenUsage;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.service.common.AbstractAiServiceIT;
import java.util.List;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

// TODO move to langchain4j-open-ai module once dependency cycle is resolved
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class OpenAiAiServiceIT extends AbstractAiServiceIT {

    private static OpenAiChatModel.OpenAiChatModelBuilder defaultModelBuilder() {
        return OpenAiChatModel.builder()
                .baseUrl("https://api.minimaxi.com/v1")
                .apiKey("sk-cp-k94NcRwMEUPF_ls-fqeyN9Gk9msntO6yv1WZxwhV3joGHW8rXfeP5Xqe27hsklfARyU0YnqQW1acyFsR6nWKE85mp8HJLFoz0YiZklBukg4_LXilJ5JXbyM")
                .organizationId("MiniMax")
                .modelName("MiniMax-M2.7")
                .logRequests(true)
                .logResponses(true);
    }

    @Override
    protected List<ChatModel> models() {
        return List.of(
                defaultModelBuilder().build()
                // TODO more configs?
                );
    }

    @Override
    protected List<ChatModel> modelsSupportingToolsAndJsonResponseFormatWithSchema() {
        return List.of(
                defaultModelBuilder()
                        .supportedCapabilities(RESPONSE_FORMAT_JSON_SCHEMA)
                        .strictJsonSchema(true)
                        .build(),
                defaultModelBuilder()
                        .responseFormat("json_schema") // testing backward compatibility
                        .strictJsonSchema(true)
                        .build(),
                defaultModelBuilder()
                        .supportedCapabilities(RESPONSE_FORMAT_JSON_SCHEMA)
                        .strictJsonSchema(false)
                        .build(),
                defaultModelBuilder()
                        .responseFormat("json_schema") // testing backward compatibility
                        .strictJsonSchema(false)
                        .build()
                // TODO more configs?
                );
    }

    @Override
    protected Class<? extends TokenUsage> tokenUsageType(ChatModel chatModel) {
        return OpenAiTokenUsage.class;
    }
}
