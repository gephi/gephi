package org.gephi.desktop.search.api;

import java.util.HashSet;
import java.util.Set;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

public interface SearchRequest {

    String getQuery();

    Workspace getWorkspace();

    Set<SearchCategory> getCategoryFilters();

    default boolean isCategoryIncluded(SearchCategory category) {
        Set<SearchCategory> categories = getCategoryFilters();
        return categories == null || categories.contains(category);
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder {

        private String query;
        private Workspace workspace;

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

        public Builder workspace(Workspace workspace) {
            this.workspace = workspace;
            return this;
        }

        public SearchRequest build() {
            if (query == null || query.trim().isEmpty()) {
                throw new IllegalArgumentException("Query cannot be null or empty");
            }
            if (workspace == null) {
                ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
                if (pc.getCurrentProject() == null) {
                    throw new IllegalStateException("Workspace cannot be null if there is no current project");
                }
                workspace = pc.getCurrentWorkspace();
            }
            return new SearchRequest() {
                @Override
                public String getQuery() {
                    return query;
                }

                @Override
                public Workspace getWorkspace() {
                    return workspace;
                }

                @Override
                public Set<SearchCategory> getCategoryFilters() {
                    return categories;
                }
            };
        }
    }
}
