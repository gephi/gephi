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

    public void saveAsProject(Project project);

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
