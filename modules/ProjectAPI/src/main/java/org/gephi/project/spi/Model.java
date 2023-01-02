package org.gephi.project.spi;

import org.gephi.project.api.Workspace;

/**
 * Model interface that can be used in combination with {@link Controller} to store data in workspaces. This model
 * should work as a one-to-one relationship with the workspace so that a unique instance of this model is created for
 * each workspace during its lifetime.
 *
 * @see Controller
 */
public interface Model {

    /**
     * Returns the workspace this model is associated with.
     *
     * @return the workspace
     */
    Workspace getWorkspace();
}
