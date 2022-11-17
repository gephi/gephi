package org.gephi.desktop.search.api;

import java.util.Collection;

public interface SearchController {

    <T> Collection<SearchResult<T>> search(SearchRequest request, Class<T> typeFilter);

    void search(SearchRequest request, SearchListener listener);

}
