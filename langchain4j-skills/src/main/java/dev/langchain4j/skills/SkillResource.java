package dev.langchain4j.skills;

import dev.langchain4j.Experimental;

/**
 * An additional resource associated with a {@link Skill},
 * such as a reference file, asset, or template that the LLM can read on demand.
 * 与技能相关的其他资源，例如 LLM 可以按需读取的参考文件、资产或模板。
 */
@Experimental
public interface SkillResource {

    /**
     * Returns the relative path of this resource within the skill's directory.
     * Used to identify the resource when the LLM requests it.
     * 返回此资源在技能目录中的相对路径。用于在LLM请求资源时识别该资源。
     */
    String relativePath();

    /**
     * Returns the content of this resource.
     * 返回资源的内容。
     */
    String content();

    static DefaultSkillResource.Builder builder() {
        return new DefaultSkillResource.Builder();
    }
}
