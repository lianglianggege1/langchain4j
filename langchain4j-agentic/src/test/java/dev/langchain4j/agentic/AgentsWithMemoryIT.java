package dev.langchain4j.agentic;

import dev.langchain4j.agentic.observability.AgentMonitor;
import dev.langchain4j.agentic.planner.AgentInstance;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static dev.langchain4j.agentic.Models.baseModel;
import static dev.langchain4j.agentic.observability.HtmlReportGenerator.generateReport;
import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
@EnabledIfEnvironmentVariable(named = "GOOGLE_AI_GEMINI_API_KEY", matches = ".+")
public class AgentsWithMemoryIT {

    public interface Assistant {
        @SystemMessage("你是一名机器人研发智能体，需要确定待制造的机器人数量。")
        @UserMessage("{{request}}")
        @Agent
        String processData(@V("request") String request);
    }

    // 相关流程其实没怎么明白和串起来
    @Test
    public void agent_with_default_chat_memory_test() {
        Assistant assistant = AgenticServices.agentBuilder(Assistant.class)
                .chatModel(baseModel())
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .outputKey("response")
                .build();

        UntypedAgent sequenceAgent = AgenticServices.sequenceBuilder()
                .subAgents(assistant)
                .outputKey("response")
                .build();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("request", "构建一个机器人");

        String result = (String) sequenceAgent.invoke(parameters);
        assertThat(result).containsIgnoringCase("机器");
    }
}
