package org.gephi.desktop.search.impl;

import org.gephi.desktop.search.api.SearchResult;
import org.gephi.desktop.search.spi.SearchProvider;

public class SearchResultImpl<T> implements SearchResult<T> {

    private final T result;
    private final SearchProvider<T> provider;
    private final int position;
    private final String htmlDisplay;

    public SearchResultImpl(SearchProvider<T> provider, int position, T result, String htmlDisplay) {
        this.provider = provider;
        this.position = position;
        this.result = result;
        this.htmlDisplay = htmlDisplay;
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

    public int getPosition() {
        return position;
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
}
