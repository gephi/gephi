package org.gephi.desktop.search.impl;

import java.util.ArrayList;
import java.util.List;
import org.gephi.desktop.search.spi.SearchProvider;
import org.gephi.desktop.search.spi.SearchResultsBuilder;

public class SearchResultsBuilderImpl<T> implements SearchResultsBuilder<T> {

    private boolean obsolete = false;
    private final int maxResults;
    private final List<SearchResultImpl<T>> resultsList;
    private final SearchProvider<T> provider;
    private int position;

    public SearchResultsBuilderImpl(SearchProvider<T> provider, int position, int maxResults) {
        this.provider = provider;
        this.position = position;
        this.maxResults = maxResults;
        this.resultsList = new ArrayList<>();
    }

    @Override
    public synchronized boolean addResult(T result, String htmlDisplayText, String matchLocation) {
        if (result == null) {
            throw new NullPointerException("Result cannot be null");
        }
        resultsList.add(new SearchResultImpl<>(provider, position, result, htmlDisplayText, matchLocation));
        return !obsolete && resultsList.size() < maxResults;
    }

    @Override
    public boolean isObsolete() {
        return obsolete;
    }

    protected List<SearchResultImpl<T>> getResults() {
        return resultsList;
    }

    protected void markObsolete() {
        this.obsolete = true;
    }
}
