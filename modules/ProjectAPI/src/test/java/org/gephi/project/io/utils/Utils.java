package org.gephi.project.io.utils;

import org.gephi.project.impl.ProjectControllerImpl;
import org.gephi.project.impl.ProjectImpl;
import org.gephi.project.impl.WorkspaceProviderImpl;
import org.gephi.workspace.impl.WorkspaceImpl;
import org.openide.util.Lookup;

public class Utils {

    public static final String PROJECT_NAME = "Project";

    public static ProjectImpl newProject() {
        return new ProjectImpl(PROJECT_NAME);
    }

    public static WorkspaceImpl newWorkspace() {
        ProjectImpl project = newProject();
        WorkspaceProviderImpl provider = project.getLookup().lookup(WorkspaceProviderImpl.class);
        WorkspaceImpl workspace = provider.newWorkspace();
        provider.setCurrentWorkspace(workspace);
        return workspace;
    }

    public static ProjectImpl getCurrentProject() {
        return Lookup.getDefault().lookup(ProjectControllerImpl.class).getCurrentProject();
    }

    public static WorkspaceImpl getCurrentWorkspace(ProjectImpl project) {
        return project.getLookup().lookup(WorkspaceProviderImpl.class).getCurrentWorkspace();
    }
}
