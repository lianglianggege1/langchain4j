package dev.langchain4j.rag.query.transformer;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.query.Query;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.stream.Stream;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;
import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class ExpandingQueryTransformerIT {

    @ParameterizedTest
    @MethodSource
    void should_expand_query(ChatModel model) {

        // given
        Query query = Query.from("Tell me about dogs");

        QueryTransformer transformer = new ExpandingQueryTransformer(model);

        // when
        Collection<Query> queries = transformer.transform(query);

        // then
        assertThat(queries).hasSize(3);

        queries.forEach(q -> assertThat(q.text())
                .doesNotStartWith("1")
                .doesNotStartWith("-")
        );
    }

    static Stream<Arguments> should_expand_query() {
        return Stream.of(
                Arguments.of(
                        OpenAiChatModel.builder()
                                .baseUrl("https://api.minimaxi.com/v1")
                                .apiKey("sk-cp-k94NcRwMEUPF_ls-fqeyN9Gk9msntO6yv1WZxwhV3joGHW8rXfeP5Xqe27hsklfARyU0YnqQW1acyFsR6nWKE85mp8HJLFoz0YiZklBukg4_LXilJ5JXbyM")
                                .organizationId("MiniMax")
                                .modelName("MiniMax-M2.7")
                                .logRequests(true)
                                .logResponses(true)
                                .build()
                )
                // TODO add more models
        );
    }
}
