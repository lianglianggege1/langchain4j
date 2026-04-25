package dev.langchain4j.service;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * When a method in the AI Service is annotated with @Moderate, each invocation of this method will call not only the LLM,
 * but also the moderation model (which must be provided during the construction of the AI Service) in parallel.
 * This ensures that no malicious content is supplied by the user.
 * Before the method returns an answer from the LLM, it will wait until the moderation model returns a result.
 * If the moderation model flags the content, a ModerationException will be thrown.
 * There is also an option to moderate user input *before* sending it to the LLM. If you require this functionality,
 * please open an issue.
 * <p/>
 * 当AI服务中的一个方法被注释为@Moderate时，
 * 每次调用此方法不仅会调用LLM，还会并行调用审核模型（必须在构建AI服务期间提供）。
 * 这确保了用户不会提供恶意内容。在该方法从LLM返回答案之前，
 * 它将等待审核模型返回结果。如果审核模型标记了内容，
 * 将抛出ModerationException。
 * 还有一个选项可以在将用户输入发送到LLM之前对其进行调节。如果您需要此功能，请打开问题。
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface Moderate {

}
