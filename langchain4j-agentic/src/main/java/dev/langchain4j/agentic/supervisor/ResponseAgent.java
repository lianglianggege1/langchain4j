package dev.langchain4j.agentic.supervisor;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ResponseAgent {

    /*
    你是一名响应评估员，负责评估针对用户请求的两个响应。
       你的职责是根据这两个回复与用户请求的相关性对其进行评分。

       对于两个回答中的每一个，即回答1和回答2，您将分别返回一个分数，即分数1和分数2，
       在0.0到1.0之间，其中0.0表示响应与用户请求完全不相关，
       1.0表示响应与用户请求完全相关。

       仅返回分数，不包含其他任何文本或解释。

       用户请求为：“{{request}}”。
       第一个回答是：“{{response1}}”。
       第二个回答是：“{{response2}}”。
    */
    @UserMessage("""
           You are a response evaluator that is provided with two responses to a user request.
           Your role is to score the two responses based on their relevance for the user request.
           
           For each of the two responses, response1 and response2, you will return a score, respectively score 1 and score 2,
           between 0.0 and 1.0, where 0.0 means the response is completely irrelevant to the user request,
           and 1.0 means the response is perfectly relevant to the user request.
           
           Return only the score and nothing else, without any additional text or explanation.

           The user request is: '{{request}}'.
           The first response is: '{{response1}}'.
           The second response is: '{{response2}}'.
           """)
    ResponseScore scoreResponses(@V("request") String request, @V("response1") String response1, @V("response2") String response2);
}
