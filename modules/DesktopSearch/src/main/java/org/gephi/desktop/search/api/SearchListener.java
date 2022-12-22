package org.gephi.desktop.search.api;

import java.util.List;

public interface SearchListener {

    void started(SearchRequest request);

    void cancelled();

    void finished(SearchRequest request, List<SearchResult> results);
}
