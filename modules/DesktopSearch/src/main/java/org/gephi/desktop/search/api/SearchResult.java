package org.gephi.desktop.search.api;

public interface SearchResult<T> {

    /**
     * Return the search result object.
     *
     * @return the search result object
     */
    T getResult();

    /**
     * Return the HTML display of the search result.
     *
     * @return the HTML display of the search result
     */
    String getHtmlDisplay();

    String getMatchLocation();
}
