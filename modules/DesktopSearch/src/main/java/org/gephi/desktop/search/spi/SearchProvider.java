package org.gephi.desktop.search.spi;

import org.gephi.desktop.search.api.SearchRequest;

/**
 * Search providers are responsible for searching in the graph and append the results to the search results builder.
 * <p>
 * It's expected that multiple search providers are called in parallel and search differently. For instance, one provider
 * could search for nodes based on labels and another one could search for edges based on identifiers.
 * <p>
 * A search provider can only return homogeneous results. For instance, it cannot mix nodes and edges in the same search.
 * Two different search providers should be used for that.
 *
 * @param <T> the type of the results
 */
public interface SearchProvider<T> {

    /**
     * Execute a search. The search results need to be added to the search results builder.
     *
     * @param request        the search request
     * @param resultsBuilder the search results builder to append the results to
     */
    void search(SearchRequest request, SearchResultsBuilder<T> resultsBuilder);
}
