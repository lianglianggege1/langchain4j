package dev.langchain4j.agentic.patterns.bdi.trading;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.declarative.Output;
import dev.langchain4j.agentic.observability.MonitoredAgent;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.agentic.scope.ResultWithAgenticScope;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public class TradingAgents {

    /*
    - STRONG_BUY：强烈买入
    - BUY：买入
    - HOLD：持有
    - SELL：卖出
    - STRONG_SELL：强烈卖出
     */
    public enum MarketRecommendation {
        STRONG_BUY, BUY, HOLD, SELL, STRONG_SELL
    }

//    public interface MarketAnalysisAgent {
//
//        @UserMessage("""
//                You are a market analyst. Analyze the following market data and portfolio state.
//                Provide a concise assessment of the current market conditions, risks, and opportunities.
//                Market data: {{marketData}}
//                Portfolio: {{portfolio}}
//                """)
//        @Agent(value = "Analyze market conditions and identify risks/opportunities", outputKey = "marketAnalysis")
//        String analyzeMarket(@V("marketData") String marketData, @V("portfolio") String portfolio);
//    }

    public interface MarketAnalysisAgent {
        @UserMessage("""
            你是一名市场分析师。分析以下市场数据与投资组合状态。
            简要评估当前市场环境、风险以及投资机会。
            市场数据：{{marketData}}
            投资组合：{{portfolio}}
            """)
        @Agent(value = "分析市场行情，识别风险与机会", outputKey = "marketAnalysis")
        String analyzeMarket(@V("marketData") String marketData, @V("portfolio") String portfolio);
    }


    public interface MarketRecommendationAgent {

        /*@UserMessage("""
                You are a financial advisor. Based on the following market analysis,
                provide a single trading recommendation.
                Market analysis: {{marketAnalysis}}
                """)
        @Agent(value = "Provide a trading recommendation based on market analysis", outputKey = "recommendation")
        MarketRecommendation recommend(@V("marketAnalysis") String marketAnalysis);*/

        @UserMessage("""
        你是一名金融顾问。基于以下市场分析，
        给出单一交易建议。
        市场分析：{{marketAnalysis}}
        """)
        @Agent(value = "根据市场分析输出交易建议", outputKey = "recommendation")
        MarketRecommendation recommend(@V("marketAnalysis") String marketAnalysis);


    }

    // 对冲策略默认处理器
    public static class HedgingStrategyDefaulter {
        @Agent(outputKey = "hedgingStrategy")
        public String defaultHedging(AgenticScope scope) {
            return scope.hasState("hedgingStrategy") ? (String) scope.readState("hedgingStrategy") : "None";
        }
    }

    public interface RebalancingAgent {

        /*@UserMessage("""
                You are a portfolio rebalancing specialist.
                Based on the market analysis, suggest portfolio adjustments to maximize returns.
                Keep suggestions concise and actionable.
                Market analysis: {{marketAnalysis}}
                Hedging strategy in place: {{hedgingStrategy}}
                Portfolio: {{portfolio}}
                """)
        @Agent(value = "Suggest portfolio rebalancing to maximize returns", outputKey = "rebalancingPlan")
        String rebalance(@V("marketAnalysis") String marketAnalysis,
                         @V("hedgingStrategy") String hedgingStrategy,
                         @V("portfolio") String portfolio);*/

        @UserMessage("""
        你是投资组合再平衡专家。
        根据市场分析，给出投资组合调整方案以最大化收益。
        建议保持简洁、具备可执行性。
        市场分析：{{marketAnalysis}}
        当前对冲策略：{{hedgingStrategy}}
        投资组合：{{portfolio}}
        """)
        @Agent(value = "给出投资组合再平衡建议以最大化收益", outputKey = "rebalancingPlan")
        String rebalance(@V("marketAnalysis") String marketAnalysis,
                         @V("hedgingStrategy") String hedgingStrategy,
                         @V("portfolio") String portfolio);

    }

    public interface HedgingAgent {

        /*@UserMessage("""
                You are a risk management specialist.
                Based on the market analysis, recommend hedging strategies to minimize risk exposure.
                Focus on protecting against identified threats.
                Market analysis: {{marketAnalysis}}
                """)
        @Agent(value = "Recommend hedging strategies to minimize risk", outputKey = "hedgingStrategy")
        String hedge(@V("marketAnalysis") String marketAnalysis);*/

        @UserMessage("""
        你是风险管理专家。
        根据市场分析，推荐对冲策略以降低风险敞口。
        重点针对已识别的风险威胁进行防护。
        市场分析：{{marketAnalysis}}
        """)
        @Agent(value = "推荐对冲策略以降低风险", outputKey = "hedgingStrategy")
        String hedge(@V("marketAnalysis") String marketAnalysis);


    }

    public interface LiquidityAgent {

        /*@UserMessage("""
                You are a liquidity management specialist.
                Based on the portfolio state, assess current liquidity and recommend actions
                to maintain adequate cash reserves.
                Portfolio: {{portfolio}}
                """)
        @Agent(value = "Assess and maintain portfolio liquidity", outputKey = "liquidityAssessment")
        String assessLiquidity(@V("portfolio") String portfolio);*/

        @UserMessage("""
        你是流动性管理专家。
        根据投资组合状态，评估当前流动性，并给出操作建议，
        以维持充足的现金储备。
        投资组合：{{portfolio}}
        """)
        @Agent(value = "评估并维护投资组合流动性", outputKey = "liquidityAssessment")
        String assessLiquidity(@V("portfolio") String portfolio);


    }

    public interface TradingSystem extends MonitoredAgent {

        @Agent
        ResultWithAgenticScope<String> trade(@V("marketData") String marketData, @V("portfolio") String portfolio);

        @Output
        static String trade(@V("marketAnalysis") String marketAnalysis,
                            @V("recommendation") MarketRecommendation recommendation,
                            @V("rebalancingPlan") String rebalancingPlan,
                            @V("liquidityAssessment") String liquidityAssessment,
                            AgenticScope scope) {
            String hedging = scope.hasState("hedgingStrategy")
                    ? (String) scope.readState("hedgingStrategy")
                    : "Not needed (recommendation: " + recommendation + ")";
            return "Trading system output:\n----\n" +
                    "Market Analysis: " + marketAnalysis + "\n----\n" +
                    "Recommendation: " + recommendation + "\n----\n" +
                    "Hedging Strategy: " + hedging + "\n----\n" +
                    "Rebalancing Plan: " + rebalancingPlan + "\n----\n" +
                    "Liquidity Assessment: " + liquidityAssessment;
        }
    }
}
