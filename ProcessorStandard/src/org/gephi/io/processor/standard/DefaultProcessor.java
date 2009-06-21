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

package org.gephi.io.processor.standard;

import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.Node;
import org.gephi.io.container.ContainerUnloader;
import org.gephi.io.container.EdgeDraft;
import org.gephi.io.processor.EdgeDraftGetter;
import org.gephi.io.processor.NodeDraftGetter;
import org.gephi.io.processor.Processor;
import org.openide.util.Lookup;

/**
 *
 * @author  Mathieu Bastian
 */
public class DefaultProcessor implements Processor {

    public void process(ContainerUnloader container) {

        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        Graph graph = graphController.getDirectedGraph();
        GraphFactory factory = graphController.factory();

        int nodeCount=0;
        for (NodeDraftGetter node : container.getNodes()) {
            Node n = factory.newNode();
            flushToNode(node, n);
            node.setNode(n);
            nodeCount++;
            graph.addNode(n);
            flushToNodeAfter(node, n, graph);
        }

        int edgeCount=0;
        for (EdgeDraftGetter edge : container.getEdges()) {
            Node source = edge.getSource().getNode();
            Node target = edge.getTarget().getNode();

            Edge e = factory.newEdge(source, target);
            flushToEdge(edge, e);
            edgeCount++;
            graph.addEdge(e);
        }

        System.out.println("# Nodes loaded: "+nodeCount+"\n# Edges loaded: "+edgeCount);
    }

    private void flushToNode(NodeDraftGetter nodeDraft, Node node) {

        if (nodeDraft.getColor() != null) {
            node.getNodeData().setR(nodeDraft.getColor().getRed() / 255f);
            node.getNodeData().setG(nodeDraft.getColor().getGreen() / 255f);
            node.getNodeData().setB(nodeDraft.getColor().getBlue() / 255f);
        }

        if (nodeDraft.getLabel() != null) {
            node.getNodeData().setLabel(nodeDraft.getLabel());
        }

        if (nodeDraft.getX() != 0) {
            node.getNodeData().setX(nodeDraft.getX());
        } else {
            node.getNodeData().setX((float)((0.01+Math.random())*1000)-500);
        }
        if (nodeDraft.getY() != 0) {
            node.getNodeData().setY(nodeDraft.getY());
        } else {
            node.getNodeData().setY((float)((0.01+Math.random())*1000)-500);
        }

        if (nodeDraft.getZ() != 0) {
            node.getNodeData().setZ(nodeDraft.getZ());
        }

        if (nodeDraft.getSize() != 0) {
            node.getNodeData().setSize(nodeDraft.getSize());
        }

        //Attributes
        if (node.getNodeData().getAttributes() != null) {
            AttributeRow row = (AttributeRow) node.getNodeData().getAttributes();
            for (AttributeValue val : nodeDraft.getAttributeValues()) {
                row.setValue(val.getColumn(), val.getValue());
            }
        }
    }

    private void flushToNodeAfter(NodeDraftGetter nodeDraft, Node node, Graph graph) {
        if(!nodeDraft.isVisible()) {
            graph.setVisible(node, false);
        }
    }

    private void flushToEdge(EdgeDraft edgeDraft, Edge edge) {
    }
}


