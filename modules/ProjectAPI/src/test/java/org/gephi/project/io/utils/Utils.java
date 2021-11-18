package org.gephi.project.io.utils;

import org.gephi.project.impl.ProjectImpl;
import org.gephi.workspace.impl.WorkspaceImpl;

public class Utils {

    public static final String PROJECT_NAME = "Project";
    public static final String WORKSPACE_NAME = "Workspace";

    public static ProjectImpl newProject() {
        return new ProjectImpl(PROJECT_NAME);
    }

    public static WorkspaceImpl newWorkspace() {
        return new WorkspaceImpl(newProject(), 0, WORKSPACE_NAME);
    }


}
