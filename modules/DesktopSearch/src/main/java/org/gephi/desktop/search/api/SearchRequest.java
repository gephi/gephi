package org.gephi.desktop.search.api;

import java.util.HashSet;
import java.util.Set;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.openide.util.Lookup;

/**
 * Encapsulate a search request.
 * <p>
 * To build a search query, call the <pre>SearchRequest.builder()</pre> to get a builder.
 */
public interface SearchRequest {

    /**
     * Return the search's query.
     *
     * @return the search's query
     */
    String getQuery();

    /**
     * Return the graph to search in.
     *
     * @return the graph to search in
     */
    Graph getGraph();

    /**
     * Return the categories to search in. If empty, all categories are searched.
     * <p>
     * The returned set is immutable.
     *
     * @return the categories to search in
     */
    Set<SearchCategory> getCategoryFilters();

    /**
     * Return true if the search is executed using multiple threads.
     *
     * @return true if parallel search, false otherwise
     */
    boolean inParallel();

    /**
     * Return true if the search has a limit on the number of results.
     *
     * @return true if results are limited, false otherwise
     */
    boolean isLimitResults();

    /**
     * Return true if the provided category is in the category filters. If not categories are set, it returns true.
     *
     * @param category the category to check
     * @return true if the category is in the category filters, false otherwise
     */
    default boolean isCategoryIncluded(SearchCategory category) {
        Set<SearchCategory> categories = getCategoryFilters();
        return categories == null || categories.contains(category);
    }

    /**
     * Return a search request builder.
     *
     * @return a search request builder
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for a search request.
     * <p>
     * When configured, call the <code>build()</code> method to get the search request.
     */
    class Builder {

        private String query;
        private Graph graph;
        private boolean parallel = true;
        private boolean limitResults = true;

        private Set<SearchCategory> categories;

        private Builder() {
        }

        /**
         * Adds the provided category to the category filters.
         *
         * @param category the category to add
         * @return this builder
         */
        public Builder category(SearchCategory category) {
            if (category == null) {
                return this;
            }
            if (categories == null) {
                categories = new HashSet<>();
            }
            categories.add(category);
            return this;
        }

        /**
         * Sets the query.
         *
         * @param query the query
         * @return this builder
         */
        public Builder query(String query) {
            this.query = query;
            return this;
        }

        /**
         * Sets the graph. If  no graph is provided, it uses the graph from the current workspace.
         *
         * @param graph the graph
         * @return this builder
         */
        public Builder graph(Graph graph) {
            this.graph = graph;
            return this;
        }

        /**
         * Sets the parallel flag. It controls whether the search is executed using multiple threads.
         *
         * @param parallel true if parallel search, false otherwise
         * @return this builder
         */
        public Builder parallel(boolean parallel) {
            this.parallel = parallel;
            return this;
        }

        /**
         * Sets the limit results flag. It controls whether the search has a limit on the number of results.
         *
         * @param limitResults true if results are limited, false otherwise
         * @return this builder
         */
        public Builder limitResults(boolean limitResults) {
            this.limitResults = limitResults;
            return this;
        }

        /**
         * Builds the search request.
         *
         * @return the search request
         */
        public SearchRequest build() {
            if (query == null || query.trim().isEmpty()) {
                throw new IllegalArgumentException("Query cannot be null or empty");
            }
            if (graph == null) {
                GraphController gc = Lookup.getDefault().lookup(GraphController.class);
                if (gc.getGraphModel() == null) {
                    throw new IllegalStateException("Workspace cannot be null if there is no current project");
                }
                graph = gc.getGraphModel().getGraph();
            }
            return new SearchRequest() {
                @Override
                public String getQuery() {
                    return query;
                }

                @Override
                public Graph getGraph() {
                    return graph;
                }

                @Override
                public Set<SearchCategory> getCategoryFilters() {
                    return categories;
                }

                @Override
                public boolean inParallel() {
                    return parallel;
                }

                @Override
                public boolean isLimitResults() {
                    return limitResults;
                }
            };
        }
    }
}
