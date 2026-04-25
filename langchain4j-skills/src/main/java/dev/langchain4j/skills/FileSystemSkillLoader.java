package dev.langchain4j.skills;

import static dev.langchain4j.internal.Exceptions.unchecked;
import static dev.langchain4j.internal.Utils.isNullOrBlank;
import static java.util.stream.Collectors.joining;
import static java.util.stream.StreamSupport.stream;

import dev.langchain4j.Experimental;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads skills from the file system.
 * 从文件系统中加载技能
 * <p>
 * Each skill must reside in its own directory containing a {@code SKILL.md} file.
 * The file must have a YAML front matter block that declares the skill's {@code name}
 * and {@code description}. The body of the file (below the front matter) becomes the
 * skill's {@link Skill#content() content}.
 * 每个技能都必须位于单独的目录中，该目录下包含一个 SKILL.md 文件。
 * 该文件必须包含一个 YAML 前置元数据块，用于声明技能的名称和描述。
 * 文件正文（前置元数据块下方）即为技能的内容。
 * <p>
 * Example {@code SKILL.md} structure:
 * <pre>{@code
 * ---
 * name: my-skill
 * description: Does something useful
 * ---
 *
 * Detailed instructions for the LLM go here.
 * }</pre>
 * <p>
 * Any additional files in the skill directory are loaded as {@link SkillResource}s,
 * except {@code SKILL.md} itself and any files under a {@code scripts/} subdirectory.
 * Empty files are silently skipped.
 * 技能目录中的所有其他文件都会作为技能资源加载，
 * 但 SKILL.md 文件本身以及 scripts/ 子目录下的任何文件除外。空文件将被静默跳过。
 */
@Experimental
public class FileSystemSkillLoader {

    private static final Logger log = LoggerFactory.getLogger(FileSystemSkillLoader.class);

    /**
     * Loads all skills found in immediate subdirectories of the given directory.
     * A subdirectory is treated as a skill only if it contains a {@code SKILL.md} file;
     * subdirectories without one are silently skipped.
     *
     * @param directory the directory whose immediate subdirectories are scanned for skills
     * @return the list of loaded skills, in filesystem iteration order
     * @throws RuntimeException if the directory cannot be listed or a skill fails to load
     */
    public static List<FileSystemSkill> loadSkills(Path directory) {
        try (Stream<Path> entries = Files.list(directory)) {
            return entries.filter(Files::isDirectory)
                    .filter(dir -> Files.exists(dir.resolve("SKILL.md")))
                    .map(FileSystemSkillLoader::loadSkill)
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load skills from " + directory, e);
        }
    }

    /**
     * Loads a single skill from the given directory.
     * 从给定的目录中加载技能
     * <p>
     * The directory must contain a {@code SKILL.md} file with a YAML front matter block
     * declaring the skill's {@code name} and {@code description}.
     * 该目录必须包含一个 SKILL.md 文件，其中包含一个 YAML 前置元数据块，用于声明技能的名称和描述。
     *
     * @param skillDirectory the directory to load the skill from
     * @return the loaded skill
     * @throws IllegalArgumentException if {@code SKILL.md} is not found in the directory
     * @throws RuntimeException         if the file cannot be read or resources cannot be loaded
     */
    public static FileSystemSkill loadSkill(Path skillDirectory) {
        Path skillFile = skillDirectory.resolve("SKILL.md");

        if (!Files.exists(skillFile)) {
            throw new IllegalArgumentException("SKILL.md not found in " + skillDirectory);
        }

        String markdown = unchecked(() -> Files.readString(skillFile));

        Map<String, List<String>> frontMatter = SkillLoaderCommon.parseFrontMatter(markdown);
        String content = SkillLoaderCommon.extractContent(markdown);

        String name = SkillLoaderCommon.getSingle(frontMatter, "name");
        String description = SkillLoaderCommon.getSingle(frontMatter, "description");

        List<DefaultSkillResource> resources = loadResources(skillDirectory);

        return DefaultFileSystemSkill.builder()
                .name(name)
                .description(description)
                .content(content)
                .resources(resources)
                .basePath(skillDirectory)
                .build();
    }

    private static List<DefaultSkillResource> loadResources(Path skillDirectory) {
        try (Stream<Path> files = Files.walk(skillDirectory)) {
            return files.filter(Files::isRegularFile)
                    .filter(path -> !path.getFileName().toString().equals("SKILL.md"))
                    .filter(path -> !skillDirectory.relativize(path).startsWith("scripts"))
                    .map(path -> {
                        try {
                            String content = Files.readString(path);
                            if (isNullOrBlank(content)) {
                                return null;
                            }
                            String relativePath = stream(
                                            skillDirectory.relativize(path).spliterator(), false)
                                    .map(Path::toString)
                                    .collect(joining("/"));
                            return SkillResource.builder()
                                    .relativePath(relativePath)
                                    .content(content)
                                    .build();
                        } catch (MalformedInputException e) {
                            log.warn("Skipping binary file that cannot be read as UTF-8 text: {}", path);
                            return null;
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to load skill resource from " + path, e);
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load skill resources from " + skillDirectory, e);
        }
    }
}
