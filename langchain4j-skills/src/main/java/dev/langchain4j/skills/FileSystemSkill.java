package dev.langchain4j.skills;

import dev.langchain4j.Experimental;

import java.nio.file.Path;

/**
 * A {@link Skill} backed by the file system.
 * 一个由文件系统支持的{@link Skill}
 * <p>
 * Extends {@link Skill} with a {@link #basePath()} pointing to the directory
 * that contains the skill's {@code SKILL.md} and any associated resource files.
 * 使用 basePath() 扩展技能，该 basePath() 指向包含技能的 SKILL.md 和任何相关资源文件的目录。
 */
@Experimental
public interface FileSystemSkill extends Skill {

    /**
     * Returns the base directory of this skill on the file system.
     * This directory is expected to contain a {@code SKILL.md} file
     * and optionally additional resource files.
     * 返回该技能在文件系统中的根目录。
     * 该目录应包含一个 SKILL.md 文件，并可选择性地包含其他资源文件。
     */
    Path basePath();

    static DefaultFileSystemSkill.Builder builder() {
        return new DefaultFileSystemSkill.Builder();
    }
}
