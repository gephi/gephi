/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.project.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Projects;
import org.gephi.project.api.WorkspaceProvider;
import org.gephi.project.io.GephiDataObject;
import org.gephi.project.io.LoadTask;
import org.gephi.project.io.SaveTask;
import org.gephi.ui.utils.DialogFileFilter;
import org.gephi.utils.longtask.LongTask;
import org.gephi.utils.longtask.LongTaskErrorHandler;
import org.gephi.utils.longtask.LongTaskExecutor;
import org.gephi.utils.longtask.LongTaskListener;
import org.gephi.workspace.api.Workspace;
import org.gephi.workspace.api.WorkspaceInformation;
import org.gephi.workspace.api.WorkspaceListener;
import org.gephi.workspace.impl.WorkspaceImpl;
import org.gephi.workspace.impl.WorkspaceInformationImpl;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ProjectController.class)
public class ProjectControllerImpl implements ProjectController {

    private enum EventType {

        INITIALIZE, SELECT, UNSELECT, CLOSE, DISABLE
    };
    //Actions
    private boolean openProject = true;
    private boolean newProject = true;
    private boolean openFile = true;
    private boolean saveProject;
    private boolean saveAsProject;
    private boolean projectProperties;
    private boolean closeProject;
    private boolean newWorkspace;
    private boolean deleteWorkspace;
    private boolean cleanWorkspace;
    private boolean duplicateWorkspace;
    //Data
    private final ProjectsImpl projects = new ProjectsImpl();
    private final LongTaskExecutor longTaskExecutor;
    private final List<WorkspaceListener> listeners;

    public ProjectControllerImpl() {
        //Project IO executor
        longTaskExecutor = new LongTaskExecutor(true, "Project IO");
        longTaskExecutor.setDefaultErrorHandler(new LongTaskErrorHandler() {

            public void fatalError(Throwable t) {
                unlockProjectActions();
                Logger.getLogger("").log(Level.WARNING, "", t.getCause());
            }
        });
        longTaskExecutor.setLongTaskListener(new LongTaskListener() {

            public void taskFinished(LongTask task) {
                unlockProjectActions();
            }
        });

        //Listeners
        listeners = new ArrayList<WorkspaceListener>();
        listeners.addAll(Lookup.getDefault().lookupAll(WorkspaceListener.class));

        //Actions
        saveProject = false;
        saveAsProject = false;
        projectProperties = false;
        closeProject = false;
        newWorkspace = false;
        deleteWorkspace = false;
        cleanWorkspace = false;
        duplicateWorkspace = false;
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

    private void lockProjectActions() {
        saveProject = false;
        saveAsProject = false;
        openProject = false;
        closeProject = false;
        newProject = false;
        openFile = false;
        newWorkspace = false;
        deleteWorkspace = false;
        cleanWorkspace = false;
        duplicateWorkspace = false;
    }

    private void unlockProjectActions() {
        if (projects.hasCurrentProject()) {
            saveProject = true;
            saveAsProject = true;
            closeProject = true;
            newWorkspace = true;
            if (projects.getCurrentProject().getLookup().lookup(WorkspaceProvider.class).hasCurrentWorkspace()) {
                deleteWorkspace = true;
                cleanWorkspace = true;
                duplicateWorkspace = true;
            }
        }
        openProject = true;
        newProject = true;
        openFile = true;
    }

    public void newProject() {
        closeCurrentProject();
        ProjectImpl project = new ProjectImpl();
        projects.addProject(project);
        openProject(project);
    }

    public void loadProject(DataObject dataObject) {
        final GephiDataObject gephiDataObject = (GephiDataObject) dataObject;
        LoadTask loadTask = new LoadTask(gephiDataObject);
        lockProjectActions();
        longTaskExecutor.execute(loadTask, loadTask);
    }

    public void saveProject(DataObject dataObject) {
        GephiDataObject gephiDataObject = (GephiDataObject) dataObject;
        ProjectImpl project = getCurrentProject();
        project.getLookup().lookup(ProjectInformationImpl.class).setDataObject(gephiDataObject);
        gephiDataObject.setProject(project);
        SaveTask saveTask = new SaveTask(gephiDataObject);
        lockProjectActions();
        longTaskExecutor.execute(saveTask, saveTask);
    }

    public void saveProject(Project project) {
        if (project.getLookup().lookup(ProjectInformationImpl.class).hasFile()) {
            GephiDataObject gephiDataObject = (GephiDataObject) project.getLookup().lookup(ProjectInformationImpl.class).getDataObject();
            saveProject(gephiDataObject);
        } else {
            saveAsProject(project);
        }
    }

    public void saveAsProject(Project project) {
        final String LAST_PATH = "SaveAsProject_Last_Path";
        final String LAST_PATH_DEFAULT = "SaveAsProject_Last_Path_Default";

        DialogFileFilter filter = new DialogFileFilter(NbBundle.getMessage(ProjectControllerImpl.class, "SaveAsProject_filechooser_filter"));
        filter.addExtension(".gephi");

        //Get last directory
        String lastPathDefault = NbPreferences.forModule(ProjectControllerImpl.class).get(LAST_PATH_DEFAULT, null);
        String lastPath = NbPreferences.forModule(ProjectControllerImpl.class).get(LAST_PATH, lastPathDefault);

        //File chooser
        final JFileChooser chooser = new JFileChooser(lastPath);
        chooser.addChoosableFileFilter(filter);
        int returnFile = chooser.showSaveDialog(null);
        if (returnFile == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();

            //Save last path
            NbPreferences.forModule(ProjectControllerImpl.class).put(LAST_PATH, file.getAbsolutePath());

            //File management
            try {
                if (!file.getPath().endsWith(".gephi")) {
                    file = new File(file.getPath() + ".gephi");
                }
                if (!file.exists()) {
                    if (!file.createNewFile()) {
                        String failMsg = NbBundle.getMessage(
                                ProjectControllerImpl.class,
                                "SaveAsProject_SaveFailed", new Object[]{file.getPath()});
                        JOptionPane.showMessageDialog(null, failMsg);
                        return;
                    }
                } else {
                    String overwriteMsg = NbBundle.getMessage(
                            ProjectControllerImpl.class,
                            "SaveAsProject_Overwrite", new Object[]{file.getPath()});
                    if (JOptionPane.showConfirmDialog(null, overwriteMsg) != JOptionPane.OK_OPTION) {
                        return;
                    }
                }
                file = FileUtil.normalizeFile(file);
                FileObject fileObject = FileUtil.toFileObject(file);

                //File exist now, Save project
                DataObject dataObject = DataObject.find(fileObject);
                saveProject(dataObject);

            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    public void closeCurrentProject() {
        if (projects.hasCurrentProject()) {
            ProjectImpl currentProject = projects.getCurrentProject();

            //Save ?
            String messageBundle = NbBundle.getMessage(ProjectControllerImpl.class, "CloseProject_confirm_message");
            String titleBundle = NbBundle.getMessage(ProjectControllerImpl.class, "CloseProject_confirm_title");
            String saveBundle = NbBundle.getMessage(ProjectControllerImpl.class, "CloseProject_confirm_save");
            String doNotSaveBundle = NbBundle.getMessage(ProjectControllerImpl.class, "CloseProject_confirm_doNotSave");
            String cancelBundle = NbBundle.getMessage(ProjectControllerImpl.class, "CloseProject_confirm_cancel");
            NotifyDescriptor msg = new NotifyDescriptor(messageBundle, titleBundle,
                    NotifyDescriptor.YES_NO_CANCEL_OPTION,
                    NotifyDescriptor.INFORMATION_MESSAGE,
                    new Object[]{saveBundle, doNotSaveBundle, cancelBundle}, saveBundle);
            Object result = DialogDisplayer.getDefault().notify(msg);
            if (result == saveBundle) {
                saveProject(currentProject);
            } else if (result == cancelBundle) {
                return;
            }

            //Close
            currentProject.getLookup().lookup(ProjectInformationImpl.class).close();
            projects.closeCurrentProject();


            //Actions
            saveProject = false;
            saveAsProject = false;
            projectProperties = false;
            closeProject = false;
            newWorkspace = false;
            deleteWorkspace = false;
            cleanWorkspace = false;
            duplicateWorkspace = false;

            //Title bar
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
                    String title = frame.getTitle();
                    title = title.substring(0, title.indexOf('-') - 1);
                    frame.setTitle(title);
                }
            });

            //Event
            if (currentProject.getLookup().lookup(WorkspaceProvider.class).hasCurrentWorkspace()) {
                fireWorkspaceEvent(EventType.UNSELECT, currentProject.getLookup().lookup(WorkspaceProvider.class).getCurrentWorkspace());
            }
            for (Workspace ws : currentProject.getLookup().lookup(WorkspaceProviderImpl.class).getWorkspaces()) {
                fireWorkspaceEvent(EventType.CLOSE, ws);
            }
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

    /*public Workspace importFile() {
    Project project = projects.getCurrentProject();
    if (project == null) {
    newProject();
    project = projects.getCurrentProject();
    }

    Workspace ws = newWorkspace(projects.getCurrentProject());
    openWorkspace(ws);
    return ws;
    }*/
    public void deleteWorkspace(Workspace workspace) {
        if (getCurrentWorkspace() == workspace) {
            closeCurrentProject();
        }
        WorkspaceInformation wi = workspace.getLookup().lookup(WorkspaceInformation.class);

        wi.getProject().getLookup().lookup(WorkspaceProviderImpl.class).removeWorkspace(workspace);

        //Event
        fireWorkspaceEvent(EventType.CLOSE, workspace);

        if (getCurrentProject() == null || getCurrentProject().getLookup().lookup(WorkspaceProviderImpl.class).getWorkspaces().length == 0) {
            //Event
            fireWorkspaceEvent(EventType.DISABLE, workspace);
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

        saveProject = true;
        saveAsProject = true;
        projectProperties = true;
        closeProject = true;
        newWorkspace = true;
        if (workspaceProviderImpl.hasCurrentWorkspace()) {
            deleteWorkspace = true;
            cleanWorkspace = true;
            duplicateWorkspace = true;
        }

        //Title bar
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
                String title = frame.getTitle() + " - " + projectInformationImpl.getName();
                frame.setTitle(title);
            }
        });

        //Status line
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(ProjectControllerImpl.class, "DesktoProjectController.status.opened", projectInformationImpl.getName()));
    }

    public ProjectImpl getCurrentProject() {
        return projects.getCurrentProject();
    }

    public WorkspaceImpl getCurrentWorkspace() {
        if (projects.hasCurrentProject()) {
            return getCurrentProject().getLookup().lookup(WorkspaceProviderImpl.class).getCurrentWorkspace();
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

    public void duplicateWorkspace(Workspace workspace) {
    }

    public void renameProject(Project project, final String name) {
        project.getLookup().lookup(ProjectInformationImpl.class);

        //Title bar
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
                String title = frame.getTitle();
                title = title.substring(0, title.indexOf('-') - 1);
                title += " - " + name;
                frame.setTitle(title);
            }
        });
    }

    public void renameWorkspace(Workspace workspace, String name) {
        workspace.getLookup().lookup(WorkspaceInformationImpl.class).setName(name);
    }

    public void setSource(Workspace workspace, String source) {
        workspace.getLookup().lookup(WorkspaceInformationImpl.class).setSource(source);
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

    public boolean canCleanWorkspace() {
        return cleanWorkspace;
    }

    public boolean canCloseProject() {
        return closeProject;
    }

    public boolean canDeleteWorkspace() {
        return deleteWorkspace;
    }

    public boolean canNewProject() {
        return newProject;
    }

    public boolean canNewWorkspace() {
        return newWorkspace;
    }

    public boolean canOpenFile() {
        return openFile;
    }

    public boolean canOpenProject() {
        return openProject;
    }

    public boolean canSave() {
        return saveProject;
    }

    public boolean canSaveAs() {
        return saveAsProject;
    }

    public boolean canProjectProperties() {
        return projectProperties;
    }
}
