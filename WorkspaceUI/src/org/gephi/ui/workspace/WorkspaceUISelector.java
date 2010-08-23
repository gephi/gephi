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
package org.gephi.ui.workspace;

import java.awt.Component;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.openide.awt.StatusLineElementProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = org.openide.awt.StatusLineElementProvider.class, position = -100)
public class WorkspaceUISelector implements StatusLineElementProvider, WorkspaceListener {

    private WorkspaceUISelectorPanel panel;

    public WorkspaceUISelector() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.addWorkspaceListener(this);
    }

    public Component getStatusLineElement() {
        panel = new WorkspaceUISelectorPanel();
        return panel;
    }

    public void initialize(Workspace workspace) {
        panel.refreshList();
    }

    public void select(Workspace workspace) {
        panel.setSelectedWorkspace(workspace);
    }

    public void unselect(Workspace workspace) {
    }

    public void close(Workspace workspace) {
        panel.refreshList();
    }

    public void disable() {
        panel.noSelectedWorkspace();
    }
}
