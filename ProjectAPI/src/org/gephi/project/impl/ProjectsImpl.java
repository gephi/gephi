/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.project.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.gephi.project.api.Project;
import org.gephi.project.api.Projects;

/**
 *
 * @author Mathieu Bastian
 */
public class ProjectsImpl implements Projects, Serializable {

    //Project
    private List<Project> projects = new ArrayList<Project>();
    private ProjectImpl currentProject;

    public ProjectsImpl() {
    }

    public void addProject(Project project) {
        if (!projects.contains(project)) {
            projects.add(project);
        }
    }

    public void removeProject(Project project) {
        projects.remove(project);
    }

    public Project[] getProjects() {
        return projects.toArray(new Project[0]);
    }

    public ProjectImpl getCurrentProject() {
        return currentProject;
    }

    public boolean hasCurrentProject() {
        return currentProject != null;
    }

    public void setCurrentProject(ProjectImpl currentProject) {
        this.currentProject = currentProject;
    }

    public void closeCurrentProject() {
        this.currentProject = null;
    }
}
