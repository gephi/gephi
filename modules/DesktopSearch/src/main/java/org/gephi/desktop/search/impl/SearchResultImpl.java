package org.gephi.desktop.search.impl;

import org.gephi.desktop.search.api.SearchResult;
import org.gephi.desktop.search.spi.SearchProvider;

public class SearchResultImpl<T> implements SearchResult<T>, Comparable<SearchResultImpl<T>> {

    private final T result;
    private final SearchProvider<T> provider;
    private final int position;
    private final String htmlDisplay;

    private final String matchLocation;

    public SearchResultImpl(SearchProvider<T> provider, int position, T result, String htmlDisplay,
                            String matchLocation) {
        this.provider = provider;
        this.position = position;
        this.result = result;
        this.htmlDisplay = htmlDisplay;
        this.matchLocation = matchLocation;
    }

    @Override
    public T getResult() {
        return result;
    }

    protected SearchProvider<T> getProvider() {
        return provider;
    }

    @Override
    public String getHtmlDisplay() {
        return htmlDisplay;
    }

    @Override
    public String getMatchLocation() {
        return matchLocation;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "<html>" + getHtmlDisplay() + " <font color='#aaaaaa'>" + getMatchLocation() + "</font></html>";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SearchResultImpl<?> that = (SearchResultImpl<?>) o;

        return result.equals(that.result);
    }

    @Override
    public int hashCode() {
        return result.hashCode();
    }

    @Override
    public int compareTo(SearchResultImpl<T> o) {
        return Integer.compare(position, o.position);
    }
}
