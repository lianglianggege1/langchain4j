package dev.langchain4j.rag.content.injector;

import static dev.langchain4j.internal.Utils.copy;
import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.internal.ValidationUtils.ensureNotEmpty;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;
import static java.util.stream.Collectors.joining;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.content.Content;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link ContentInjector} intended to be suitable for the majority of use cases.
 * {@link ContentInjector}的默认实现旨在适用于大多数用例。
 * <br>
 * <br>
 * It's important to note that while efforts will be made to avoid breaking changes,
 * the default behavior of this class may be updated in the future if it's found
 * that the current behavior does not adequately serve the majority of use cases.
 * Such changes would be made to benefit both current and future users.
 * 值得注意的是，虽然将努力避免破坏性更改，但如果发现当前行为不能充分服务于大多数用例，
 * 则该类的默认行为可能会在未来进行更新。这些变化将使当前和未来的用户受益。
 * <br>
 * <br>
 * This implementation appends all given {@link Content}s to the end of the given {@link UserMessage}
 * in their order of iteration.
 * Refer to {@link #DEFAULT_PROMPT_TEMPLATE} and implementation for more details.
 * 此实现按照迭代顺序将所有给定的{@link Content}附加到给定的{@link UserMessage}的末尾。
 * 有关更多详细信息，请参阅{@link #DEFAULT_PROMPT_TEMPLATE}和实现。
 * <br>
 * <br>
 * Configurable parameters (optional):
 * 可配置参数（可选）：
 * <br>
 * - {@link #promptTemplate}: The prompt template that defines how the original {@code userMessage}
 * and {@code contents} are combined into the resulting {@link UserMessage}.
 * - {@link#promptTemplate}：定义如何将原始{@code userMessage}和{@code contents}组合成结果{@link UserMessage}的提示模板。
 * The text of the template should contain the {@code {{userMessage}}} and {@code {{contents}}} variables.
 * 模板的文本应包含｛@code｛｛userMessage｝｝｝和｛@code｝｛contents｝｝｝变量。
 * <br>
 * - {@link #metadataKeysToInclude}: A list of {@link Metadata} keys that should be included
 * with each {@link Content#textSegment()}.
 */
public class DefaultContentInjector implements ContentInjector {

    /**
     {{userMessage}}

     使用以下信息回答：
     {{contents}}
     */
    public static final PromptTemplate DEFAULT_PROMPT_TEMPLATE = PromptTemplate.from(
            """
                    {{userMessage}}

                    Answer using the following information:
                    {{contents}}""");

    private final PromptTemplate promptTemplate;
    private final List<String> metadataKeysToInclude;

    public DefaultContentInjector() {
        this(DEFAULT_PROMPT_TEMPLATE, null);
    }

    public DefaultContentInjector(List<String> metadataKeysToInclude) {
        this(DEFAULT_PROMPT_TEMPLATE, ensureNotEmpty(metadataKeysToInclude, "metadataKeysToInclude"));
    }

    public DefaultContentInjector(PromptTemplate promptTemplate) {
        this(ensureNotNull(promptTemplate, "promptTemplate"), null);
    }

    public DefaultContentInjector(PromptTemplate promptTemplate, List<String> metadataKeysToInclude) {
        this.promptTemplate = getOrDefault(promptTemplate, DEFAULT_PROMPT_TEMPLATE);
        this.metadataKeysToInclude = copy(metadataKeysToInclude);
    }

    public static DefaultContentInjectorBuilder builder() {
        return new DefaultContentInjectorBuilder();
    }

    @Override
    public ChatMessage inject(List<Content> contents, ChatMessage chatMessage) {
        if (contents.isEmpty()) {
            return chatMessage;
        }

        // 提示词
        Prompt prompt = createPrompt(chatMessage, contents);
        if (chatMessage instanceof UserMessage userMessage) {
            return userMessage.toBuilder()
                    .contents(List.of(TextContent.from(prompt.text())))
                    .build();
        } else {
            return prompt.toUserMessage();
        }
    }

    protected Prompt createPrompt(ChatMessage chatMessage, List<Content> contents) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("userMessage", ((UserMessage) chatMessage).singleText());
        variables.put("contents", format(contents));
        return promptTemplate.apply(variables);
    }

    protected String format(List<Content> contents) {
        return contents.stream().map(this::format).collect(joining("\n\n"));
    }

    protected String format(Content content) {

        TextSegment segment = content.textSegment();

        if (metadataKeysToInclude.isEmpty()) {
            return segment.text();
        }

        String segmentContent = segment.text();
        String segmentMetadata = format(segment.metadata());

        return format(segmentContent, segmentMetadata);
    }

    protected String format(Metadata metadata) {
        StringBuilder formattedMetadata = new StringBuilder();
        for (String metadataKey : metadataKeysToInclude) {
            String metadataValue = metadata.getString(metadataKey);
            if (metadataValue != null) {
                if (!formattedMetadata.isEmpty()) {
                    formattedMetadata.append("\n");
                }
                formattedMetadata.append(metadataKey).append(": ").append(metadataValue);
            }
        }
        return formattedMetadata.toString();
    }

    protected String format(String segmentContent, String segmentMetadata) {
        return segmentMetadata.isEmpty()
                ? segmentContent
                : String.format("content: %s\n%s", segmentContent, segmentMetadata);
    }

    public static class DefaultContentInjectorBuilder {

        private PromptTemplate promptTemplate;
        private List<String> metadataKeysToInclude;

        DefaultContentInjectorBuilder() {}

        public DefaultContentInjectorBuilder promptTemplate(PromptTemplate promptTemplate) {
            this.promptTemplate = promptTemplate;
            return this;
        }

        public DefaultContentInjectorBuilder metadataKeysToInclude(List<String> metadataKeysToInclude) {
            this.metadataKeysToInclude = metadataKeysToInclude;
            return this;
        }

        public DefaultContentInjector build() {
            return new DefaultContentInjector(this.promptTemplate, this.metadataKeysToInclude);
        }
    }
}
