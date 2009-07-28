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

import javax.swing.JFrame;
import javax.swing.JOptionPane;
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
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author Mathieu Bastian
 */
public class DesktopProjectController implements ProjectController {

    private Projects projects = new ProjectsImpl();
    private static boolean avoidDisableSaveProject = true;

    public DesktopProjectController() {
        //Actions
        disableAction(SaveProject.class);
        disableAction(SaveAsProject.class);
        disableAction(ProjectProperties.class);
    }

    public void newProject() {
        closeCurrentProject();
        ProjectImpl project = new ProjectImpl();
        projects.addProject(project);
        openProject(project);
    }

    public void loadProject(DataObject dataObject) {
        GephiDataObject gephiDataObject = (GephiDataObject) dataObject;
        Project project = gephiDataObject.load();
        if (project != null) {
            projects.addProject(project);
            enableAction(SaveAsProject.class);
        }
    }

    public void saveProject(DataObject dataObject) {
        GephiDataObject gephiDataObject = (GephiDataObject) dataObject;
        Project project = getCurrentProject();
        project.setDataObject(gephiDataObject);
        gephiDataObject.setProject(project);
        gephiDataObject.save();

        disableAction(SaveProject.class);
    }

    public void saveProject(Project project) {
        ((GephiDataObject) project.getDataObject()).save();
        disableAction(SaveProject.class);
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
            JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
            String title = frame.getTitle();
            title = title.substring(0, title.indexOf('-') - 1);
            frame.setTitle(title);
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

    public void openProject(Project project) {
        if (projects.hasCurrentProject()) {
            closeCurrentProject();
        }
        projects.setCurrentProject(project);
        project.open();
        enableAction(SaveAsProject.class);
        enableAction(ProjectProperties.class);

        //Title bar
        JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
        String title = frame.getTitle() + " - " + project.getName();
        frame.setTitle(title);
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
        enableAction(SaveProject.class);
    }

    public void renameProject(Project project, String name) {
        project.setName(name);
        enableAction(SaveProject.class);

        //Title bar
        JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
        String title = frame.getTitle();
        title = title.substring(0, title.indexOf('-') - 1);
        title += " - " + name;
        frame.setTitle(title);
    }

    public void renameWorkspace(Workspace workspace, String name) {
        workspace.setName(name);
        enableAction(SaveProject.class);
    }

    public void enableAction(Class clazz) {
        SystemAction action = SystemAction.get(clazz);
        if (action != null) {
            action.setEnabled(true);
        }
    }

    public void disableAction(Class clazz) {
        SystemAction action = SystemAction.get(clazz);

        if (avoidDisableSaveProject && clazz.equals(SaveProject.class)) {
            return;
        }

        if (action != null) {
            action.setEnabled(false);
        }
    }
}
