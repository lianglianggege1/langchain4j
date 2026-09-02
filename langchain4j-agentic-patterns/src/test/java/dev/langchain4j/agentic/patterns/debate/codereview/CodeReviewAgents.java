package dev.langchain4j.agentic.patterns.debate.codereview;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import static dev.langchain4j.agentic.patterns.debate.DebatePlanner.DEBATE_CONTEXT_KEY;

public class CodeReviewAgents {

    public interface BugHunter {

        /*@UserMessage("""
                You are a code reviewer focused on correctness and bug detection. \
                Analyze the following Java code snippet for logical errors, off-by-one mistakes, null safety issues, \
                race conditions, and any other bugs.
                If previous debate context is provided, consider the other reviewers' findings and refine your analysis: \
                confirm valid findings, dispute false positives, and add anything missed.
                Keep your response to 3-4 sentences. End with a one-word verdict: APPROVE or REJECT.
                Code: {{code}}
                Previous debate context: {{debateContext}}
                """)
        @Agent(description = "Reviews code for correctness and bugs", name = "BugHunter")
        String review(@V("code") String code, @V(DEBATE_CONTEXT_KEY) String debateContext);*/

        @UserMessage("""
        你是专注于正确性与缺陷检测的代码评审人员。
        分析下面的Java代码片段，查找逻辑错误、差一错误、空安全问题、竞态条件以及其他各类bug。
        如果提供了过往辩论上下文，请参考其他评审人员的发现并优化你的分析：
        确认有效问题，驳斥误报，补充遗漏点。
        回答控制在3‑4句话，结尾输出单个单词裁决：APPROVE（通过）或 REJECT（驳回）。
        代码：{{code}}
        过往辩论上下文：{{debateContext}}
        """)
        @Agent(description = "评审代码正确性与缺陷", name = "BugHunter")
        String review(@V("code") String code, @V(DEBATE_CONTEXT_KEY) String debateContext);

    }

    public interface SecurityReviewer {

        /*@UserMessage("""
                You are a code reviewer focused on security vulnerabilities. \
                Analyze the following Java code snippet for injection flaws, unsafe deserialization, \
                information leakage, improper input validation, and other OWASP top 10 risks.
                If previous debate context is provided, consider the other reviewers' findings and refine your analysis: \
                confirm valid findings, dispute false positives, and add anything missed.
                Keep your response to 3-4 sentences. End with a one-word verdict: APPROVE or REJECT.
                Code: {{code}}
                Previous debate context: {{debateContext}}
                """)
        @Agent(description = "Reviews code for security vulnerabilities", name = "SecurityReviewer")
        String review(@V("code") String code, @V(DEBATE_CONTEXT_KEY) String debateContext);*/

        @UserMessage("""
        你是专注于安全漏洞的代码评审人员。
        分析下述Java代码片段，检测注入漏洞、不安全反序列化、信息泄露、输入校验不当以及其他OWASP Top 10风险。
        如果提供了过往辩论上下文，请参考其他评审人员的发现并优化你的分析：
        确认有效问题，驳斥误报，补充遗漏点。
        回答控制在3‑4句话，结尾输出单个单词裁决：APPROVE（通过）或 REJECT（驳回）。
        代码：{{code}}
        过往辩论上下文：{{debateContext}}
        """)
        @Agent(description = "评审代码安全漏洞", name = "SecurityReviewer")
        String review(@V("code") String code, @V(DEBATE_CONTEXT_KEY) String debateContext);


    }

    public interface DesignCritic {

        /*@UserMessage("""
                You are a code reviewer focused on design quality and maintainability. \
                Analyze the following Java code snippet for SOLID violations, poor abstractions, \
                code smells, readability issues, and missing error handling.
                If previous debate context is provided, consider the other reviewers' findings and refine your analysis: \
                confirm valid findings, dispute false positives, and add anything missed.
                Keep your response to 3-4 sentences. End with a one-word verdict: APPROVE or REJECT.
                Code: {{code}}
                Previous debate context: {{debateContext}}
                """)
        @Agent(description = "Reviews code for design quality and maintainability", name = "DesignCritic")
        String review(@V("code") String code, @V(DEBATE_CONTEXT_KEY) String debateContext);*/

        @UserMessage("""
        你是专注于设计质量与可维护性的代码评审人员。
        分析下述Java代码片段，检查是否违反SOLID原则、抽象设计不佳、代码坏味道、可读性问题以及缺失异常处理。
        如果提供了过往辩论上下文，请参考其他评审人员的发现并优化你的分析：
        确认有效问题，驳斥误报，补充遗漏点。
        回答控制在3‑4句话，结尾输出单个单词裁决：APPROVE（通过）或 REJECT（驳回）。
        代码：{{code}}
        过往辩论上下文：{{debateContext}}
        """)
        @Agent(description = "评审代码设计质量与可维护性", name = "DesignCritic")
        String review(@V("code") String code, @V(DEBATE_CONTEXT_KEY) String debateContext);

    }

    public interface ReviewSummarizer {

        /*@UserMessage("""
                You are a senior engineer summarizing a code review. \
                Multiple reviewers have analyzed a code snippet from different angles: bugs, security, and design. \
                Synthesize their findings into a final review summary. List the confirmed issues, \
                note any disagreements that were resolved, and give a final recommendation.
                Keep your response to 4-5 sentences.
                Debate context: {{debateContext}}
                """)
        @Agent(description = "Summarizes the code review debate into a final verdict", name = "Summarizer")
        String summarize(@V(DEBATE_CONTEXT_KEY) String debateContext);*/

        @UserMessage("""
        你是资深工程师，负责汇总代码评审结果。
        多名评审人员分别从缺陷、安全、设计多个维度分析代码片段。
        整合各方发现生成最终评审摘要，列出确认存在的问题，
        记录已达成共识的分歧点，并给出最终建议。
        回答控制在4‑5句话。
        辩论上下文：{{debateContext}}
        """)
        @Agent(description = "将代码评审辩论汇总为最终裁决", name = "Summarizer")
        String summarize(@V(DEBATE_CONTEXT_KEY) String debateContext);

    }

    public interface CodeReviewPanel {

        @Agent
        String review(@V("code") String code);
    }
}
