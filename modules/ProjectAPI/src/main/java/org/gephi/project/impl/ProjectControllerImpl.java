/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.project.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Projects;
import org.gephi.project.api.WorkspaceProvider;
import org.gephi.project.io.LoadTask;
import org.gephi.project.io.SaveTask;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceInformation;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.workspace.impl.WorkspaceImpl;
import org.gephi.workspace.impl.WorkspaceInformationImpl;
import org.gephi.project.spi.WorkspaceDuplicateProvider;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ProjectController.class)
public class ProjectControllerImpl implements ProjectController {

    private enum EventType {

        INITIALIZE, SELECT, UNSELECT, CLOSE, DISABLE
    };
    //Data
    private final ProjectsImpl projects = new ProjectsImpl();
    private final List<WorkspaceListener> listeners;
    private WorkspaceImpl temporaryOpeningWorkspace;

    public ProjectControllerImpl() {

        //Listeners
        listeners = new ArrayList<WorkspaceListener>();
        listeners.addAll(Lookup.getDefault().lookupAll(WorkspaceListener.class));
    }

    public void startup() {
        final String OPEN_LAST_PROJECT_ON_STARTUP = "Open_Last_Project_On_Startup";
        final String NEW_PROJECT_ON_STARTUP = "New_Project_On_Startup";
        boolean openLastProject = NbPreferences.forModule(ProjectControllerImpl.class).getBoolean(OPEN_LAST_PROJECT_ON_STARTUP, false);
        boolean newProjectStartup = NbPreferences.forModule(ProjectControllerImpl.class).getBoolean(NEW_PROJECT_ON_STARTUP, false);

        //Default project
        if (!openLastProject && newProjectStartup) {
            newProject();
        }
    }

    public void newProject() {
        closeCurrentProject();
        ProjectImpl project = new ProjectImpl();
        projects.addProject(project);
        openProject(project);
    }

    public Runnable openProject(File file) {
        return new LoadTask(file);
    }

    public Runnable saveProject(Project project) {
        if (project.getLookup().lookup(ProjectInformationImpl.class).hasFile()) {
            File file = project.getLookup().lookup(ProjectInformationImpl.class).getFile();
            return saveProject(project, file);
        }
        return null;
    }

    public Runnable saveProject(Project project, File file) {
        project.getLookup().lookup(ProjectInformationImpl.class).setFile(file);
        SaveTask saveTask = new SaveTask(project, file);
        return saveTask;
    }

    public void closeCurrentProject() {
        if (projects.hasCurrentProject()) {
            ProjectImpl currentProject = projects.getCurrentProject();

            //Event
            if (currentProject.getLookup().lookup(WorkspaceProvider.class).hasCurrentWorkspace()) {
                fireWorkspaceEvent(EventType.UNSELECT, currentProject.getLookup().lookup(WorkspaceProvider.class).getCurrentWorkspace());
            }
            for (Workspace ws : currentProject.getLookup().lookup(WorkspaceProviderImpl.class).getWorkspaces()) {
                fireWorkspaceEvent(EventType.CLOSE, ws);
            }

            //Close
            currentProject.getLookup().lookup(ProjectInformationImpl.class).close();
            projects.closeCurrentProject();

            fireWorkspaceEvent(EventType.DISABLE, null);
        }
    }

    public void removeProject(Project project) {
        if (projects.getCurrentProject() == project) {
            closeCurrentProject();
        }
        projects.removeProject(project);
    }

    public Projects getProjects() {
        return projects;
    }

    public void setProjects(Projects projects) {
        final String OPEN_LAST_PROJECT_ON_STARTUP = "Open_Last_Project_On_Startup";
        boolean openLastProject = NbPreferences.forModule(ProjectControllerImpl.class).getBoolean(OPEN_LAST_PROJECT_ON_STARTUP, false);

        Project lastOpenProject = null;
        for (Project p : ((ProjectsImpl) projects).getProjects()) {
            if (p.getLookup().lookup(ProjectInformationImpl.class).hasFile()) {
                ProjectImpl pImpl = (ProjectImpl) p;
                pImpl.init();
                this.projects.addProject(p);
                pImpl.getLookup().lookup(ProjectInformationImpl.class).close();
                if (p == projects.getCurrentProject()) {
                    lastOpenProject = p;
                }
            }
        }

        if (openLastProject && lastOpenProject != null && !lastOpenProject.getLookup().lookup(ProjectInformationImpl.class).isInvalid() && lastOpenProject.getLookup().lookup(ProjectInformationImpl.class).hasFile()) {
            openProject(lastOpenProject);
        } else {
            //newProject();
        }
    }

    public Workspace newWorkspace(Project project) {
        Workspace workspace = project.getLookup().lookup(WorkspaceProviderImpl.class).newWorkspace();

        //Event
        fireWorkspaceEvent(EventType.INITIALIZE, workspace);
        return workspace;
    }

    public void deleteWorkspace(Workspace workspace) {
        WorkspaceInformation wi = workspace.getLookup().lookup(WorkspaceInformation.class);
        WorkspaceProviderImpl workspaceProvider = wi.getProject().getLookup().lookup(WorkspaceProviderImpl.class);

        Workspace toSelectWorkspace = null;
        if (getCurrentWorkspace() == workspace) {
            toSelectWorkspace = workspaceProvider.getPrecedingWorkspace(workspace);
        }

        workspaceProvider.removeWorkspace(workspace);

        //Event
        fireWorkspaceEvent(EventType.CLOSE, workspace);

        if (getCurrentWorkspace() == workspace) {
            //Select the one before, or after
            if (toSelectWorkspace == null) {
                closeCurrentProject();
            } else {
                openWorkspace(toSelectWorkspace);
            }
        }

    }

    public void openProject(Project project) {
        final ProjectImpl projectImpl = (ProjectImpl) project;
        final ProjectInformationImpl projectInformationImpl = projectImpl.getLookup().lookup(ProjectInformationImpl.class);
        final WorkspaceProviderImpl workspaceProviderImpl = project.getLookup().lookup(WorkspaceProviderImpl.class);

        if (projects.hasCurrentProject()) {
            closeCurrentProject();
        }
        projects.addProject(projectImpl);
        projects.setCurrentProject(projectImpl);
        projectInformationImpl.open();

        if (!workspaceProviderImpl.hasCurrentWorkspace()) {
            if (workspaceProviderImpl.getWorkspaces().length == 0) {
                Workspace workspace = newWorkspace(project);
                openWorkspace(workspace);
            } else {
                Workspace workspace = workspaceProviderImpl.getWorkspaces()[0];
                openWorkspace(workspace);
            }
        } else {
            fireWorkspaceEvent(EventType.SELECT, workspaceProviderImpl.getCurrentWorkspace());
        }
    }

    public ProjectImpl getCurrentProject() {
        return projects.getCurrentProject();
    }

    public WorkspaceImpl getCurrentWorkspace() {
        if (projects.hasCurrentProject()) {
            temporaryOpeningWorkspace = null;
            return getCurrentProject().getLookup().lookup(WorkspaceProviderImpl.class).getCurrentWorkspace();
        } else if (temporaryOpeningWorkspace != null) {
            return temporaryOpeningWorkspace;
        }
        return null;
    }

    public void closeCurrentWorkspace() {
        WorkspaceImpl workspace = getCurrentWorkspace();
        if (workspace != null) {
            workspace.getLookup().lookup(WorkspaceInformationImpl.class).close();

            //Event
            fireWorkspaceEvent(EventType.UNSELECT, workspace);
        }
    }

    public void openWorkspace(Workspace workspace) {
        closeCurrentWorkspace();
        getCurrentProject().getLookup().lookup(WorkspaceProviderImpl.class).setCurrentWorkspace(workspace);
        workspace.getLookup().lookup(WorkspaceInformationImpl.class).open();

        //Event
        fireWorkspaceEvent(EventType.SELECT, workspace);
    }

    public void cleanWorkspace(Workspace workspace) {
    }

    public Workspace duplicateWorkspace(Workspace workspace) {
        if (projects.hasCurrentProject()) {
            Workspace duplicate = newWorkspace(projects.getCurrentProject());
            for (WorkspaceDuplicateProvider dp : Lookup.getDefault().lookupAll(WorkspaceDuplicateProvider.class)) {
                dp.duplicate(workspace, duplicate);
            }
            openWorkspace(duplicate);
            return duplicate;
        }
        return null;
    }

    public void renameProject(Project project, final String name) {
        project.getLookup().lookup(ProjectInformationImpl.class).setName(name);
    }

    public void renameWorkspace(Workspace workspace, String name) {
        workspace.getLookup().lookup(WorkspaceInformationImpl.class).setName(name);
    }

    public void setSource(Workspace workspace, String source) {
        workspace.getLookup().lookup(WorkspaceInformationImpl.class).setSource(source);
    }

    /**
     * Hack to have a current workpace when opening workspace
     * @param temporaryOpeningWorkspace the opening workspace or null
     */
    public void setTemporaryOpeningWorkspace(WorkspaceImpl temporaryOpeningWorkspace) {
        this.temporaryOpeningWorkspace = temporaryOpeningWorkspace;
        if (temporaryOpeningWorkspace != null) {
            //Init controllers with empty models
            fireWorkspaceEvent(EventType.INITIALIZE, temporaryOpeningWorkspace);
        }
    }

    public void addWorkspaceListener(WorkspaceListener workspaceListener) {
        synchronized (listeners) {
            listeners.add(workspaceListener);
        }
    }

    public void removeWorkspaceListener(WorkspaceListener workspaceListener) {
        synchronized (listeners) {
            listeners.remove(workspaceListener);
        }
    }

    private void fireWorkspaceEvent(EventType event, Workspace workspace) {
        WorkspaceListener[] listenersArray;
        synchronized (listeners) {
            listenersArray = listeners.toArray(new WorkspaceListener[0]);
        }
        for (WorkspaceListener wl : listenersArray) {
            switch (event) {
                case INITIALIZE:
                    wl.initialize(workspace);
                    break;
                case SELECT:
                    wl.select(workspace);
                    break;
                case UNSELECT:
                    wl.unselect(workspace);
                    break;
                case CLOSE:
                    wl.close(workspace);
                    break;
                case DISABLE:
                    wl.disable();
                    break;
            }
        }
    }
}
