package org.gephi.project.io.utils;

import org.gephi.project.impl.ProjectControllerImpl;
import org.gephi.project.impl.ProjectImpl;
import org.gephi.project.impl.WorkspaceImpl;
import org.gephi.project.impl.WorkspaceProviderImpl;
import org.openide.util.Lookup;

public class Utils {

    public static final String PROJECT_NAME = "Project";

    public static ProjectImpl newProject() {
        return new ProjectImpl(PROJECT_NAME);
    }

    public static WorkspaceImpl newWorkspace() {
        ProjectImpl project = newProject();
        WorkspaceImpl workspace = project.newWorkspace();
        project.setCurrentWorkspace(project.newWorkspace());
        return workspace;
    }

    public static ProjectImpl getCurrentProject() {
        return Lookup.getDefault().lookup(ProjectControllerImpl.class).getCurrentProject();
    }

    public static WorkspaceImpl getCurrentWorkspace(ProjectImpl project) {
        return project.getLookup().lookup(WorkspaceProviderImpl.class).getCurrentWorkspace();
    }
}
