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

import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Projects;
import org.gephi.project.api.Workspace;
import org.openide.util.actions.SystemAction;
import org.gephi.branding.desktop.actions.SaveProject;
import org.gephi.project.api.Project.Status;
import org.gephi.project.filetype.GephiDataObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author Mathieu
 */
public class DesktopProjectController implements ProjectController {

    private Projects projects = new Projects();

    public DesktopProjectController()
    {
        //Actions
        disableAction(SaveProject.class);
    }

    public void newProject() {
       projects.addProject(new Project());
    }

    public void loadProject(DataObject dataObject)
    {
         GephiDataObject gephiDataObject = (GephiDataObject)dataObject;
         Project project = gephiDataObject.load();
         if(project!=null)
         {
             projects.addProject(project);
         }
    }

    public void closeProject(Project project) {
        project.setStatus(Status.CLOSED);
        disableAction(SaveProject.class);
    }

    public void removeProject(Project project)
    {
        if(projects.getCurrentProject()==project)
        {
            closeProject(project);
        }
        projects.removeProject(project);
    }

    public Projects getProjects() {
        return projects;
    }

    public void setProjects(Projects projects)
    {
        this.projects = projects;
    }

    public void newWorkspace(Project project) {
        project.newWorkspace();
        enableAction(SaveProject.class);
    }

    public void deleteWorkspace(Workspace workspace) {
        workspace.getProject().removeWorkspace(workspace);
    }

    public void openProject(Project project) {
        if(projects.getCurrentProject()!=null)
        {
            closeProject(projects.getCurrentProject());
        }
        projects.setCurrentProject(project);
    }

    public Project getCurrentProject() {
        return projects.getCurrentProject();
    }

    public void enableAction(Class clazz)
    {
        SystemAction action = SystemAction.get(clazz);
        if(action!=null)
            action.setEnabled(true);
    }

    public void disableAction(Class clazz)
    {
        SystemAction action = SystemAction.get(clazz);
        if(action!=null)
            action.setEnabled(false);
    }

    public void renameProject(Project project, String name) {
        project.setName(name);
    }
}
