package org.gephi.desktop.search.api;

import java.util.HashSet;
import java.util.Set;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.openide.util.Lookup;

public interface SearchRequest {

    String getQuery();

    Graph getGraph();

    Set<SearchCategory> getCategoryFilters();

    boolean inParallel();

    boolean isLimitResults();

    default boolean isCategoryIncluded(SearchCategory category) {
        Set<SearchCategory> categories = getCategoryFilters();
        return categories == null || categories.contains(category);
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder {

        private String query;
        private Graph graph;
        private boolean parallel = true;
        private boolean limitResults = true;

        private Set<SearchCategory> categories;

        private Builder() {
        }

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

        public Builder query(String query) {
            this.query = query;
            return this;
        }

        public Builder graph(Graph graph) {
            this.graph = graph;
            return this;
        }

        public Builder parallel(boolean parallel) {
            this.parallel = parallel;
            return this;
        }

        public Builder limitResults(boolean limitResults) {
            this.limitResults = limitResults;
            return this;
        }

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
