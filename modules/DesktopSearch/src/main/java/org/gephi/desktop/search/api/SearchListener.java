package org.gephi.desktop.search.api;

import java.util.Collection;

public interface SearchListener {

    void started(SearchRequest request);

    void cancelled();

    void finished(SearchRequest request, Collection<SearchResult> results);
}
