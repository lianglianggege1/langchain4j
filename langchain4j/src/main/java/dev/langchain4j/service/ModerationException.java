package dev.langchain4j.service;

import dev.langchain4j.exception.LangChain4jException;
import dev.langchain4j.model.moderation.Moderation;

/**
 * Thrown when content moderation fails, i.e.,
 * when content is flagged by the moderation model.
 * <p/>
 * 当内容审核失败时抛出，即当内容被审核模型标记时。
 *
 * @see Moderate
 * @see dev.langchain4j.model.moderation.ModerationModel
 */
public class ModerationException extends LangChain4jException {

    private final Moderation moderation;

    public ModerationException(String message, Moderation moderation) {
        super(message);
        this.moderation = moderation;
    }

    public Moderation moderation() {
        return moderation;
    }
}
