package dev.langchain4j.agentic.patterns.p2p;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import java.util.Collection;
import java.util.Map;

public interface VariablesExtractorAgent {

    @UserMessage("""
            从给定文本中提取指定变量列表对应的值，
            返回一个键为变量名、值为对应提取内容的映射集合。
            若文本中未找到某个变量，则该变量不加入映射集合。
            提取内容需严谨，仅收录文本中明确存在的内容。
            待处理文本：{{text}}
            待提取变量名列表：{{variableNames}}
            """)
    Map<String, String> extractVariables(@V("text") String text, @V("variableNames") Collection<String> variableNames);
}
