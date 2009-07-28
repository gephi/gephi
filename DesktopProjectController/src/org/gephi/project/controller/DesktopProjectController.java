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
package org.gephi.project.controller;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.gephi.branding.desktop.actions.NewProject;
import org.gephi.branding.desktop.actions.OpenFile;
import org.gephi.branding.desktop.actions.OpenProject;
import org.gephi.branding.desktop.actions.ProjectProperties;
import org.gephi.branding.desktop.actions.SaveAsProject;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Projects;
import org.gephi.project.api.Workspace;
import org.openide.util.actions.SystemAction;
import org.gephi.branding.desktop.actions.SaveProject;
import org.gephi.io.project.GephiDataObject;
import org.gephi.project.ProjectImpl;
import org.gephi.project.ProjectsImpl;
import org.gephi.ui.utils.DialogFileFilter;
import org.gephi.utils.longtask.LongTask;
import org.gephi.utils.longtask.LongTaskErrorHandler;
import org.gephi.utils.longtask.LongTaskExecutor;
import org.gephi.utils.longtask.LongTaskListener;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 *
 * @author Mathieu Bastian
 */
public class DesktopProjectController implements ProjectController {

    private Projects projects = new ProjectsImpl();
    private LongTaskExecutor longTaskExecutor;

    public DesktopProjectController() {
        //Project IO executor
        longTaskExecutor = new LongTaskExecutor(true, "Project IO");
        longTaskExecutor.setDefaultErrorHandler(new LongTaskErrorHandler() {

            public void fatalError(Throwable t) {
                unlockProjectActions();
                NotifyDescriptor.Exception ex = new NotifyDescriptor.Exception(t);
                DialogDisplayer.getDefault().notify(ex);
                t.printStackTrace();
            }
        });
        longTaskExecutor.setLongTaskListener(new LongTaskListener() {

            public void taskFinished(LongTask task) {
                unlockProjectActions();
            }
        });

        //Actions
        disableAction(SaveProject.class);
        disableAction(SaveAsProject.class);
        disableAction(ProjectProperties.class);
    }

    private void lockProjectActions() {
        disableAction(SaveProject.class);
        disableAction(SaveAsProject.class);
        disableAction(OpenProject.class);
        disableAction(NewProject.class);
        disableAction(OpenFile.class);
    }

    private void unlockProjectActions() {
        if (projects.hasCurrentProject()) {
            enableAction(SaveProject.class);
            enableAction(SaveAsProject.class);
        }
        enableAction(OpenProject.class);
        enableAction(NewProject.class);
        enableAction(OpenFile.class);
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
        Project project = getCurrentProject();
        project.setDataObject(gephiDataObject);
        gephiDataObject.setProject(project);
        SaveTask saveTask = new SaveTask(gephiDataObject);
        lockProjectActions();
        longTaskExecutor.execute(saveTask, saveTask);
    }

    public void saveProject(Project project) {
        if (project.hasFile()) {
            GephiDataObject gephiDataObject = (GephiDataObject) project.getDataObject();
            saveProject(gephiDataObject);
        } else {
            saveAsProject(project);
        }
    }

    public void saveAsProject(Project project) {
        final String LAST_PATH = "SaveAsProject_Last_Path";
        final String LAST_PATH_DEFAULT = "SaveAsProject_Last_Path_Default";

        DialogFileFilter filter = new DialogFileFilter(NbBundle.getMessage(DesktopProjectController.class, "SaveAsProject_filechooser_filter"));
        filter.addExtension(".gephi");

        //Get last directory
        String lastPathDefault = NbPreferences.forModule(DesktopProjectController.class).get(LAST_PATH_DEFAULT, null);
        String lastPath = NbPreferences.forModule(DesktopProjectController.class).get(LAST_PATH, lastPathDefault);

        //File chooser
        final JFileChooser chooser = new JFileChooser(lastPath);
        chooser.addChoosableFileFilter(filter);
        int returnFile = chooser.showSaveDialog(null);
        if (returnFile == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();

            //Save last path
            NbPreferences.forModule(DesktopProjectController.class).put(LAST_PATH, file.getAbsolutePath());

            //File management
            try {
                if (!file.getPath().endsWith(".gephi")) {
                    file = new File(file.getPath() + ".gephi");
                }
                if (!file.exists()) {
                    if (!file.createNewFile()) {
                        String failMsg = NbBundle.getMessage(
                                DesktopProjectController.class,
                                "SaveAsProject_SaveFailed", new Object[]{file.getPath()});
                        JOptionPane.showMessageDialog(null, failMsg);
                        return;
                    }
                } else {
                    String overwriteMsg = NbBundle.getMessage(
                            DesktopProjectController.class,
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
            Project currentProject = projects.getCurrentProject();

            //Save ?
            String messageBundle = NbBundle.getMessage(DesktopProjectController.class, "CloseProject_confirm_message");
            String titleBundle = NbBundle.getMessage(DesktopProjectController.class, "CloseProject_confirm_title");
            String saveBundle = NbBundle.getMessage(DesktopProjectController.class, "CloseProject_confirm_save");
            String doNotSaveBundle = NbBundle.getMessage(DesktopProjectController.class, "CloseProject_confirm_doNotSave");
            String cancelBundle = NbBundle.getMessage(DesktopProjectController.class, "CloseProject_confirm_cancel");
            switch (JOptionPane.showOptionDialog(WindowManager.getDefault().getMainWindow(), messageBundle, titleBundle,
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, new Object[]{saveBundle, doNotSaveBundle, cancelBundle}, saveBundle)) {
                case JOptionPane.YES_OPTION:
                    saveProject(currentProject);
                    break;
                case JOptionPane.NO_OPTION:
                    break;
                default:
                    return;
            }

            //Close
            currentProject.close();
            projects.closeCurrentProject();

            //Actions
            disableAction(SaveProject.class);
            disableAction(SaveAsProject.class);
            disableAction(ProjectProperties.class);

            //Title bar
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
                    String title = frame.getTitle();
                    title = title.substring(0, title.indexOf('-') - 1);
                    frame.setTitle(title);
                }
            });
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
        this.projects = projects;
        projects.refresh();
        if (getCurrentProject() != null) {
            enableAction(SaveAsProject.class);
        }
    }

    public Workspace newWorkspace(Project project) {
        Workspace ws = project.newWorkspace();
        enableAction(SaveProject.class);
        return ws;
    }

    public Workspace importFile() {
        if (projects.getCurrentProject() == null) {
            newProject();
        }
        Workspace ws = newWorkspace(projects.getCurrentProject());
        setCurrentWorkspace(ws);
        return ws;
    }

    public void deleteWorkspace(Workspace workspace) {
        if (getCurrentWorkspace() == workspace) {
            workspace.close();
            getCurrentProject().setCurrentWorkspace(workspace);
        }

        workspace.getProject().removeWorkspace(workspace);
        enableAction(SaveProject.class);
    }

    public void openProject(final Project project) {
        if (projects.hasCurrentProject()) {
            closeCurrentProject();
        }
        projects.addProject(project);
        projects.setCurrentProject(project);
        project.open();
        enableAction(SaveAsProject.class);
        enableAction(ProjectProperties.class);
        enableAction(SaveProject.class);

        //Title bar
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
                String title = frame.getTitle() + " - " + project.getName();
                frame.setTitle(title);
            }
        });
    }

    public Project getCurrentProject() {
        return projects.getCurrentProject();
    }

    public Workspace getCurrentWorkspace() {
        if (projects.hasCurrentProject()) {
            return getCurrentProject().getCurrentWorkspace();
        }
        return null;
    }

    public void setCurrentWorkspace(Workspace workspace) {
        if (getCurrentWorkspace() != null) {
            getCurrentWorkspace().close();
        }
        getCurrentProject().setCurrentWorkspace(workspace);
        workspace.open();
    }

    public void renameProject(Project project, final String name) {
        project.setName(name);

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
        workspace.setName(name);
    }

    public void enableAction(Class clazz) {
        SystemAction action = SystemAction.get(clazz);
        if (action != null) {
            action.setEnabled(true);
        }
    }

    public void disableAction(Class clazz) {
        SystemAction action = SystemAction.get(clazz);

        if (action != null) {
            action.setEnabled(false);
        }
    }
}
