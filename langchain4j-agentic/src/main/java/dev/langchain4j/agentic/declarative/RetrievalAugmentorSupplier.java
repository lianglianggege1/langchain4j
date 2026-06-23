package dev.langchain4j.agentic.declarative;

import dev.langchain4j.rag.RetrievalAugmentor;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a method as a supplier of a retrieval augmentor that an agent can utilize during its operation.
 * The annotated method must be static, with no arguments, and return an instance of {@link RetrievalAugmentor}.
 * <p>
 * Example:
 * <pre>
 * {@code
 *     public interface ResearchAgent {
 *         @Agent("A research agent")
 *         String research(@V("topic") String topic);
 *
 *         @RetrievalAugmentorSupplier
 *         static RetrievalAugmentor retrievalAugmentor() {
 *             return RetrievalAugmentors.from(yourVectorStore);
 *         }
 *     }
 * }
 * </pre>
 */
/**
 * 将方法标记为检索增强器提供器，智能体运行过程中可使用该增强器。
 * 被该注解修饰的方法必须为无参静态方法，且返回 {@link RetrievalAugmentor} 实例。
 * <p>
 * 示例：
 * <pre>
 * {@code
 *     public interface ResearchAgent {
 *         @Agent("调研智能体")
 *         String research(@V("topic") String topic);
 *
 *         @RetrievalAugmentorSupplier
 *         static RetrievalAugmentor retrievalAugmentor() {
 *             return RetrievalAugmentors.from(yourVectorStore);
 *         }
 *     }
 * }
 * </pre>
 */
@Retention(RUNTIME)
@Target({METHOD})
public @interface RetrievalAugmentorSupplier {
}
