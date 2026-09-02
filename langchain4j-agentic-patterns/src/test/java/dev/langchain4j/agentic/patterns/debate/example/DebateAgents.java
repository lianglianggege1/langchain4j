package dev.langchain4j.agentic.patterns.debate.example;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.observability.MonitoredAgent;
import dev.langchain4j.agentic.scope.ResultWithAgenticScope;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import static dev.langchain4j.agentic.patterns.debate.DebatePlanner.DEBATE_CONTEXT_KEY;

public class DebateAgents {

    public interface UtilitarianDebater {

        /*@UserMessage("""
                You are a utilitarian ethics debater. \
                Consider the following question and argue from a utilitarian perspective, maximizing overall well-being.
                If previous debate context is provided, consider the other debaters' arguments and refine your position.
                Keep your response to 2-3 sentences. End with a one-word verdict: AGREE or DISAGREE.
                Question: {{question}}
                Previous debate context: {{debateContext}}
                """)
        @Agent(description = "Argues from a utilitarian ethics perspective", name = "Utilitarian")
        String debate(@V("question") String question, @V(DEBATE_CONTEXT_KEY) String debateContext);*/

        @UserMessage("""
        你是功利主义伦理辩手。
        针对下述问题，从功利主义视角展开论证，以最大化整体福祉为目标。
        如果提供过往辩论上下文，请参考其他辩手观点并优化自身立场。
        回答控制在2‑3句话，结尾输出单个单词裁决：AGREE（同意）或 DISAGREE（不同意）。
        问题：{{question}}
        过往辩论上下文：{{debateContext}}
        """)
        @Agent(description = "从功利主义伦理视角进行辩论", name = "Utilitarian")
        String debate(@V("question") String question, @V(DEBATE_CONTEXT_KEY) String debateContext);

    }

    public interface DeontologicalDebater {

        /*@UserMessage("""
                You are a deontological ethics debater. \
                Consider the following question and argue based on moral rules, duties, and rights.
                If previous debate context is provided, consider the other debaters' arguments and refine your position.
                Keep your response to 2-3 sentences. End with a one-word verdict: AGREE or DISAGREE.
                Question: {{question}}
                Previous debate context: {{debateContext}}
                """)
        @Agent(description = "Argues from a deontological ethics perspective", name = "Deontologist")
        String debate(@V("question") String question, @V(DEBATE_CONTEXT_KEY) String debateContext);*/

        @UserMessage("""
        你是义务论伦理辩手。
        针对下述问题，基于道德准则、义务与权利展开论证。
        如果提供过往辩论上下文，请参考其他辩手观点并优化自身立场。
        回答控制在2‑3句话，结尾输出单个单词裁决：AGREE（同意）或 DISAGREE（不同意）。
        问题：{{question}}
        过往辩论上下文：{{debateContext}}
        """)
        @Agent(description = "从义务论伦理视角进行辩论", name = "Deontologist")
        String debate(@V("question") String question, @V(DEBATE_CONTEXT_KEY) String debateContext);


    }

    public interface PragmatistDebater {

        /*@UserMessage("""
                You are a pragmatist debater. \
                Consider the following question and argue based on practical consequences and real-world outcomes.
                If previous debate context is provided, consider the other debaters' arguments and refine your position.
                Keep your response to 2-3 sentences. End with a one-word verdict: AGREE or DISAGREE.
                Question: {{question}}
                Previous debate context: {{debateContext}}
                """)
        @Agent(description = "Argues from a pragmatist perspective", name = "Pragmatist")
        String debate(@V("question") String question, @V(DEBATE_CONTEXT_KEY) String debateContext);*/

        @UserMessage("""
        你是实用主义辩手。
        针对下述问题，立足实际后果与现实世界结果展开论证。
        如果提供过往辩论上下文，请参考其他辩手观点并优化自身立场。
        回答控制在2‑3句话，结尾输出单个单词裁决：AGREE（同意）或 DISAGREE（不同意）。
        问题：{{question}}
        过往辩论上下文：{{debateContext}}
        """)
        @Agent(description = "从实用主义视角进行辩论", name = "Pragmatist")
        String debate(@V("question") String question, @V(DEBATE_CONTEXT_KEY) String debateContext);

    }

    public interface EthicsJudge {

        /*@UserMessage("""
                You are an impartial ethics judge. \
                Review the debate context where multiple debaters have argued about a question from different perspectives.
                Synthesize their arguments and provide a balanced, well-reasoned final verdict in 3-4 sentences.
                Debate context: {{debateContext}}
                """)
        @Agent(description = "Renders a final verdict by synthesizing debate arguments", name = "Judge")
        String judge(@V("debateContext") String debateContext);*/

        @UserMessage("""
        你是公正的伦理裁判。
        审阅辩论上下文，多位辩手从不同角度就问题展开论证。
        综合各方论点，给出客观公允、逻辑严谨的最终裁决，回答控制在3‑4句话。
        辩论上下文：{{debateContext}}
        """)
        @Agent(description = "综合辩论论点给出最终裁决", name = "Judge")
        String judge(@V("debateContext") String debateContext);

    }

    public interface EthicsPanel extends MonitoredAgent {

        @Agent
        String debate(@V("question") String question);
    }
}
