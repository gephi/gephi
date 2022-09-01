package org.gephi.project.io;

import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectListener;
import org.gephi.project.api.Projects;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceProvider;
import org.gephi.project.impl.ProjectControllerImpl;
import org.gephi.project.impl.ProjectImpl;
import org.gephi.project.impl.ProjectInformationImpl;
import org.gephi.project.impl.ProjectTask;
import org.gephi.project.impl.ProjectsImpl;
import org.gephi.project.impl.WorkspaceProviderImpl;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;

public class CloseTask  implements ProjectTask {

    private final ProjectImpl project;

    public CloseTask(ProjectImpl project) {
        this.project = project;
    }

    @Override
    public void onStart(ProjectListener projectListener) {

    }

    @Override
    public void onSuccess(ProjectListener projectListener) {
        projectListener.closed(project);
    }

    @Override
    public void onError(ProjectListener projectListener, Throwable throwable) {
        //TODO error
    }

    @Override
    public void run() {
        ProjectControllerImpl pc = Lookup.getDefault().lookup(ProjectControllerImpl.class);
        ProjectsImpl projects = pc.getProjects();

        //Event
        if (project.hasCurrentWorkspace()) {
            pc.fireWorkspaceEvent(ProjectControllerImpl.EventType.UNSELECT,
                project.getCurrentWorkspace());
        }
        for (Workspace ws : project.getWorkspaces()) {
            pc.fireWorkspaceEvent(ProjectControllerImpl.EventType.CLOSE, ws);
        }

        //Close
        project.getLookup().lookup(ProjectInformationImpl.class).close();
        projects.closeCurrentProject();

        pc.fireWorkspaceEvent(ProjectControllerImpl.EventType.DISABLE, null);

        //Remove
        projects.removeProject(project);
    }

    @Override
    public boolean cancel() {
        return false;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        // Not needed as the task is short
    }
}
