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
package org.gephi.project.api;

import java.io.File;

/**
 * Project controller, manage projects and workspaces states.
 * <p>
 * This controller is a service and can therefore be found in Lookup:
 * <pre>ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);</pre>
 * @author Mathieu Bastian
 * @see Project
 * @see Workspace
 */
public interface ProjectController {

    public void startup();

    public void newProject();

    public Runnable openProject(File file);

    public Runnable saveProject(Project project);

    public Runnable saveProject(Project project, File file);

    public void closeCurrentProject();

    public void removeProject(Project project);

    public Projects getProjects();

    public void setProjects(Projects projects);

    public Workspace newWorkspace(Project project);

    public void deleteWorkspace(Workspace workspace);

    public void renameWorkspace(Workspace workspace, String name);

    public Project getCurrentProject();

    public void renameProject(Project project, String name);

    public Workspace getCurrentWorkspace();

    public void openWorkspace(Workspace workspace);

    public void closeCurrentWorkspace();

    public void cleanWorkspace(Workspace workspace);

    public Workspace duplicateWorkspace(Workspace workspace);

    public void setSource(Workspace workspace, String source);

    public void addWorkspaceListener(WorkspaceListener workspaceListener);

    public void removeWorkspaceListener(WorkspaceListener workspaceListener);
}
