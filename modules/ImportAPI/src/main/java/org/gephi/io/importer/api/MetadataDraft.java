package org.gephi.io.importer.api;

import org.gephi.project.api.Workspace;

/**
 * Draft metadata, hosted by import containers to represent graph metadata.
 * <code>Processors</code> will process this metadata and set it to the workspace's metadata.
 * <p>
 * Use the provided builder to create a new instance: <pre>MetadataDraft.builder().setDescription("desc").setTitle("title").build()</pre>
 *
 * @author Mathieu Bastian
 * @see ContainerLoader
 * @see Workspace#getWorkspaceMetadata()
 */
public interface MetadataDraft {

    /**
     * Create a new builder.
     *
     * @return new builder
     */
    static Builder builder() {
        return new Builder();
    }

    class Builder {

        private String title;
        private String description;

        private Builder() {
        }

        /**
         * Builds a new instance of <code>MetadataDraft</code> with the provided title and description.
         *
         * @return new instance of <code>MetadataDraft</code>
         */
        public MetadataDraft build() {
            return new MetadataDraft() {
                @Override
                public String getTitle() {
                    return title;
                }

                @Override
                public String getDescription() {
                    return description;
                }

                @Override
                public boolean equals(Object o) {
                    if (this == o) {
                        return true;
                    }
                    if (o == null || getClass() != o.getClass()) {
                        return false;
                    }

                    MetadataDraft that = (MetadataDraft) o;

                    if (getTitle() != null ? !getTitle().equals(that.getTitle()) : that.getTitle() != null) {
                        return false;
                    }
                    return getDescription() != null ? getDescription().equals(that.getDescription()) :
                        that.getDescription() == null;
                }

                @Override
                public int hashCode() {
                    int result = getTitle() != null ? getTitle().hashCode() : 0;
                    result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
                    return result;
                }
            };
        }

        /**
         * Sets the graph title.
         *
         * @param title graph title
         * @return this builder
         */
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        /**
         * Sets the graph description.
         *
         * @param description graph description
         * @return this builder
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }
    }

    /**
     * Returns the graph title.
     *
     * @return graph title or <code>null</code> if not set
     */
    String getTitle();

    /**
     * Returns the graph description.
     *
     * @return graph description or <code>null</code> if not set
     */
    String getDescription();
}
