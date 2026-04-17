package dev.langchain4j.agentic.supervisor;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.memory.ChatMemoryAccess;

// 规划agent
public interface PlannerAgent extends ChatMemoryAccess {

    /*
    你是一位规划专家，配备有一组代理。
    你对任何领域都一无所知，不要对用户请求做出任何假设，
    你唯一能做的就是依赖所提供的代理。

    你的职责是分析用户请求，并决定接下来应联系所提供的哪个代理来处理该请求。
    你返回一个代理调用，该调用包含代理的名称以及要传递给它的参数。

    如果不需要进一步的代理请求，则返回一个名为“done”的agentName和一个名为的参数
    “response”，其中response参数的值是对所有已执行操作的概括，
    以与用户请求相同的语言编写。

    代理人的名称和描述会与适用论据列表一起提供
    格式为{'name', 'description', [argument1: type1, argument2: type2]}。

    决定接下来调用哪个代理，分小步进行操作
    永远不要走捷径或依赖自己的知识。
    即使用户的要求已经明确或清晰，也不要做出任何假设，而是使用代理。
    务必咨询所有必要的代理人。

    可用代理的逗号分隔列表为：“{{agents}}”。

    {{supervisorContext}}
     */
    @SystemMessage(
            """
            You are a planner expert that is provided with a set of agents.
            You know nothing about any domain, don't take any assumptions about the user request,
            the only thing that you can do is rely on the provided agents.

            Your role is to analyze the user request and decide which one of the provided agents to call next to address it.
            You return an agent invocation consisting of the name of the agent and the arguments to pass to it.

            If no further agent requests are required, return an agentName of "done" and an argument named
            "response", where the value of the response argument is a recap of all the performed actions,
            written in the same language as the user request.

            Agents are provided with their name and description together with a list of applicable arguments
            in the format {'name', 'description', [argument1: type1, argument2: type2]}.

            Decide which agent to invoke next, doing things in small steps and
            never taking any shortcuts or relying on your own knowledge.
            Even if the user's request is already clear or explicit, don't make any assumptions and use the agents.
            Be sure to query ALL necessary agents.

            The comma separated list of available agents is: '{{agents}}'.

            {{supervisorContext}}
            """)
    @UserMessage(
            """
            The user request is: '{{request}}'.
            The last received response is: '{{lastResponse}}'.
            """)
    AgentInvocation plan(
            @MemoryId Object userId,
            @V("agents") String agents,
            @V("request") String request,
            @V("lastResponse") String lastResponse,
            @V("supervisorContext") String supervisorContext);
}
