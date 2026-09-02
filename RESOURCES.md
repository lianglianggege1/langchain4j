# langchain4j agentic patterns 学习资源

## Knowledge

- 本仓库源码（最高优先级，事实来源）
  - `langchain4j-agentic-patterns/src/main/java/dev/langchain4j/agentic/patterns/goap/`：GOAP 三件套（GoalOrientedPlanner / GoalOrientedSearchGraph / DependencyGraphSearch）
  - `langchain4j-agentic/src/main/java/dev/langchain4j/agentic/planner/Planner.java`、`Action.java`：规划器接口与动作语义
  - `langchain4j-agentic/src/main/java/dev/langchain4j/agentic/internal/PlannerBasedInvocationHandler.java`：运行时执行循环 PlannerLoop 所在
  - `langchain4j-agentic/src/main/java/dev/langchain4j/agentic/workflow/impl/LoopPlanner.java`：循环子系统的规划器（writer 实例内层循环）
  - `langchain4j-agentic-patterns/src/test/java/dev/langchain4j/agentic/patterns/goap/writer/`：writer 实例（GoapWriterIT + WriterAgents）
  - `langchain4j-agentic-patterns/src/main/java/dev/langchain4j/agentic/patterns/p2p/`：P2P 三件套（P2PAgent / P2PPlanner / VariablesExtractorAgent），AgentActivator 是激活引擎核心（P2PPlanner.java:128-173）
  - `langchain4j-agentic-patterns/src/test/java/dev/langchain4j/agentic/patterns/p2p/`：P2P 测试锚点（P2PPlannerTest 四用例钉死终止规则；P2PPlannerComposeActionsIT 防冻结回归；researcher/ 为真实 LLM 实例，P2PResearcherIT 目前 @Disabled——环依赖振荡活标本）
  - 使用方式：讲解任何行为前先读对应源码，禁止凭记忆断言。

- [论文: Three States and a Plan: The AI of F.E.A.R. — Jeff Orkin, GDC 2006](https://www.gamedevs.org/uploads/three-states-and-a-plan.pdf)
  GOAP 概念的游戏 AI 起源：动作=前置条件+效果，规划器在运行时搜索满足目标的最短动作序列。用于理解本模式的思想根源。

- [百科: A* search algorithm (Wikipedia)](https://en.wikipedia.org/wiki/A*_search_algorithm)
  A* 的 g(n)+h(n) 语义与可采纳启发式。DependencyGraphSearch 是它的多输入依赖改造版。

- [langchain4j GitHub 仓库](https://github.com/langchain4j/langchain4j)
  上游仓库，用于跟踪 agentic 模块演进。[官方文档站点](https://docs.langchain4j.dev/)。

- [论文: Self-Consistency Improves Chain of Thought Reasoning in Language Models — Wang et al., 2022](https://arxiv.org/abs/2203.11171)
  LLM 投票模式的理论根基：对同一问题采样多条推理路径再多数表决，显著优于单路推理。用于理解 Voting 模式为什么对 LLM 有效。

- [百科: Ensemble learning (Wikipedia)](https://en.wikipedia.org/wiki/Ensemble_learning)
  集成学习（随机森林的多数表决、回归的均值聚合）——Voting 模式在 ML 中的直系亲戚。majority/average/highest 三个内置策略一一对应。

## Wisdom (Communities)

- [langchain4j GitHub Discussions / Issues](https://github.com/langchain4j/langchain4j/discussions)
  提问、验证对运行时行为的理解、跟踪 agentic 模块 API 变更的地方。

## Gaps

- agentic 模块官方教程文档目前较薄，运行时（PlannerLoop、AgenticScope 生命周期）细节主要靠源码与 IT 测试佐证
- GOAP × 恢复/挂起（suspend）组合行为的权威说明缺失，需以 PlannerBasedInvocationHandler 源码为准
