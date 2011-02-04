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

import javax.swing.Icon;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.desktop.project.api.ProjectControllerUI;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceInformation;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class CopyOrMoveToWorkspaceSubItem implements GraphContextMenuItem {

    private Workspace workspace;
    private boolean canExecute;
    private int type;
    private int position;
    private HierarchicalGraph graph;
    private Node[] nodes;
    private final boolean copy;

    /**
     * Constructor with copy or move settings
     * @param workspace Workspace to copy or move, or null to use new workspace
     * @param canExecute canExecute
     * @param type type
     * @param position position
     * @param copy True to copy, false to move
     */
    public CopyOrMoveToWorkspaceSubItem(Workspace workspace, boolean canExecute, int type, int position, boolean copy) {
        this.workspace = workspace;
        this.canExecute = canExecute;
        this.type = type;
        this.position = position;
        this.copy = copy;
    }

    public void setup(HierarchicalGraph graph, Node[] nodes) {
        this.graph = graph;
        this.nodes = nodes;
    }

    public void execute() {
        if (workspace == null) {
            workspace = Lookup.getDefault().lookup(ProjectControllerUI.class).newWorkspace();
        }
        if (copy) {
            copyToWorkspace(workspace);
        } else {
            moveToWorkspace(workspace);
        }
    }

    public GraphContextMenuItem[] getSubItems() {
        return null;
    }

    public String getName() {
        if (workspace != null) {
            return workspace.getLookup().lookup(WorkspaceInformation.class).getName();
        } else {
            return NbBundle.getMessage(CopyOrMoveToWorkspaceSubItem.class, copy ? "GraphContextMenu_CopyToWorkspace_NewWorkspace" : "GraphContextMenu_MoveToWorkspace_NewWorkspace");
        }
    }

    public String getDescription() {
        return null;
    }

    public boolean isAvailable() {
        return true;
    }

    public boolean canExecute() {
        return canExecute;
    }

    public int getType() {
        return type;
    }

    public int getPosition() {
        return position;
    }

    public Icon getIcon() {
        return null;
    }

    public void copyToWorkspace(Workspace workspace) {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);

        Workspace currentWorkspace = projectController.getCurrentWorkspace();
        AttributeModel sourceAttributeModel = attributeController.getModel(currentWorkspace);
        AttributeModel destAttributeModel = attributeController.getModel(workspace);
        destAttributeModel.mergeModel(sourceAttributeModel);

        GraphModel sourceModel = graphController.getModel(currentWorkspace);
        GraphModel destModel = graphController.getModel(workspace);
        Graph destGraph = destModel.getHierarchicalGraphVisible();
        Graph sourceGraph = sourceModel.getHierarchicalGraphVisible();

        destModel.pushNodes(sourceGraph, nodes);
    }

    public void moveToWorkspace(Workspace workspace) {
        copyToWorkspace(workspace);
        delete();
    }

    public void delete() {
        for (Node n : nodes) {
            graph.removeNode(n);
        }
    }

    public Integer getMnemonicKey() {
        return null;
    }
}
