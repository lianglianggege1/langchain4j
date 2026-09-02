package dev.langchain4j.agentic.patterns.blackboard;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.observability.MonitoredAgent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public class BlackboardMedicalAgents {

    public interface SymptomExtractor {

        /*@UserMessage("""
                You are a medical symptom extraction specialist.
                Extract and summarize the key symptoms from the following patient description.
                List each symptom clearly and concisely.
                Patient description: {{patientInput}}
                """)
        @Agent(value = "Extract symptoms from patient input", outputKey = "symptoms")
        String extractSymptoms(@V("patientInput") String patientInput);*/

        @UserMessage("""
        你是医学症状提取专家。
        从下面患者描述中提取并汇总核心症状。
        清晰简洁地逐条列出各项症状。
        患者描述：{{patientInput}}
        """)
        @Agent(value = "从患者输入中提取症状", outputKey = "symptoms")
        String extractSymptoms(@V("patientInput") String patientInput);

    }

    public interface LabResultAnalyzer {

        /*@UserMessage("""
                You are a clinical laboratory specialist.
                Analyze the following lab results and provide a summary of findings,
                highlighting any abnormal values and their clinical significance.
                Lab results: {{labResults}}
                """)
        @Agent(value = "Analyze lab results", outputKey = "labAnalysis")
        String analyzeLabResults(@V("labResults") String labResults);*/

        @UserMessage("""
        你是临床检验专家。
        分析下述检验结果，给出结果汇总，
        标出异常数值及其临床意义。
        检验结果：{{labResults}}
        """)
        @Agent(value = "分析检验结果", outputKey = "labAnalysis")
        String analyzeLabResults(@V("labResults") String labResults);

    }

    public interface DrugInteractionChecker {

        /*@UserMessage("""
                You are a pharmacology specialist.
                Based on the patient's symptoms and current medications,
                identify any potential drug interactions or contraindications.
                Symptoms: {{symptoms}}
                Current medications: {{medications}}
                """)
        @Agent(value = "Check drug interactions based on symptoms and medications", outputKey = "drugInteractions")
        String checkInteractions(@V("symptoms") String symptoms, @V("medications") String medications);*/

        @UserMessage("""
        你是药理学专家。
        根据患者症状与当前用药，
        识别潜在的药物相互作用与用药禁忌。
        症状：{{symptoms}}
        当前用药：{{medications}}
        """)
        @Agent(value = "结合症状与用药检查药物相互作用", outputKey = "drugInteractions")
        String checkInteractions(@V("symptoms") String symptoms, @V("medications") String medications);


    }

    public interface DiagnosisAgent {

        /*@UserMessage("""
                You are an experienced diagnostician.
                Based on the extracted symptoms and lab analysis, provide a preliminary diagnosis.
                Consider the most likely conditions and any differential diagnoses.
                Symptoms: {{symptoms}}
                Lab analysis: {{labAnalysis}}
                """)
        @Agent(value = "Provide diagnosis based on symptoms and lab analysis", outputKey = "diagnosis")
        String diagnose(@V("symptoms") String symptoms, @V("labAnalysis") String labAnalysis);*/

        @UserMessage("""
        你是药理学专家。
        根据患者症状与当前用药，
        识别潜在的药物相互作用与用药禁忌。
        症状：{{symptoms}}
        当前用药：{{medications}}
        """)
        @Agent(value = "结合症状与用药检查药物相互作用", outputKey = "drugInteractions")
        String checkInteractions(@V("symptoms") String symptoms, @V("medications") String medications);


    }

    public interface MedicalDiagnostics extends MonitoredAgent {

        @Agent
        String diagnose(@V("patientInput") String patientInput,
                        @V("labResults") String labResults,
                        @V("medications") String medications);
    }
}
