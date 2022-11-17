package org.gephi.desktop.search.api;

import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

public interface SearchRequest {

    String getQuery();

    Workspace workspace();

    static Builder builder() {
        return new Builder();
    }

    class Builder {

        private String query;
        private Workspace workspace;

        private Builder() {
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
                public Workspace workspace() {
                    return workspace;
                }
            };
        }
    }
}
