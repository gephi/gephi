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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.project.api.GephiFormatException;
import org.gephi.project.api.LegacyGephiFormatException;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.ProjectListener;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.project.io.DuplicateTask;
import org.gephi.project.io.LoadTask;
import org.gephi.project.io.SaveTask;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ProjectController.class)
public class ProjectControllerImpl implements ProjectController {

    private static final Logger LOGGER = Logger.getLogger(ProjectControllerImpl.class.getName());

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

    /**
     * Reports a mutating operation started from the Event Dispatch Thread. Such an operation blocks
     * its caller until completion and notifies the listeners synchronously, so on the EDT it freezes
     * the interface and hands the listeners a thread they are told never to expect.
     * <p>
     * The EDT is detected by thread name deliberately: <code>EventQueue.isDispatchThread()</code> and
     * <code>SwingUtilities.isEventDispatchThread()</code> both go through
     * <code>Toolkit.getDefaultToolkit()</code> and would initialize the AWT toolkit as a side effect.
     * This module is AWT-free so that gephi-toolkit can use it headless, so please don't replace this
     * check with the Swing one.
     */
    private void warnIfEventDispatchThread(String method) {
        if (Thread.currentThread().getName().startsWith("AWT-EventQueue")) {
            StringWriter callSite = new StringWriter();
            PrintWriter writer = new PrintWriter(callSite);
            new Throwable("call site").printStackTrace(writer);
            writer.flush();
            LOGGER.log(Level.WARNING, "ProjectController." + method
                + "() was called from the Event Dispatch Thread. It blocks its caller until the operation completes"
                + " and notifies the project and workspace listeners synchronously, so it freezes the interface."
                + " Run it on a background thread instead; the desktop application has ProjectControllerUIImpl"
                + " for that. This will become an error in a future release.\n" + callSite);
        }
    }

    @Override
    public ProjectImpl newProject() {
        warnIfEventDispatchThread("newProject");
        return this.newProjectInternal();
    }


    private ProjectImpl newProjectInternal(Object... objectsForLookup) {
        synchronized (this) {
            fireProjectEvent(ProjectListener::lock);
            ProjectImpl project = null;
            try {
                closeCurrentProject();
                project = new ProjectImpl(projects.nextUntitledProjectName());
                projects.addProject(project);
                openProjectInternal(project, objectsForLookup);
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
        warnIfEventDispatchThread("openProject");
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
        warnIfEventDispatchThread("openProject");
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
        warnIfEventDispatchThread("saveProject");
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
        warnIfEventDispatchThread("saveProject");
        synchronized (this) {
            fireProjectEvent(ProjectListener::lock);
            SaveTask saveTask = new SaveTask(project, file);
            longTaskExecutor.execute(saveTask, () -> {
                if (saveTask.run()) {
                    //Only associate the project with the file once it has actually been written
                    project.getLookup().lookup(ProjectInformationImpl.class).setFile(file);
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
        warnIfEventDispatchThread("closeCurrentProject");
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
        warnIfEventDispatchThread("removeProject");
        synchronized (this) {
            if (projects.getCurrentProject() == project) {
                closeCurrentProject();
            }
            projects.removeProject((ProjectImpl) project);
        }
    }

    @Override
    public ProjectsImpl getProjects() {
        return projects;
    }

    @Override
    public Collection<Project> getAllProjects() {
        return Collections.unmodifiableList(Arrays.asList(projects.getProjects()));
    }

    @Override
    public boolean hasCurrentProject() {
        return projects.hasCurrentProject();
    }

    @Override
    public Workspace newWorkspace(Project project) {
        warnIfEventDispatchThread("newWorkspace");
        return newWorkspaceInternal(project);
    }

    @Override
    public Workspace newWorkspace(Project project, Object... objectsForLookup) {
        warnIfEventDispatchThread("newWorkspace");
        return newWorkspaceInternal(project, objectsForLookup);
    }

    private Workspace newWorkspaceInternal(Project project, Object... objectsForLookup) {
        synchronized (this) {
            WorkspaceProviderImpl workspaceProvider = project.getLookup().lookup(WorkspaceProviderImpl.class);
            Workspace workspace = workspaceProvider.newWorkspace(workspaceProvider.getProject().nextWorkspaceId(), objectsForLookup);

            //Event
            fireWorkspaceEvent(EventType.INITIALIZE, workspace);
            return workspace;
        }
    }

    @Override
    public void deleteWorkspace(Workspace workspace) {
        warnIfEventDispatchThread("deleteWorkspace");
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

    private void openProjectInternal(Project project, Object... objectsForLookup) {
        ProjectImpl projectImpl = (ProjectImpl) project;
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
                Workspace workspace = newWorkspace(project, objectsForLookup);
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
        return projects.getCurrentProject();
    }

    @Override
    public WorkspaceImpl getCurrentWorkspace() {
        // Read-only access; do not acquire the controller-wide monitor here, as long-running
        // write operations (open/save/load) hold it and the EDT frequently calls this method.
        ProjectImpl current = projects.getCurrentProject();
        return current != null ? current.getCurrentWorkspace() : null;
    }

    @Override
    public void closeCurrentWorkspace() {
        warnIfEventDispatchThread("closeCurrentWorkspace");
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
        warnIfEventDispatchThread("openWorkspace");
        synchronized (this) {
            closeCurrentWorkspace();
            getCurrentProject().setCurrentWorkspace(workspace);

            //Event
            fireWorkspaceEvent(EventType.SELECT, workspace);
        }
    }

    @Override
    public Workspace openNewWorkspace() {
        warnIfEventDispatchThread("openNewWorkspace");
        return openNewWorkspaceInternal();
    }

    @Override
    public Workspace openNewWorkspace(Object... objectsForLookup) {
        warnIfEventDispatchThread("openNewWorkspace");
        return openNewWorkspaceInternal(objectsForLookup);
    }

    private Workspace openNewWorkspaceInternal(Object... objectsForLookup) {
        synchronized (this) {
            Project project;
            Workspace workspace;
            if (hasCurrentProject()) {
                project = getCurrentProject();
                workspace = newWorkspace(project, objectsForLookup);
                openWorkspace(workspace);
            } else {
                project = newProjectInternal(objectsForLookup);
                workspace = project.getCurrentWorkspace();
            }
            return workspace;
        }
    }

    @Override
    public Workspace duplicateWorkspace(Workspace workspace) {
        warnIfEventDispatchThread("duplicateWorkspace");
        synchronized (this) {
            DuplicateTask duplicateTask = new DuplicateTask(workspace);
            Future<WorkspaceImpl> res = longTaskExecutor.execute(duplicateTask, () -> {
                WorkspaceImpl newWorkspace = duplicateTask.run();
                // Null if cancelled
                if (newWorkspace != null) {
                    newWorkspace.getLookup().lookup(WorkspaceInformationImpl.class).setName(
                        NbBundle.getMessage(ProjectControllerImpl.class, "Workspace.duplicated.name",
                            workspace.getName()));
                    fireWorkspaceEvent(EventType.INITIALIZE, newWorkspace);

                    openWorkspace(newWorkspace);
                }
                return newWorkspace;
            }, "", t -> handleException(workspace.getProject(), t));
            try {
                return res.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void renameProject(Project project, final String name) {
        warnIfEventDispatchThread("renameProject");
        synchronized (this) {
            project.getLookup().lookup(ProjectInformationImpl.class).setName(name);
            fireProjectEvent((pl) -> pl.changed(project));
        }
    }

    @Override
    public void renameWorkspace(Workspace workspace, String name) {
        warnIfEventDispatchThread("renameWorkspace");
        synchronized (this) {
            workspace.getLookup().lookup(WorkspaceInformationImpl.class).setName(name);
        }
    }

    @Override
    public void setSource(Workspace workspace, String source) {
        warnIfEventDispatchThread("setSource");
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
        for (ProjectListener listener : listeners) {
            try {
                consumer.accept(listener);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private void fireWorkspaceEvent(EventType event, Workspace workspace) {
        List<WorkspaceListener> listeners;
        synchronized (workspaceListeners) {
            listeners = new ArrayList<>(workspaceListeners);
            listeners.addAll(Lookup.getDefault().lookupAll(WorkspaceListener.class));
        }
        for (WorkspaceListener wl : listeners) {
            try {
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
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    public enum EventType {

        INITIALIZE, SELECT, UNSELECT, CLOSE, DISABLE
    }
}
