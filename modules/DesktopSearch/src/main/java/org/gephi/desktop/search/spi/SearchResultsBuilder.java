package org.gephi.desktop.search.spi;

public interface SearchResultsBuilder<T> {

    boolean addResult(T result, String htmlDisplayText);

    boolean isObsolete();
}
