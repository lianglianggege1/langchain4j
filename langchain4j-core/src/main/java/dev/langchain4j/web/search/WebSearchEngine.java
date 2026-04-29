package dev.langchain4j.web.search;

/**
 * Represents a web search engine that can be used to perform searches on the Web in response to a user query.
 * 表示一个网页搜索引擎，用于根据用户查询在Web上执行搜索。
 */
public interface WebSearchEngine {

    /**
     * Performs a search query on the web search engine and returns the search results.
     * 在网络搜索引擎上执行搜索查询并返回搜索结果。
     *
     * @param query the search query
     * @return the search results
     */
    default WebSearchResults search(String query) {
        return search(WebSearchRequest.from(query));
    }

    /**
     * Performs a search request on the web search engine and returns the search results.
     * 在网络搜索引擎上执行搜索请求并返回搜索结果。
     * @param webSearchRequest the search request
     * @return the web search results
     */
    WebSearchResults search(WebSearchRequest webSearchRequest);
}
