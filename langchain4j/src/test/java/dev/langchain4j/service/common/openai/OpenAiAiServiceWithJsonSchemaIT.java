package dev.langchain4j.service.common.openai;

import static dev.langchain4j.model.chat.Capability.RESPONSE_FORMAT_JSON_SCHEMA;
import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.common.AbstractAiServiceWithJsonSchemaIT;
import java.util.List;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

// TODO move to langchain4j-open-ai module once dependency cycle is resolved
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class OpenAiAiServiceWithJsonSchemaIT extends AbstractAiServiceWithJsonSchemaIT {

    OpenAiChatModel modelWithStrictJsonSchema = OpenAiChatModel.builder()
            .baseUrl("https://api.minimaxi.com/v1")
            .apiKey("sk-cp-k94NcRwMEUPF_ls-fqeyN9Gk9msntO6yv1WZxwhV3joGHW8rXfeP5Xqe27hsklfARyU0YnqQW1acyFsR6nWKE85mp8HJLFoz0YiZklBukg4_LXilJ5JXbyM")
            .organizationId("MiniMax")
            .modelName("MiniMax-M2.7")
            .supportedCapabilities(RESPONSE_FORMAT_JSON_SCHEMA)
            .strictJsonSchema(true)
            .temperature(0.0)
            .logRequests(true)
            .logResponses(true)
            .build();

    OpenAiChatModel modelWithStrictJsonSchemaLegacy = OpenAiChatModel.builder()
            .baseUrl("https://api.minimaxi.com/v1")
            .apiKey("sk-cp-k94NcRwMEUPF_ls-fqeyN9Gk9msntO6yv1WZxwhV3joGHW8rXfeP5Xqe27hsklfARyU0YnqQW1acyFsR6nWKE85mp8HJLFoz0YiZklBukg4_LXilJ5JXbyM")
            .organizationId("MiniMax")
            .modelName("MiniMax-M2.7")
            .responseFormat("json_schema") // testing backward compatibility
            .strictJsonSchema(true)
            .temperature(0.0)
            .logRequests(true)
            .logResponses(true)
            .build();

    @Override
    protected List<ChatModel> models() {
        return List.of(
                modelWithStrictJsonSchema,
                modelWithStrictJsonSchemaLegacy,
                OpenAiChatModel.builder()
                        .baseUrl("https://api.minimaxi.com/v1")
                        .apiKey("sk-cp-k94NcRwMEUPF_ls-fqeyN9Gk9msntO6yv1WZxwhV3joGHW8rXfeP5Xqe27hsklfARyU0YnqQW1acyFsR6nWKE85mp8HJLFoz0YiZklBukg4_LXilJ5JXbyM")
                        .organizationId("MiniMax")
                        .modelName("MiniMax-M2.7")
                        .supportedCapabilities(RESPONSE_FORMAT_JSON_SCHEMA)
                        .strictJsonSchema(false)
                        .temperature(0.0)
                        .logRequests(true)
                        .logResponses(true)
                        .build(),
                OpenAiChatModel.builder()
                        .baseUrl("https://api.minimaxi.com/v1")
                        .apiKey("sk-cp-k94NcRwMEUPF_ls-fqeyN9Gk9msntO6yv1WZxwhV3joGHW8rXfeP5Xqe27hsklfARyU0YnqQW1acyFsR6nWKE85mp8HJLFoz0YiZklBukg4_LXilJ5JXbyM")
                        .organizationId("MiniMax")
                        .modelName("MiniMax-M2.7")
                        .responseFormat("json_schema") // testing backward compatibility
                        .strictJsonSchema(false)
                        .temperature(0.0)
                        .logRequests(true)
                        .logResponses(true)
                        .build());
    }

    @Override
    protected boolean supportsRecursion() {
        return true;
    }

    @Override
    protected boolean isStrictJsonSchemaEnabled(ChatModel model) {
        return model == modelWithStrictJsonSchema || model == modelWithStrictJsonSchemaLegacy;
    }
}
