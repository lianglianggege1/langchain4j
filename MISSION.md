# Mission: 掌握 langchain4j-agentic-patterns 的智能体编排模式

## Why
系统性地吃透本仓库 `langchain4j-agentic` / `langchain4j-agentic-patterns` 模块中的每一种编排模式（blackboard、GOAP、BDI、debate、voting、P2P 等），能读懂源码与运行时执行机制，最终在自己的 Java 智能体项目中选型并扩展这些模式。

## Success looks like
- 给任意一个模式，能说出它的 Planner 决策逻辑、Runtime（PlannerLoop）执行路径与 AgenticScope 状态流转
- 能对着仓库里的 IT 测试实例，手工推演出规划路径与执行顺序
- 能自己写一个新的 Planner 实现（如自定义搜索策略）并接入 plannerBuilder

## Constraints
- 教学语言为中文
- 以本地仓库源码与测试实例为唯一事实来源（可离线学习）
- 每次一个模式、一个实例，小步推进

## Out of scope
- 其他语言/框架的 agent 编排（LangGraph、CrewAI 等）仅作对照，不作学习对象
- langchain4j 非 agentic 部分（RAG、embedding 等）
