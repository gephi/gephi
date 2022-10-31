package org.gephi.project.spi;

import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.impl.ProjectControllerImpl;
import org.gephi.project.impl.WorkspaceImpl;
import org.openide.util.Lookup;

public interface Controller<T extends Model> {

    T newModel(Workspace workspace);

    Class<T> getModelClass();

    default T getModel(Workspace workspace) {
        return workspace.getLookup().lookup(getModelClass());
    }

    default T getModel() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        Workspace workspace = pc.getCurrentWorkspace();
        if (workspace != null) {
            return getModel(workspace);
        }
        return null;
    }
}
