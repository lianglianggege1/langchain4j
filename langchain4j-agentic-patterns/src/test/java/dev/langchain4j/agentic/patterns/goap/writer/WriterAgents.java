package dev.langchain4j.agentic.patterns.goap.writer;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.scope.ResultWithAgenticScope;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public class WriterAgents {

    public interface StoryGenerator {

        /*@UserMessage("""
                You are a creative writer.
                Generate a draft of a story long no more than 3 sentence around the given topic.
                Return only the story and nothing else.
                The topic is {{topic}}.
                """)
        @Agent("Generate a story based on the given topic")
        String generateStory(@V("topic") String topic);*/

        @UserMessage("""
        你是创意写作者。
        围绕给定主题创作一篇故事草稿，不超过3句话。
        只返回故事内容，不要输出其他任何内容。
        主题：{{topic}}。
        """)
        @Agent("根据给定主题生成故事")
        String generateStory(@V("topic") String topic);

    }

    public interface AudienceEditor {

        /*@UserMessage("""
            You are a professional editor.
            Analyze and rewrite the following story to better align with the target audience of {{audience}}.
            Return only the story and nothing else.
            The story is "{{styledStory}}".
            """)
        @Agent("Edit a story to better fit a given audience")
        String editStoryForAudience(@V("styledStory") String styledStory, @V("audience") String audience);*/

        @UserMessage("""
        你是专业编辑。
        分析并重写下述故事，使其更贴合目标受众{{audience}}。
        只返回改写后的故事，不要输出其他任何内容。
        故事内容："{{styledStory}}"。
        """)
        @Agent("改写故事以适配指定受众")
        String editStoryForAudience(@V("styledStory") String styledStory, @V("audience") String audience);


    }

    public interface StyleEditor {

        /*@UserMessage("""
                You are a professional editor.
                Analyze and rewrite the following story to better fit and be more coherent with the {{style}} style.
                Return only the story and nothing else.
                The story is "{{story}}".
                """)
        @Agent("Edit a story to better fit a given style")
        String editStoryForStyle(@V("story") String story, @V("style") String style);*/

        @UserMessage("""
        你是专业编辑。
        分析并重写下述故事，使其更加贴合{{style}}文风，行文逻辑更连贯。
        只返回改写后的故事，不要输出其他任何内容。
        故事内容："{{story}}"。
        """)
        @Agent("改写故事适配指定文风")
        String editStoryForStyle(@V("story") String story, @V("style") String style);



    }

    public interface StyleScorer {

        /*@UserMessage("""
                You are a critical reviewer.
                Give a review score between 0.0 and 1.0 for the following story based on how well it aligns with the style '{{style}}'.
                Return only the score and nothing else.
                
                The story is: "{{styledStory}}"
                """)
        @Agent("Score a story based on how well it aligns with a given style")
        double scoreStyle(@V("styledStory") String styledStory, @V("style") String style);*/


        @UserMessage("""
        你是评审评论家。
        根据故事与文风「{{style}}」的契合程度，给出0.0‑1.0之间的评分。
        仅返回分数，不要输出其他任何内容。
        故事内容："{{styledStory}}"
        """)
        @Agent("依据指定文风对故事打分")
        double scoreStyle(@V("styledStory") String styledStory, @V("style") String style);

    }

    public interface StyleReviewLoop {

        /*@Agent("Review the given story to ensure it aligns with the specified style")
        String reviewStyleAndScore(@V("story") String story, @V("style") String style);*/

        @Agent("校验故事是否匹配指定文风并给出评审打分")
        String reviewStyleAndScore(@V("story") String story, @V("style") String style);

    }

    public interface Writer {

        @Agent
        ResultWithAgenticScope<String> write(@V("topic") String topic, @V("style") String style, @V("audience") String audience);
    }
}
