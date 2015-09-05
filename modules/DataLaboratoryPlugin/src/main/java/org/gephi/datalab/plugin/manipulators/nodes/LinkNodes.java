/*
 Copyright 2008-2010 Gephi
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

import javax.swing.Icon;
import org.gephi.datalab.api.DataLaboratoryHelper;
import org.gephi.datalab.api.GraphElementsController;
import org.gephi.datalab.plugin.manipulators.general.AddEdgeToGraph;
import org.gephi.datalab.plugin.manipulators.nodes.ui.LinkNodesUI;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Nodes manipulator that links at least 2 different nodes creating edges. Asks the user to select a source node and whether to create directed or undirected edges. It will create edges between the
 * source node and all of the other nodes.
 *
 * @author Eduardo Ramos
 */
public class LinkNodes extends BasicNodesManipulator {

    private Node[] nodes;
    private Node sourceNode;
    private static boolean directed;
    private static GraphModel graphModel;

    @Override
    public void setup(Node[] nodes, Node clickedNode) {
        this.nodes = nodes;
        this.sourceNode = clickedNode;//Choose clicked node as source by default (but the user can select it or other one in the UI)

        GraphModel currentGraphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        if (graphModel != currentGraphModel) {//If graph model has changed since last execution, change default mode for edges to create in UI, else keep this parameter across calls
            directed = currentGraphModel.isDirected() || currentGraphModel.isMixed();//Get graph directed state. Set to true if graph is directed or mixed
            graphModel = currentGraphModel;
        }
    }

    @Override
    public void execute() {
        if (nodes.length > 1) {
            GraphElementsController gec = Lookup.getDefault().lookup(GraphElementsController.class);
            gec.createEdges(sourceNode, nodes, directed);
        }else{
            AddEdgeToGraph manipulator = new AddEdgeToGraph();
            manipulator.setSource(sourceNode);
            DataLaboratoryHelper.getDefault().executeManipulator(manipulator);
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(LinkNodes.class, "LinkNodes.name");
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(LinkNodes.class, "LinkNodes.description");
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    @Override
    public ManipulatorUI getUI() {
        return nodes.length > 1 ? new LinkNodesUI() : null;//Use link nodes UI if more than one node selected, otherwise add edge to graph action will be called in execute.
    }

    @Override
    public int getType() {
        return 500;
    }

    @Override
    public int getPosition() {
        return 100;
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalab/plugin/manipulators/resources/edge.png", true);
    }

    public Node[] getNodes() {
        return nodes;
    }

    public Node getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(Node sourceNode) {
        this.sourceNode = sourceNode;
    }

    public boolean isDirected() {
        return directed;
    }

    public void setDirected(boolean directed) {
        LinkNodes.directed = directed;
    }
}
