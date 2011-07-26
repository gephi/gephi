/*
Copyright 2008-2011 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalab.plugin.manipulators.nodes;

import javax.swing.Icon;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.datalab.api.GraphElementsController;
import org.gephi.datalab.api.datatables.DataTablesController;
import org.gephi.datalab.plugin.manipulators.nodes.ui.MergeNodesUI;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.rows.merge.AttributeRowsMergeStrategy;
import org.gephi.graph.api.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * <p>Nodes manipulator that merges 2 or more nodes</p>
 * <p>
 * The behaviour is:
 * <ul>
 * <li>Merged nodes are deleted if desired, and one new node is created</li>
 * <li>Edges of all the nodes are assigned to the new node</li>
 * <li>Each column uses an strategy to reduce the rows values to one value</li>
 * <li></li>
 * </ul>
 * </p>
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class MergeNodes extends BasicNodesManipulator {

    public static final String DELETE_MERGED_NODES_SAVED_PREFERENCES = "MergeNodes_DeleteMergedNodes";
    private Node[] nodes;
    private Node selectedNode;
    private AttributeColumn[] columns;
    private AttributeRowsMergeStrategy[] mergeStrategies;
    private boolean deleteMergedNodes;

    public void setup(Node[] nodes, Node clickedNode) {
        this.nodes = nodes;
        selectedNode = clickedNode != null ? clickedNode : nodes[0];
        columns = Lookup.getDefault().lookup(AttributeController.class).getModel().getNodeTable().getColumns();
        mergeStrategies = new AttributeRowsMergeStrategy[columns.length];
        deleteMergedNodes = NbPreferences.forModule(MergeNodes.class).getBoolean(DELETE_MERGED_NODES_SAVED_PREFERENCES, true);
    }

    public void execute() {
        GraphElementsController gec = Lookup.getDefault().lookup(GraphElementsController.class);
        Node newNode=gec.mergeNodes(nodes, selectedNode, mergeStrategies, deleteMergedNodes);
        Lookup.getDefault().lookup(DataTablesController.class).setNodeTableSelection(new Node[]{newNode});
        NbPreferences.forModule(MergeNodes.class).putBoolean(DELETE_MERGED_NODES_SAVED_PREFERENCES, deleteMergedNodes);
    }

    public String getName() {
        return NbBundle.getMessage(MergeNodes.class, "MergeNodes.name");
    }

    public String getDescription() {
        return NbBundle.getMessage(MergeNodes.class, "MergeNodes.description");
    }

    public boolean canExecute() {
        return nodes.length > 1;
    }

    public ManipulatorUI getUI() {
        return new MergeNodesUI();
    }

    public int getType() {
        return 500;
    }

    public int getPosition() {
        return 0;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalab/plugin/manipulators/resources/merge.png", true);
    }

    public boolean isDeleteMergedNodes() {
        return deleteMergedNodes;
    }

    public void setDeleteMergedNodes(boolean deleteMergedNodes) {
        this.deleteMergedNodes = deleteMergedNodes;
    }

    public Node[] getNodes() {
        return nodes;
    }

    public Node getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(Node selectedNode) {
        this.selectedNode = selectedNode;
    }

    public AttributeColumn[] getColumns() {
        return columns;
    }

    public AttributeRowsMergeStrategy[] getMergeStrategies() {
        return mergeStrategies;
    }

    public void setMergeStrategies(AttributeRowsMergeStrategy[] mergeStrategies) {
        this.mergeStrategies = mergeStrategies;
    }
}
