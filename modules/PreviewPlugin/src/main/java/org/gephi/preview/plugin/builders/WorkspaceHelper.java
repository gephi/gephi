package org.gephi.preview.plugin.builders;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceProvider;
import org.openide.util.Lookup;

public class WorkspaceHelper {

    /**
     * Hack functions that allows to get the Workspace from a given graph.
     *
     * @param graph graph
     * @return workspace this graph belongs to
     */
    public static Workspace getWorkspace(Graph graph) {
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        if (projectController.getCurrentProject() == null) {
            return null;
        }
        WorkspaceProvider workspaceProvider =
            projectController.getCurrentProject().getLookup().lookup(WorkspaceProvider.class);
        for (Workspace workspace : workspaceProvider.getWorkspaces()) {
            GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
            if (graphModel == graph.getModel()) {
                return workspace;
            }
        }
        throw new RuntimeException("The workspace can't be found");
    }
}
