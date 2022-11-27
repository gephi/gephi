package org.gephi.desktop.search.api;

public interface SearchResult<T> {

    T getResult();

    String getHtmlDisplay();

    String getMatchLocation();
}
