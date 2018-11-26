/*
Copyright 2008-2011 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.datalab.plugin.manipulators.nodes;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import org.gephi.datalab.api.GraphElementsController;
import org.gephi.datalab.api.datatables.DataTablesController;
import org.gephi.datalab.plugin.manipulators.nodes.ui.MergeNodesUI;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.rows.merge.AttributeRowsMergeStrategy;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * <p>Nodes manipulator that merges 2 or more nodes</p>
 * The behaviour is:
 * <ul>
 * <li>Merged nodes are deleted if desired, and one new node is created</li>
 * <li>Edges of all the nodes are assigned to the new node</li>
 * <li>Each column uses an strategy to reduce the rows values to one value</li>
 * <li></li>
 * </ul>
 * @author Eduardo Ramos
 */
public class MergeNodes extends BasicNodesManipulator {

    public static final String DELETE_MERGED_NODES_SAVED_PREFERENCES = "MergeNodes_DeleteMergedNodes";
    private Node[] nodes;
    private Node selectedNode;
    private Column[] columns;
    private AttributeRowsMergeStrategy[] mergeStrategies;
    private boolean deleteMergedNodes;

    @Override
    public void setup(Node[] nodes, Node clickedNode) {
        this.nodes = nodes;
        selectedNode = clickedNode != null ? clickedNode : nodes[0];
        
        Table nodeTable = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getNodeTable();
        List<Column> columnsList = new ArrayList<>();
        for (Column column : nodeTable) {
            if(!column.isReadOnly()){
                columnsList.add(column);
            }
        }
        
        columns = columnsList.toArray(new Column[0]);
        
        mergeStrategies = new AttributeRowsMergeStrategy[columns.length];
        deleteMergedNodes = NbPreferences.forModule(MergeNodes.class).getBoolean(DELETE_MERGED_NODES_SAVED_PREFERENCES, true);
    }

    @Override
    public void execute() {
        Graph graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
        
        GraphElementsController gec = Lookup.getDefault().lookup(GraphElementsController.class);
        Node newNode = gec.mergeNodes(graph, nodes, selectedNode, columns, mergeStrategies, deleteMergedNodes);
        Lookup.getDefault().lookup(DataTablesController.class).setNodeTableSelection(new Node[]{newNode});
        NbPreferences.forModule(MergeNodes.class).putBoolean(DELETE_MERGED_NODES_SAVED_PREFERENCES, deleteMergedNodes);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(MergeNodes.class, "MergeNodes.name");
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(MergeNodes.class, "MergeNodes.description");
    }

    @Override
    public boolean canExecute() {
        return nodes.length > 1;
    }

    @Override
    public ManipulatorUI getUI() {
        return new MergeNodesUI();
    }

    @Override
    public int getType() {
        return 500;
    }

    @Override
    public int getPosition() {
        return 0;
    }

    @Override
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

    public Column[] getColumns() {
        return columns;
    }

    public AttributeRowsMergeStrategy[] getMergeStrategies() {
        return mergeStrategies;
    }

    public void setMergeStrategies(AttributeRowsMergeStrategy[] mergeStrategies) {
        this.mergeStrategies = mergeStrategies;
    }
}
