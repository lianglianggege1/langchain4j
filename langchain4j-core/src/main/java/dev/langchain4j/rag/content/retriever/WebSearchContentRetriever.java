package dev.langchain4j.rag.content.retriever;

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.WebSearchRequest;
import dev.langchain4j.web.search.WebSearchResults;

import java.util.List;

import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;
import static java.util.stream.Collectors.toList;

/**
 * A {@link ContentRetriever} that retrieves relevant {@link Content} from the web using a {@link WebSearchEngine}.
 * 一个{@link ContentRetriever}，它使用{@link WebSearchEngine}从网络上检索相关的{@link Content}。
 * <br>
 * It returns one {@link Content} for each result that a {@link WebSearchEngine} has returned for a given {@link Query}.
 * 对于给定的｛@link Query｝，{@link WebSearchEngine} 返回的每个结果，它都会返回一个{@link Content}。
 * <br>
 * Depending on the {@link WebSearchEngine} implementation, the {@link Content#textSegment()}
 * can contain either a snippet of a web page or a complete content of a web page.
 * 根据{@link WebSearchEngine}的实现，{@link Content#textSegment()}可以包含网页的一个片段或网页的完整内容。
 */
public class WebSearchContentRetriever implements ContentRetriever {

    private final WebSearchEngine webSearchEngine;
    private final int maxResults;

    public WebSearchContentRetriever(WebSearchEngine webSearchEngine, Integer maxResults) {
        this.webSearchEngine = ensureNotNull(webSearchEngine, "webSearchEngine");
        this.maxResults = getOrDefault(maxResults, 5);
    }

    public static WebSearchContentRetrieverBuilder builder() {
        return new WebSearchContentRetrieverBuilder();
    }

    @Override
    public List<Content> retrieve(Query query) {

        WebSearchRequest webSearchRequest = WebSearchRequest.builder()
                .searchTerms(query.text())
                .maxResults(maxResults)
                .build();

        WebSearchResults webSearchResults = webSearchEngine.search(webSearchRequest);

        return webSearchResults.toTextSegments().stream()
                .map(Content::from)
                .collect(toList());
    }

    public static class WebSearchContentRetrieverBuilder {
        private WebSearchEngine webSearchEngine;
        private Integer maxResults;

        WebSearchContentRetrieverBuilder() {
        }

        public WebSearchContentRetrieverBuilder webSearchEngine(WebSearchEngine webSearchEngine) {
            this.webSearchEngine = webSearchEngine;
            return this;
        }

        public WebSearchContentRetrieverBuilder maxResults(Integer maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        public WebSearchContentRetriever build() {
            return new WebSearchContentRetriever(this.webSearchEngine, this.maxResults);
        }
    }
}
