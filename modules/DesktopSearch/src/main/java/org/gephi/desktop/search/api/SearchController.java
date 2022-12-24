package org.gephi.desktop.search.api;

import java.util.List;

/**
 * Main entry point for search in the graph.
 * <p>
 * This controller is a singleton and can therefore be found in Lookup:
 * <pre>SearchController sc = Lookup.getDefault().lookup(SearchController.class);</pre>
 */
public interface SearchController {

    /**
     * Search using the provided request. The search can only return homogeneous result types so that's why the type has to be provided.
     * <p>
     * For instance, if the request is to search for nodes, the type has to be <code>Node.class</code>.
     *
     * @param request    the search request
     * @param typeFilter the type of the results
     * @param <T>        the type of the results
     * @return the search results
     */
    <T> List<SearchResult<T>> search(SearchRequest request, Class<T> typeFilter);

    /**
     * Asynchronous search using the provided request. When the search is completed it would call the provided listener.
     * <p>
     * Only one search can be running at a time. If a search is already running, it would be cancelled and its listener notified.
     *
     * @param request  the search request
     * @param listener the search listener
     */
    void search(SearchRequest request, SearchListener listener);
}
