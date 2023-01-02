package org.gephi.desktop.search.spi;

/**
 * Used by {@link SearchProvider} to append search results to.
 *
 * @param <T> the type of the results
 */
public interface SearchResultsBuilder<T> {

    /**
     * Add a search result.
     * <p>
     * Return false if the search is obsolete or if the maximum number of results has been reached.
     *
     * @param result          the search result
     * @param htmlDisplayText the HTML display text
     * @param matchLocation   the match location
     * @return true if the result was added, false if the result was not added
     */
    boolean addResult(T result, String htmlDisplayText, String matchLocation);

    /**
     * Return true if the search has been already cancelled.
     *
     * @return true if the search has been already cancelled
     */
    boolean isObsolete();
}
