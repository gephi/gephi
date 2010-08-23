/*
Copyright 2008-2010 Gephi
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
package org.gephi.datalaboratory.impl.manipulators.nodes;

import javax.swing.Icon;
import org.gephi.datalaboratory.api.GraphElementsController;
import org.gephi.datalaboratory.impl.manipulators.nodes.ui.LinkNodesUI;
import org.gephi.datalaboratory.spi.ManipulatorUI;
import org.gephi.datalaboratory.spi.nodes.NodesManipulator;
import org.gephi.graph.api.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Nodes manipulator that links at least 2 different nodes creating edges.
 * Asks the user to select a source node and whether to create directed or undirected edges.
 * It will create edges between the source node and all of the other nodes.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class LinkNodes implements NodesManipulator{
    private Node[] nodes;
    private Node sourceNode;
    private boolean directed=false;//TODO: Maybe keep these values across calls.

    public void setup(Node[] nodes, Node clickedNode) {
        this.nodes=nodes;
        this.sourceNode=clickedNode;//Choos clicked node as source by default (but the user will select it or other in the UI)
    }

    public void execute() {
        GraphElementsController gec = Lookup.getDefault().lookup(GraphElementsController.class);
        gec.createEdges(sourceNode, nodes, directed);
    }

    public String getName() {
        return NbBundle.getMessage(LinkNodes.class, "LinkNodes.name");
    }

    public String getDescription() {
        return NbBundle.getMessage(LinkNodes.class, "LinkNodes.description");
    }

    public boolean canExecute() {
        return nodes.length>1;
    }

    public ManipulatorUI getUI() {
        return new LinkNodesUI();
    }

    public int getType() {
        return 500;
    }

    public int getPosition() {
        return 0;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalaboratory/impl/manipulators/resources/edge.png", true);
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
        this.directed = directed;
    }
}
