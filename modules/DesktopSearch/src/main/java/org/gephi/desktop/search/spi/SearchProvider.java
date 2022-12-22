package org.gephi.desktop.search.spi;

import org.gephi.desktop.search.api.SearchRequest;


public interface SearchProvider<T> {

    void search(SearchRequest request, SearchResultsBuilder<T> resultsBuilder);
}
