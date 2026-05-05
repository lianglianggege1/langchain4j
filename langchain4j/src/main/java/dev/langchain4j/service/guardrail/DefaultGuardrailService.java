package dev.langchain4j.service.guardrail;

import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailExecutor;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailExecutor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Responsible for managing and applying input and output guardrails
 * to methods of a specified AI service class. Guardrails are defined through annotations
 * at either the class or method level and are used to enforce constraints or rules for
 * processing requests and responses.
 *
 * This class initializes guardrails for all methods of the specified AI service class,
 * allowing input and output validation, transformation, or restriction through the
 * specified guardrail implementations. The guardrails can be customized through configurations
 * specific to each guardrail type.
 * <p>
 *     Obtain instances via {@link GuardrailService#builder(Class)}
 * </p>
 */
/**
 * 负责对指定 AI 服务类的方法管理并应用输入、输出护栏。
 * 护栏通过类级别或方法级别的注解定义，用于为请求和响应的处理强制施加约束或规则。
 *
 * 本类会为指定 AI 服务类的所有方法初始化护栏，
 * 支持通过指定的护栏实现对输入和输出进行校验、转换或限制。
 * 护栏可通过每种护栏类型专属的配置进行自定义。
 * <p>
 *     通过 {@link GuardrailService#builder(Class)} 获取实例
 * </p>
 */
final class DefaultGuardrailService extends AbstractGuardrailService {
    DefaultGuardrailService(
            Class<?> aiServiceClass,
            Map<Object, InputGuardrailExecutor> inputGuardrails,
            Map<Object, OutputGuardrailExecutor> outputGuardrails) {
        super(aiServiceClass, inputGuardrails, outputGuardrails);
    }

    // These methods below really only exist for testing purposes
    // Thats why they are package-scoped
    Optional<dev.langchain4j.guardrail.config.InputGuardrailsConfig> getInputConfig(String methodName) {
        return findMethod(methodName).flatMap(super::getInputConfig);
    }

    Optional<dev.langchain4j.guardrail.config.OutputGuardrailsConfig> getOutputConfig(String methodName) {
        return findMethod(methodName).flatMap(super::getOutputConfig);
    }

    List<InputGuardrail> getInputGuardrails(String methodName) {
        return findMethod(methodName).map(super::getInputGuardrails).orElseGet(List::of);
    }

    List<OutputGuardrail> getOutputGuardrails(String methodName) {
        return findMethod(methodName).map(super::getOutputGuardrails).orElseGet(List::of);
    }

    private Optional<Method> findMethod(String methodName) {
        return Stream.of(aiServiceClass().getMethods())
                .filter(method -> methodName.equals(method.getName()))
                .findFirst();
    }
}
