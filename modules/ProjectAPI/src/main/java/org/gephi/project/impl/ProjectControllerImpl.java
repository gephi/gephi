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

import java.beans.PropertyEditorManager;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import org.gephi.project.api.GephiFormatException;
import org.gephi.project.api.LegacyGephiFormatException;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.ProjectListener;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.project.io.LoadTask;
import org.gephi.project.io.SaveTask;
import org.gephi.project.spi.WorkspaceDuplicateProvider;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ProjectController.class)
public class ProjectControllerImpl implements ProjectController {

    //Data
    private final ProjectsImpl projects = new ProjectsImpl();
    private final List<WorkspaceListener> workspaceListeners = new ArrayList<>();

    private final List<ProjectListener> projectListeners = new ArrayList<>();

    private final LongTaskExecutor longTaskExecutor = new LongTaskExecutor(false, "ProjectController");

    public ProjectControllerImpl() {
        registerNetbeansPropertyEditors();
    }

    /**
     * If not already registered, includes NetBeans property editors in the
     * search path. This is necessary when in the toolkit to properly save and
     * read project files.
     */
    private void registerNetbeansPropertyEditors() {
        List<String> list = new ArrayList<>(Arrays.asList(PropertyEditorManager.getEditorSearchPath()));
        if (!list.contains("org.netbeans.beaninfo.editors")) {
            list.add(0, "org.netbeans.beaninfo.editors");//Add first for more preference
            PropertyEditorManager.setEditorSearchPath(list.toArray(new String[list.size()]));
        }
    }

    @Override
    public ProjectImpl newProject() {
        synchronized (this) {
            fireProjectEvent(ProjectListener::lock);
            ProjectImpl project = null;
            try {
                closeCurrentProject();
                project = new ProjectImpl(projects.nextUntitledProjectName());
                projects.addProject(project);
                openProjectInternal(project);
                ProjectImpl finalProject = project;
                fireProjectEvent((pl) -> pl.opened(finalProject));
                return project;
            } catch (Exception e) {
                return handleException(project, e);
            }
        }
    }

    private ProjectImpl handleException(Project project, Throwable t) {
        fireProjectEvent((pl) -> pl.error(project, t));
        if (t instanceof GephiFormatException) {
            throw (GephiFormatException) t;
        } else if (t instanceof LegacyGephiFormatException) {
            throw (LegacyGephiFormatException) t;
        } else if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        }
        throw new RuntimeException(t);
    }

    @Override
    public Project openProject(File file) {
        synchronized (this) {
            fireProjectEvent(ProjectListener::lock);
            LoadTask loadTask = new LoadTask(file);
            Future<ProjectImpl> res = longTaskExecutor.execute(loadTask, () -> {
                ProjectImpl project = loadTask.execute(getProjects());
                // Null if cancelled
                if (project != null) {
                    openProjectInternal(project);
                    fireProjectEvent((pl) -> pl.opened(project));
                } else {
                    fireProjectEvent(ProjectListener::unlock);
                }
                return project;
            }, "", t -> handleException(null, t));
            try {
                return res.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void openProject(Project project) {
        if (!projects.containsProject(project)) {
            throw new IllegalArgumentException(
                "Project " + project.getUniqueIdentifier() + " does not belong to the list of active projects");
        }
        File file = project.getFile();
        if (file == null) {
            throw new IllegalArgumentException("Project " + project.getUniqueIdentifier() + " has no file associated");
        }
        openProject(file);
    }

    @Override
    public void saveProject(Project project) {
        synchronized (this) {
            if (project.getLookup().lookup(ProjectInformationImpl.class).hasFile()) {
                File file = project.getLookup().lookup(ProjectInformationImpl.class).getFile();
                saveProject(project, file);
            } else {
                throw new IllegalStateException("Project has no file");
            }
        }
    }

    @Override
    public void saveProject(Project project, File file) {
        synchronized (this) {
            fireProjectEvent(ProjectListener::lock);
            SaveTask saveTask = new SaveTask(project, file);
            longTaskExecutor.execute(saveTask, () -> {
                project.getLookup().lookup(ProjectInformationImpl.class).setFile(file);
                if (saveTask.run()) {
                    ((ProjectImpl) project).setLastOpened();
                    fireProjectEvent((pl) -> pl.saved(project));
                } else {
                    fireProjectEvent(ProjectListener::unlock);
                }
            }, "", t -> handleException(project, t));
        }
    }

    @Override
    public void closeCurrentProject() {
        synchronized (this) {
            if (projects.hasCurrentProject()) {
                fireProjectEvent(ProjectListener::lock);
                Project project = projects.getCurrentProject();

                try {
                    //Event
                    if (project.hasCurrentWorkspace()) {
                        fireWorkspaceEvent(ProjectControllerImpl.EventType.UNSELECT,
                            project.getCurrentWorkspace());
                    }
                    for (Workspace ws : project.getWorkspaces()) {
                        fireWorkspaceEvent(ProjectControllerImpl.EventType.CLOSE, ws);
                    }

                    //Close
                    projects.closeCurrentProject();

                    fireWorkspaceEvent(ProjectControllerImpl.EventType.DISABLE, null);
                    fireProjectEvent((pl) -> pl.closed(project));
                } catch (Exception e) {
                    handleException(project, e);
                }
            }
        }
    }

    @Override
    public void removeProject(Project project) {
        synchronized (this) {
            if (projects.getCurrentProject() == project) {
                closeCurrentProject();
            }
            projects.removeProject((ProjectImpl) project);
        }
    }

    @Override
    public ProjectsImpl getProjects() {
        synchronized (this) {
            return projects;
        }
    }

    @Override
    public Collection<Project> getAllProjects() {
        return Collections.unmodifiableList(Arrays.asList(projects.getProjects()));
    }

    @Override
    public boolean hasCurrentProject() {
        synchronized (this) {
            return projects.hasCurrentProject();
        }
    }

    @Override
    public Workspace newWorkspace(Project project) {
        synchronized (this) {
            Workspace workspace = project.getLookup().lookup(WorkspaceProviderImpl.class).newWorkspace();

            //Event
            fireWorkspaceEvent(EventType.INITIALIZE, workspace);
            return workspace;
        }
    }

    @Override
    public void deleteWorkspace(Workspace workspace) {
        synchronized (this) {
            Project project = workspace.getProject();
            WorkspaceProviderImpl workspaceProvider = project.getLookup().lookup(WorkspaceProviderImpl.class);

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
    }

    private void openProjectInternal(Project project) {
        final ProjectImpl projectImpl = (ProjectImpl) project;
        if (projects.hasCurrentProject()) {
            closeCurrentProject();
        }
        projects.addOrReplaceProject(projectImpl);
        projects.setCurrentProject(projectImpl);

        for (Workspace ws : projectImpl.getWorkspaces()) {
            fireWorkspaceEvent(EventType.INITIALIZE, ws);
        }

        if (!projectImpl.hasCurrentWorkspace()) {
            if (projectImpl.getWorkspaces().isEmpty()) {
                Workspace workspace = newWorkspace(project);
                openWorkspace(workspace);
            } else {
                Workspace workspace = projectImpl.getWorkspaces().get(0);
                openWorkspace(workspace);
            }
        } else {
            fireWorkspaceEvent(EventType.SELECT, projectImpl.getCurrentWorkspace());
        }
    }

    @Override
    public ProjectImpl getCurrentProject() {
        synchronized (this) {
            return projects.getCurrentProject();
        }
    }

    @Override
    public WorkspaceImpl getCurrentWorkspace() {
        synchronized (this) {
            if (projects.hasCurrentProject()) {
                return getCurrentProject().getCurrentWorkspace();
            }
            return null;
        }
    }

    @Override
    public void closeCurrentWorkspace() {
        synchronized (this) {
            WorkspaceImpl workspace = getCurrentWorkspace();
            if (workspace != null) {
                workspace.getLookup().lookup(WorkspaceInformationImpl.class).close();

                //Event
                fireWorkspaceEvent(EventType.UNSELECT, workspace);
            }
        }
    }

    @Override
    public void openWorkspace(Workspace workspace) {
        synchronized (this) {
            closeCurrentWorkspace();
            getCurrentProject().setCurrentWorkspace(workspace);

            //Event
            fireWorkspaceEvent(EventType.SELECT, workspace);
        }
    }

    @Override
    public Workspace openNewWorkspace() {
        synchronized (this) {
            Project project;
            if (hasCurrentProject()) {
                project = getCurrentProject();
            } else {
                project = newProject();
            }
            Workspace workspace = newWorkspace(project);
            openWorkspace(workspace);
            return workspace;
        }
    }

    @Override
    public Workspace duplicateWorkspace(Workspace workspace) {
        synchronized (this) {
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
    }

    @Override
    public void renameProject(Project project, final String name) {
        synchronized (this) {
            project.getLookup().lookup(ProjectInformationImpl.class).setName(name);
            fireProjectEvent((pl) -> pl.changed(project));
        }
    }

    @Override
    public void renameWorkspace(Workspace workspace, String name) {
        synchronized (this) {
            workspace.getLookup().lookup(WorkspaceInformationImpl.class).setName(name);
        }
    }

    @Override
    public void setSource(Workspace workspace, String source) {
        synchronized (this) {
            workspace.getLookup().lookup(WorkspaceInformationImpl.class).setSource(source);
        }
    }

    @Override
    public void addWorkspaceListener(WorkspaceListener workspaceListener) {
        synchronized (workspaceListeners) {
            workspaceListeners.add(workspaceListener);
        }
    }

    @Override
    public void removeWorkspaceListener(WorkspaceListener workspaceListener) {
        synchronized (workspaceListeners) {
            workspaceListeners.remove(workspaceListener);
        }
    }

    protected void addProjectListener(ProjectListener projectListener) {
        synchronized (projectListeners) {
            projectListeners.add(projectListener);
        }
    }

    protected void removeProjectListener(ProjectListener projectListener) {
        synchronized (projectListeners) {
            projectListeners.remove(projectListener);
        }
    }

    private void fireProjectEvent(Consumer<? super ProjectListener> consumer) {
        List<ProjectListener> listeners;
        synchronized (projectListeners) {
            listeners = new ArrayList<>(projectListeners);
            listeners.addAll(Lookup.getDefault().lookupAll(ProjectListener.class));
        }
        listeners.forEach(consumer);
    }

    private void fireWorkspaceEvent(EventType event, Workspace workspace) {
        List<WorkspaceListener> listeners;
        synchronized (workspaceListeners) {
            listeners = new ArrayList<>(workspaceListeners);
            listeners.addAll(Lookup.getDefault().lookupAll(WorkspaceListener.class));
        }
        for (WorkspaceListener wl : listeners) {
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

    public enum EventType {

        INITIALIZE, SELECT, UNSELECT, CLOSE, DISABLE
    }
}
