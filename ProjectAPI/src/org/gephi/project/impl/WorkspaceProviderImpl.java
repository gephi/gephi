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

import org.gephi.project.api.Project;
import org.gephi.project.api.WorkspaceProvider;
import org.gephi.workspace.api.Workspace;
import org.gephi.workspace.impl.WorkspaceImpl;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Mathieu Bastian
 */
public class WorkspaceProviderImpl implements WorkspaceProvider {

    private transient WorkspaceImpl currentWorkspace;
    private transient Project project;
    //Lookup
    private transient InstanceContent instanceContent;
    private transient AbstractLookup lookup;

    public WorkspaceProviderImpl(Project project) {
        init(project);
    }

    public void init(Project project) {
        this.project = project;
        instanceContent = new InstanceContent();
        lookup = new AbstractLookup(instanceContent);
    }

    public WorkspaceImpl newWorkspace() {
        WorkspaceImpl workspace = new WorkspaceImpl(project);
        instanceContent.add(workspace);
        return workspace;
    }

    public void addWorkspace(Workspace workspace) {
        instanceContent.add(workspace);
    }

    public void removeWorkspace(Workspace workspace) {
        instanceContent.remove(workspace);
    }

    public Workspace getPrecedingWorkspace(Workspace workspace) {
        Workspace[] workspaces = getWorkspaces();
        int index = -1;
        for (int i = 0; i < workspaces.length; i++) {
            if (workspaces[i] == workspace) {
                index = i;
            }
        }
        if (index != -1 && index >= 1) {
            //Get preceding
            return workspaces[index - 1];
        } else if (index == 0 && workspaces.length > 1) {
            //Get following
            return workspaces[1];
        }
        return null;
    }

    @Override
    public WorkspaceImpl getCurrentWorkspace() {
        return currentWorkspace;
    }

    public Workspace[] getWorkspaces() {
        return lookup.lookupAll(Workspace.class).toArray(new Workspace[0]);
    }

    public void setCurrentWorkspace(Workspace currentWorkspace) {
        this.currentWorkspace = (WorkspaceImpl) currentWorkspace;
    }

    public boolean hasCurrentWorkspace() {
        return currentWorkspace != null;
    }

    public Lookup getLookup() {
        return lookup;
    }
}
