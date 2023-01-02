package org.gephi.desktop.search.api;

import java.util.List;

/**
 * A listener for search results.
 */
public interface SearchListener {

    /**
     * Called when the search is started.
     *
     * @param request the search request
     */
    void started(SearchRequest request);

    /**
     * Called when a search is cancelled. This usually happens when a new search has started afterwards.
     */
    void cancelled();

    /**
     * Called when a search is finished.
     *
     * @param request the search request
     * @param results the search results
     */
    void finished(SearchRequest request, List<SearchResult> results);
}
