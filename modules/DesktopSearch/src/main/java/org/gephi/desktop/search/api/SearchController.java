package org.gephi.desktop.search.api;

import java.util.List;

public interface SearchController {

    <T> List<SearchResult<T>> search(SearchRequest request, Class<T> typeFilter);

    void search(SearchRequest request, SearchListener listener);

}
