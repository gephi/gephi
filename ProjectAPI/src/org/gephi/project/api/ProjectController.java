/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.project.api;

import org.gephi.project.api.Workspace;
import org.openide.loaders.DataObject;

/**
 *
 * @author Mathieu Bastian
 */
public interface ProjectController {

    public void newProject();
    public void loadProject(DataObject dataObject);
    public void saveProject(DataObject dataObject);
    public void saveProject(Project project);
    public void closeProject(Project project);
    public void closeCurrentProject();
    public void removeProject(Project project);
    public Projects getProjects();
    public void setProjects(Projects projects);
    public Workspace newWorkspace(Project project);
    public void deleteWorkspace(Workspace workspace);
    public void renameWorkspace(Workspace workspace, String name);
    public void openProject(Project project);
    public Project getCurrentProject();
    public void renameProject(Project project, String name);
    public Workspace importFile();
    public Workspace getCurrentWorkspace();
    public void setCurrentWorkspace(Workspace workspace);
}
