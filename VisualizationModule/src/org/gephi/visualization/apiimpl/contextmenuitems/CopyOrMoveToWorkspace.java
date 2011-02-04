/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.visualization.apiimpl.contextmenuitems;

import java.util.ArrayList;
import javax.swing.Icon;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceProvider;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.util.Lookup;

/**
 *
 */
public abstract class CopyOrMoveToWorkspace implements GraphContextMenuItem {

    private Node[] nodes;

    public void setup(HierarchicalGraph graph, Node[] nodes) {
        this.nodes = nodes;
    }

    public void execute() {
    }

    public GraphContextMenuItem[] getSubItems() {
        if (nodes != null) {
            int i = 0;
            ArrayList<GraphContextMenuItem> subItems = new ArrayList<GraphContextMenuItem>();
            if (canExecute()) {
                subItems.add(new CopyOrMoveToWorkspaceSubItem(null, true, 0, 0, isCopy()));//New workspace
                ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
                for (final Workspace w : projectController.getCurrentProject().getLookup().lookup(WorkspaceProvider.class).getWorkspaces()) {
                    GraphContextMenuItem item = new CopyOrMoveToWorkspaceSubItem(w, w != projectController.getCurrentWorkspace(), 1, i, isCopy());
                    subItems.add(item);
                    i++;
                }
                return subItems.toArray(new GraphContextMenuItem[0]);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public String getDescription() {
        return null;
    }

    public boolean isAvailable() {
        return true;
    }

    public boolean canExecute() {
        return nodes.length > 0;
    }

    public int getType() {
        return 200;
    }

    public Icon getIcon() {
        return null;
    }

    public Integer getMnemonicKey() {
        return null;
    }

    protected abstract boolean isCopy();
}
