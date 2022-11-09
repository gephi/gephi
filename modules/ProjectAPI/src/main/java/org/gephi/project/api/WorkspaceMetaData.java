package org.gephi.project.api;

/**
 * Hosts user data about a workspace.
 * <p>
 * This information is also saved to the project file.
 *
 * @author Mathieu Bastian
 */
public interface WorkspaceMetaData {

    /**
     * Returns the description of this workspace.
     *
     * @return the workspace's description or empty string if missing
     */
    String getDescription();

    /**
     * Sets the workspace's description.
     *
     * @param description description
     */
    void setDescription(String description);

    /**
     * Returns the title of this workspace.
     *
     * @return the workspace's title or empty string if missing
     */
    String getTitle();

    /**
     * Sets the workspace's title.
     *
     * @param title title
     */
    void setTitle(String title);
}
