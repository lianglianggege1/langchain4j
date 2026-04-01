package dev.langchain4j.skills;

import dev.langchain4j.Experimental;
import dev.langchain4j.service.tool.ToolProvider;

import java.util.List;

/**
 * Represents a skill that can be used by an LLM.
 * 代表LLM可以使用的一项技能
 * <p>
 * A skill has a mandatory {@link #name()} and {@link #description()} that the LLM always sees.
 * The LLM can read the full {@link #content()} and any {@link #resources()} on demand.
 * 技能必须包含名称（name）和描述{@link #description()}，
 * LLM可以按需读取完整内容（content）和任何资源（resources()）。
 * <p>
 * See more details <a href="https://agentskills.io">here</a>.
 */
@Experimental
public interface Skill {

    /**
     * Returns the unique name of this skill.
     * The LLM uses this name to identify the skill when selecting from the available skills.
     * LLM 使用此名称在从可用技能中进行选择时识别该技能。
     */
    String name();

    /**
     * Returns a short description of what this skill does.
     * Shown to the LLM so it can decide which skill is relevant for the current request.
     * 向LLM显示，以便其决定哪项技能与当前请求相关。
     */
    String description();

    /**
     * Returns the full instructions of this skill (e.g. the contents of a {@code SKILL.md} file).
     * 返回此技能的完整说明（例如，{@code SKILL.md} 文件的内容）。
     */
    String content();

    /**
     * Returns the optional list of additional resources associated with this skill
     * (e.g. references, assets, templates, etc.).
     * 返回与此技能相关的其他资源的可选列表（例如参考资料、资产、模板等）。
     *
     * @return the list of resources, empty by default
     */
    default List<SkillResource> resources() {
        return List.of();
    }

    /**
     * Returns the optional list of tool providers associated with this skill.
     * These tool providers supply tools that will be exposed to the LLM when this skill is activated.
     * 返回与此技能关联的工具提供者列表（可选）。
     * 这些工具提供者提供的工具将在该技能激活时对LLM可见。
     *
     * @return the list of tool providers, empty by default
     */
    default List<ToolProvider> toolProviders() {
        return List.of();
    }

    static DefaultSkill.Builder builder() {
        return new DefaultSkill.Builder();
    }
}
