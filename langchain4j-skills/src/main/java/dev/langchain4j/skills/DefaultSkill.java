package dev.langchain4j.skills;

import dev.langchain4j.Experimental;

@Experimental
public class DefaultSkill extends AbstractSkill {

    public DefaultSkill(Builder builder) {
        super(builder);
    }

    /**
     * Returns a new builder pre-populated with the values from this skill.
     * 返回一个预先填充了此技能值的新建造者。
     * Useful for creating a modified copy, e.g. adding tools to a filesystem-loaded skill:
     * 可用于创建修改后的副本，例如向文件系统加载的技能添加工具：
     * <pre>{@code
     * Skill skillWithTools = skill.toBuilder().tools(new MyTools()).build();
     * }</pre>
     */
    public Builder toBuilder() {
        Builder builder = new Builder();
        builder.name(name())
                .description(description())
                .content(content())
                .resources(resources())
                .toolProviders(toolProviders());
        return builder;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractSkill.BaseBuilder<Builder> {

        public DefaultSkill build() {
            return new DefaultSkill(this);
        }
    }
}
