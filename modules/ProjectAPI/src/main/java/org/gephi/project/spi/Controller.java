package org.gephi.project.spi;

import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.impl.ProjectControllerImpl;
import org.gephi.project.impl.WorkspaceImpl;
import org.openide.util.Lookup;

/**
 * Singleton controllers that want to store data in workspaces can implement this interface.
 * <p>
 * When a new workspace is created, the project controller will be requesting from this singleton a new model via
 * {@link #newModel(Workspace)} to be put in the workspace's lookup.
 * <p>
 * Implementations should register themselves in the default lookup via the {@link org.openide.util.lookup.ServiceProvider} annotation.
 *
 * @param <T> the model class this controller handles
 * @see Model
 */
public interface Controller<T extends Model> {

    /**
     * Creates a new model instance for the given workspace.
     * <p>
     * The model is not added to the workspace, it is the responsibility of the caller to do so.
     *
     * @param workspace the workspace to create the model for
     * @return new instance of a model
     */
    T newModel(Workspace workspace);

    /**
     * Returns the model class this controller handles.
     *
     * @return the model class
     */
    Class<T> getModelClass();

    /**
     * Returns the model of the given workspace. If the model is not found, it returns <code>null</code>.
     * <p>
     * This method is just a wrapper to <code>workspace.getLookup().lookup(getModelClass())</code>.
     *
     * @param workspace workspace to retrieve the model from
     * @return the model of the given workspace or <code>null</code> if not found
     */
    default T getModel(Workspace workspace) {
        return workspace.getLookup().lookup(getModelClass());
    }

    /**
     * Returns the model of the current workspace, or <code>null</code> if no workspace is selected.
     *
     * @return model associated with the current workspace if it exists, or <code>null</code> otherwise
     */
    default T getModel() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        Workspace workspace = pc.getCurrentWorkspace();
        if (workspace != null) {
            return getModel(workspace);
        }
        return null;
    }
}
