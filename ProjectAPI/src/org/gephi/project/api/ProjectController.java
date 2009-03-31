/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.project.api;

import org.gephi.project.api.Workspace;

/**
 *
 * @author Mathieu
 */
public interface ProjectController {

    public void newProject();
    public void closeProject(Project project);
    public void removeProject(Project project);
    public Projects getProjects();
    public void setProjects(Projects projects);
    public void newWorkspace(Project project);
    public void deleteWorkspace(Workspace workspace);
    public void openProject(Project project);
    public Project getCurrentProject();
    public void renameProject(Project project, String name);
}
