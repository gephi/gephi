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

import java.util.ArrayList;
import java.util.List;
import org.gephi.project.api.Project;
import org.gephi.project.api.WorkspaceProvider;
import org.gephi.project.api.Workspace;
import org.gephi.workspace.impl.WorkspaceImpl;

/**
 *
 * @author Mathieu Bastian
 */
public class WorkspaceProviderImpl implements WorkspaceProvider {

    private transient WorkspaceImpl currentWorkspace;
    private transient Project project;
    private transient List<Workspace> workspaces;

    public WorkspaceProviderImpl(Project project) {
        init(project);
    }

    public void init(Project project) {
        this.project = project;
        workspaces = new ArrayList<Workspace>();
    }

    public WorkspaceImpl newWorkspace() {
        WorkspaceImpl workspace = new WorkspaceImpl(project);
        workspaces.add(workspace);
        return workspace;
    }

    public void addWorkspace(Workspace workspace) {
        workspaces.add(workspace);
    }

    public void removeWorkspace(Workspace workspace) {
        workspaces.remove(workspace);
    }

    public Workspace getPrecedingWorkspace(Workspace workspace) {
        Workspace[] ws = getWorkspaces();
        int index = -1;
        for (int i = 0; i < ws.length; i++) {
            if (ws[i] == workspace) {
                index = i;
            }
        }
        if (index != -1 && index >= 1) {
            //Get preceding
            return ws[index - 1];
        } else if (index == 0 && ws.length > 1) {
            //Get following
            return ws[1];
        }
        return null;
    }

    @Override
    public WorkspaceImpl getCurrentWorkspace() {
        return currentWorkspace;
    }

    @Override
    public Workspace[] getWorkspaces() {
        return workspaces.toArray(new Workspace[0]);
    }

    public void setCurrentWorkspace(Workspace currentWorkspace) {
        this.currentWorkspace = (WorkspaceImpl) currentWorkspace;
    }

    public boolean hasCurrentWorkspace() {
        return currentWorkspace != null;
    }
}
