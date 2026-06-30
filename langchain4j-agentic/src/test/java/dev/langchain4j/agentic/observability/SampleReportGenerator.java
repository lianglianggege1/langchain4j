package dev.langchain4j.agentic.observability;

import static dev.langchain4j.agentic.observability.HtmlReportGenerator.generateReport;

import dev.langchain4j.agentic.declarative.TypedKey;
import dev.langchain4j.agentic.planner.AgentArgument;
import dev.langchain4j.agentic.planner.AgentInstance;
import dev.langchain4j.agentic.planner.AgenticSystemTopology;
import dev.langchain4j.agentic.planner.Planner;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.agentic.workflow.ConditionalAgent;
import dev.langchain4j.agentic.workflow.ConditionalAgentInstance;
import dev.langchain4j.agentic.workflow.LoopAgentInstance;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generates a sample HTML report with mock data to preview the AgenticSystemReport visualization.
 * Run the main method and open the generated sample-report.html in a browser.
 */
/**
 * 使用模拟数据生成一份HTML示例报告，用于预览智能代理系统报告（AgenticSystemReport）的可视化效果。
 * 运行主方法后，在浏览器中打开生成的 sample-report.html 文件即可查看效果。
 */

// 这个方法很有意义，能了解到agentic graph 的全景图 ，了解其内部思想
public class SampleReportGenerator {

    // Marker interfaces so type().getSimpleName() returns meaningful names
    /**
     * 专家路由代理（负责将请求路由至对应领域的专家代理）
     */
    interface ExpertRouterAgent {}

    /**
     * 分类路由（根据类别将请求分发至对应处理模块）
     */
    interface CategoryRouter {}

    /**
     * 医疗专家（处理医疗领域相关请求的专家代理）
     */
    interface MedicalExpert {}

    /**
     * 技术专家（处理技术领域相关请求的专家代理）
     */
    interface TechnicalExpert {}

    /**
     * 故事创作者（负责创作、编写故事内容的代理）
     */
    interface StoryWriter {}

    /**
     * 风格评分器（用于评估文本/内容的风格匹配度、质量等指标）
     */
    interface StyleScorer {}

    public static void main(String[] args) throws Exception {

        // ----- Build topology: Sequence → classify + Router(medical|technical|legal(loop)) -----

        MockAgent classify = new MockAgent(
                "分类",
                CategoryRouter.class,
                AgenticSystemTopology.AI_AGENT,
                "将用户问题划分至不同业务领域",
                List.of(new AgentArgument(String.class, "question")),
                "category",
                String.class);

        MockAgent medical = new MockAgent(
                "医疗",
                MedicalExpert.class,
                AgenticSystemTopology.AI_AGENT,
                "提供医疗建议与急救指导",
                List.of(new AgentArgument(String.class, "question")),
                "response",
                String.class);

        MockAgent technical = new MockAgent(
                "技术",
                TechnicalExpert.class,
                AgenticSystemTopology.AI_AGENT,
                "提供技术支持与故障排查",
                List.of(new AgentArgument(String.class, "question")),
                "response",
                String.class);

        // Legal branch uses a loop: writer + scorer, max 3 iterations
        MockAgent writer = new MockAgent(
                "作家",
                StoryWriter.class,
                AgenticSystemTopology.AI_AGENT,
                "起草法律答复",
                List.of(new AgentArgument(String.class, "question")),
                "response",
                String.class);

        MockAgent scorer = new MockAgent(
                "评分器",
                StyleScorer.class,
                AgenticSystemTopology.AI_AGENT,
                "评定法律答复的质量",
                List.of(new AgentArgument(String.class, "response")),
                "score",
                Double.class);

        MockLoopAgent legalLoop = new MockLoopAgent(
                "法律优化",
                "迭代优化法律答复，直至达到质量阈值",
                List.of(writer, scorer),
                "response",
                Object.class,
                3,
                "score greater than 0.8",
                true);

        MockConditionalAgent router = new MockConditionalAgent(
                "路由",
                "根据类别转接至对应领域专家",
                "response",
                Object.class,
                List.of(
                        new ConditionalAgent("医疗类", null, List.of(medical)),
                        new ConditionalAgent("技术类", null, List.of(technical)),
                        new ConditionalAgent("法律类", null, List.of(legalLoop))));

        MockAgent sequence = new MockAgent(
                "ask",
                ExpertRouterAgent.class,
                AgenticSystemTopology.SEQUENCE,
                null,
                List.of(),
                "response",
                String.class);
        sequence.subagents = List.of(classify, router);

        // Wire parent references
        classify.parent = sequence;
        router.parent = sequence;
        medical.parent = router;
        technical.parent = router;
        legalLoop.parent = router;
        writer.parent = legalLoop;
        scorer.parent = legalLoop;

        // ----- Create monitor and simulate executions -----

        AgentMonitor monitor = new AgentMonitor();
        monitor.setRootAgent(sequence);

        // Execution 1 (user-alice): medical path
        /*simulateExecution(
                monitor,
                new MockScope("user-alice"),
                sequence,
                classify,
                router,
                medical,
                Map.of("question", "徒步时摔断了腿，我该怎么办？"),
                "MEDICAL",
                "立即就医。固定伤腿并拨打急救电话。");*/

        // Execution 2 (user-bob): technical path
//        simulateExecution(
//                monitor,
//                new MockScope("user-bob"),
//                sequence,
//                classify,
//                router,
//                technical,
//                Map.of("question", "软件更新后笔记本电脑屏幕闪烁，该如何解决？"),
//                "TECHNICAL",
//                "通过设备管理器回退显卡驱动。若无效，请进入安全模式启动设备。");

//        // Execution 3 (user-alice again): legal path with loop iterations
//        simulateLegalExecution(
//                monitor,
//                new MockScope("user-alice"),
//                sequence,
//                classify,
//                router,
//                legalLoop,
//                writer,
//                scorer,
//                Map.of("question", "我能否就此次徒步步道事故起诉队友？"),
//                "LEGAL",
//                List.of("你可提起过失侵权诉讼……", "结合产权归属与安全注意义务……"),
//                List.of(0.6, 0.85),
//                "结合产权归属及安全注意义务，你大概率有理由提起过失侵权诉讼。");

        // ----- Generate report -----

        Path output = Path.of("langchain4j-agentic", "src", "test", "resources", "sample-report.html");
        generateReport(monitor, output);
        System.out.println("Report written to " + output.toAbsolutePath());
    }

    // ---------- Execution simulation helpers ----------

    private static void simulateExecution(
            AgentMonitor monitor,
            MockScope scope,
            MockAgent sequence,
            MockAgent classify,
            MockAgent router,
            MockAgent expert,
            Map<String, Object> inputs,
            String category,
            String response)
            throws Exception {
        monitor.beforeAgentInvocation(new AgentRequest(scope, sequence, inputs)); // ask 入
        Thread.sleep(5);

        monitor.beforeAgentInvocation(new AgentRequest(scope, classify, inputs)); // 分类器入
        Thread.sleep(35);
        monitor.afterAgentInvocation(new AgentResponse(scope, classify, inputs, category)); // 分类器 出 选择

        monitor.beforeAgentInvocation(new AgentRequest(scope, router, Map.of("category", category))); // 选择出的 专家 结果
        Thread.sleep(3);

        monitor.beforeAgentInvocation(new AgentRequest(scope, expert, inputs)); // 专家 入
        Thread.sleep(55);
        monitor.afterAgentInvocation(new AgentResponse(scope, expert, inputs, response)); // 专家 出

        Thread.sleep(2);
        monitor.afterAgentInvocation(new AgentResponse(scope, router, Map.of("category", category), response)); // 路由器 出

        Thread.sleep(1);
        monitor.afterAgentInvocation(new AgentResponse(scope, sequence, inputs, response)); // 序列化器 出
    }

    private static void simulateLegalExecution(
            AgentMonitor monitor,
            MockScope scope,
            MockAgent sequence,
            MockAgent classify,
            MockAgent router,
            MockAgent loop,
            MockAgent writer,
            MockAgent scorer,
            Map<String, Object> inputs,
            String category,
            List<String> drafts,
            List<Double> scores,
            String finalResponse)
            throws Exception {
        monitor.beforeAgentInvocation(new AgentRequest(scope, sequence, inputs));
        Thread.sleep(5);

        monitor.beforeAgentInvocation(new AgentRequest(scope, classify, inputs));
        Thread.sleep(30);
        monitor.afterAgentInvocation(new AgentResponse(scope, classify, inputs, category));

        monitor.beforeAgentInvocation(new AgentRequest(scope, router, Map.of("category", category)));
        Thread.sleep(3);

        monitor.beforeAgentInvocation(new AgentRequest(scope, loop, inputs));
        Thread.sleep(2);

        // Loop iterations
        for (int i = 0; i < drafts.size(); i++) {
            monitor.beforeAgentInvocation(new AgentRequest(scope, writer, inputs));
            Thread.sleep(40);
            monitor.afterAgentInvocation(new AgentResponse(scope, writer, inputs, drafts.get(i)));

            monitor.beforeAgentInvocation(new AgentRequest(scope, scorer, Map.of("response", drafts.get(i))));
            Thread.sleep(20);
            monitor.afterAgentInvocation(
                    new AgentResponse(scope, scorer, Map.of("response", drafts.get(i)), scores.get(i)));
        }

        Thread.sleep(2);
        monitor.afterAgentInvocation(new AgentResponse(scope, loop, inputs, finalResponse));

        Thread.sleep(1);
        monitor.afterAgentInvocation(new AgentResponse(scope, router, Map.of("category", category), finalResponse));

        Thread.sleep(1);
        monitor.afterAgentInvocation(new AgentResponse(scope, sequence, inputs, finalResponse));
    }

    // ---------- Mock implementations ----------

    static class MockAgent implements AgentInstance {
        final String name;
        final Class<?> type;
        final AgenticSystemTopology topology;
        final String description;
        final List<AgentArgument> arguments;
        final String outputKey;
        final Type outputType;
        AgentInstance parent;
        List<AgentInstance> subagents = List.of();

        MockAgent(
                String name,
                Class<?> type,
                AgenticSystemTopology topology,
                String description,
                List<AgentArgument> arguments,
                String outputKey,
                Type outputType) {
            this.name = name;
            this.type = type;
            this.topology = topology;
            this.description = description;
            this.arguments = arguments;
            this.outputKey = outputKey;
            this.outputType = outputType;
        }

        @Override
        public Class<?> type() {
            return type;
        }

        @Override
        public Class<? extends Planner> plannerType() {
            return null;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String agentId() {
            return name;
        }

        @Override
        public String description() {
            return description;
        }

        @Override
        public Type outputType() {
            return outputType;
        }

        @Override
        public String outputKey() {
            return outputKey;
        }

        @Override
        public boolean async() {
            return false;
        }

        @Override
        public List<AgentArgument> arguments() {
            return arguments;
        }

        @Override
        public AgentInstance parent() {
            return parent;
        }

        @Override
        public List<AgentInstance> subagents() {
            return subagents;
        }

        @Override
        public AgenticSystemTopology topology() {
            return topology;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends AgentInstance> T as(Class<T> cls) {
            if (cls.isInstance(this)) return cls.cast(this);
            throw new ClassCastException("Cannot cast " + name + " to " + cls.getSimpleName());
        }
    }

    static class MockConditionalAgent extends MockAgent implements ConditionalAgentInstance {
        private final List<ConditionalAgent> conditionalSubagents;

        MockConditionalAgent(
                String name,
                String description,
                String outputKey,
                Type outputType,
                List<ConditionalAgent> conditionalSubagents) {
            super(name, null, AgenticSystemTopology.ROUTER, description, List.of(), outputKey, outputType);
            this.conditionalSubagents = conditionalSubagents;
            // Flatten all children
            List<AgentInstance> all = new ArrayList<>();
            for (ConditionalAgent ca : conditionalSubagents) {
                all.addAll(ca.agentInstances());
            }
            this.subagents = all;
        }

        @Override
        public List<ConditionalAgent> conditionalSubagents() {
            return conditionalSubagents;
        }
    }

    static class MockLoopAgent extends MockAgent implements LoopAgentInstance {
        private final int maxIterations;
        private final String exitCondition;
        private final boolean testExitAtLoopEnd;

        MockLoopAgent(
                String name,
                String description,
                List<AgentInstance> body,
                String outputKey,
                Type outputType,
                int maxIterations,
                String exitCondition,
                boolean testExitAtLoopEnd) {
            super(name, null, AgenticSystemTopology.LOOP, description, List.of(), outputKey, outputType);
            this.subagents = body;
            this.maxIterations = maxIterations;
            this.exitCondition = exitCondition;
            this.testExitAtLoopEnd = testExitAtLoopEnd;
        }

        @Override
        public int maxIterations() {
            return maxIterations;
        }

        @Override
        public boolean testExitAtLoopEnd() {
            return testExitAtLoopEnd;
        }

        @Override
        public String exitCondition() {
            return exitCondition;
        }
    }

    record MockScope(Object memoryId) implements AgenticScope {
        @Override
        public void writeState(String key, Object value) {}

        @Override
        public <T> void writeState(Class<? extends TypedKey<T>> key, T value) {}

        @Override
        public void writeStates(Map<String, Object> newState) {}

        @Override
        public boolean hasState(String key) {
            return false;
        }

        @Override
        public boolean hasState(Class<? extends TypedKey<?>> key) {
            return false;
        }

        @Override
        public Object readState(String key) {
            return null;
        }

        @Override
        public <T> T readState(String key, T dv) {
            return dv;
        }

        @Override
        public <T> T readState(Class<? extends TypedKey<T>> key) {
            return null;
        }

        @Override
        public Map<String, Object> state() {
            return Map.of();
        }

        @Override
        public String contextAsConversation(String... n) {
            return "";
        }

        @Override
        public String contextAsConversation(Object... a) {
            return "";
        }

        @Override
        public List<dev.langchain4j.agentic.scope.AgentInvocation> agentInvocations() {
            return List.of();
        }

        @Override
        public List<dev.langchain4j.agentic.scope.AgentInvocation> agentInvocations(String n) {
            return List.of();
        }

        @Override
        public List<dev.langchain4j.agentic.scope.AgentInvocation> agentInvocations(Class<?> t) {
            return List.of();
        }

        @Override
        public void writeExecutionContext(final String key, final Object context) {}

        @Override
        public Object executionContext(final String key) {
            return null;
        }

        @Override
        public <T> T executionContextAs(final String key, final Class<T> type) {
            return null;
        }
    }
}
