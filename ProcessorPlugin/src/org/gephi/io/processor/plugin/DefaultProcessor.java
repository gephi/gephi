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
package org.gephi.io.processor.plugin;

import java.awt.Color;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.io.importer.api.ContainerUnloader;
import org.gephi.io.importer.api.EdgeDraft.EdgeType;
import org.gephi.io.importer.api.EdgeDraftGetter;
import org.gephi.io.importer.api.NodeDraftGetter;
import org.gephi.io.processor.spi.Processor;
import org.gephi.project.api.Workspace;
import org.gephi.timeline.api.TimelineController;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author  Mathieu Bastian
 */
@ServiceProvider(service = Processor.class)
public class DefaultProcessor implements Processor {

    private TimelineController timelineController;
    private Workspace workspace;

    public void process(Workspace workspace, ContainerUnloader container) {
        System.out.println("process " + container.getEdgeDefault());
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        timelineController = Lookup.getDefault().lookup(TimelineController.class);
        this.workspace = workspace;

        HierarchicalGraph graph = null;
        switch (container.getEdgeDefault()) {
            case DIRECTED:
                graph = graphModel.getHierarchicalDirectedGraph();
                break;
            case UNDIRECTED:
                graph = graphModel.getHierarchicalUndirectedGraph();
                break;
            case MIXED:
                graph = graphModel.getHierarchicalMixedGraph();
                break;
            default:
                graph = graphModel.getHierarchicalMixedGraph();
                break;
        }
        GraphFactory factory = graphModel.factory();

        //Attributes - Creates columns for properties
        AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
        attributeModel.mergeModel(container.getAttributeModel());

        int nodeCount = 0;
        //Create all nodes
        for (NodeDraftGetter draftNode : container.getNodes()) {
            Node n = factory.newNode();
            flushToNode(draftNode, n);
            draftNode.setNode(n);
            nodeCount++;
        }

        //Push nodes in data structure
        for (NodeDraftGetter draftNode : container.getNodes()) {
            Node n = draftNode.getNode();
            NodeDraftGetter[] parents = draftNode.getParents();
            if (parents != null) {
                for (int i = 0; i < parents.length; i++) {
                    Node parent = parents[i].getNode();
                    graph.addNode(n, parent);
                }
            } else {
                graph.addNode(n);
            }

            flushToNodeAfter(draftNode, n, graph);
        }

        //Create all edges and push to data structure
        int edgeCount = 0;
        for (EdgeDraftGetter edge : container.getEdges()) {
            Node source = edge.getSource().getNode();
            Node target = edge.getTarget().getNode();
            Edge e = null;
            switch (container.getEdgeDefault()) {
                case DIRECTED:
                    e = factory.newEdge(source, target, edge.getWeight(), true);
                    break;
                case UNDIRECTED:
                    e = factory.newEdge(source, target, edge.getWeight(), false);
                    break;
                case MIXED:
                    e = factory.newEdge(source, target, edge.getWeight(), edge.getType().equals(EdgeType.UNDIRECTED) ? false : true);
                    break;
            }

            flushToEdge(edge, e);
            edgeCount++;
            graph.addEdge(e);
        }

        System.out.println("# Nodes loaded: " + nodeCount + "\n# Edges loaded: " + edgeCount);
        timelineController = null;
        workspace = null;
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

        node.getNodeData().getTextData().setVisible(nodeDraft.isLabelVisible());

        if (nodeDraft.getLabelColor() != null) {
            Color labelColor = nodeDraft.getLabelColor();
            node.getNodeData().getTextData().setColor(labelColor.getRed() / 255f, labelColor.getGreen() / 255f, labelColor.getBlue() / 255f, labelColor.getAlpha() / 255f);
        }

        if (nodeDraft.getLabelSize() != -1f) {
            node.getNodeData().getTextData().setSize(nodeDraft.getLabelSize());
        }

        if (nodeDraft.getX() != 0 && !Float.isNaN(nodeDraft.getX())) {
            node.getNodeData().setX(nodeDraft.getX());
        } else {
            node.getNodeData().setX((float) ((0.01 + Math.random()) * 1000) - 500);
        }
        if (nodeDraft.getY() != 0 && !Float.isNaN(nodeDraft.getY())) {
            node.getNodeData().setY(nodeDraft.getY());
        } else {
            node.getNodeData().setY((float) ((0.01 + Math.random()) * 1000) - 500);
        }

        if (nodeDraft.getZ() != 0 && !Float.isNaN(nodeDraft.getZ())) {
            node.getNodeData().setZ(nodeDraft.getZ());
        }

        if (nodeDraft.getSize() != 0 && !Float.isNaN(nodeDraft.getSize())) {
            node.getNodeData().setSize(nodeDraft.getSize());
        } else {
            node.getNodeData().setSize(10f);
        }

        if (nodeDraft.getId() != null) {
            node.getNodeData().setId(nodeDraft.getId());
        }

        //Dynamic
        if (timelineController != null && nodeDraft.getSlices() != null) {
            for (String[] slice : nodeDraft.getSlices()) {
                String from = slice[0];
                String to = slice[1];
                timelineController.pushSlice(workspace, from, to, node);
            }
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
//        if (!nodeDraft.isVisible()) {
//            graph.setVisible(node, false);
//        }
    }

    private void flushToEdge(EdgeDraftGetter edgeDraft, Edge edge) {
        if (edgeDraft.getColor() != null) {
            edge.getEdgeData().setR(edgeDraft.getColor().getRed() / 255f);
            edge.getEdgeData().setG(edgeDraft.getColor().getGreen() / 255f);
            edge.getEdgeData().setB(edgeDraft.getColor().getBlue() / 255f);
        } else {
            edge.getEdgeData().setR(-1f);
            edge.getEdgeData().setG(-1f);
            edge.getEdgeData().setB(-1f);
        }

        if (edgeDraft.getLabel() != null) {
            edge.getEdgeData().setLabel(edgeDraft.getLabel());
        }
        edge.getEdgeData().getTextData().setVisible(edgeDraft.isLabelVisible());

        if (edgeDraft.getLabelSize() != -1f) {
            edge.getEdgeData().getTextData().setSize(edgeDraft.getLabelSize());
        }

        if (edgeDraft.getLabelColor() != null) {
            Color labelColor = edgeDraft.getLabelColor();
            edge.getEdgeData().getTextData().setColor(labelColor.getRed() / 255f, labelColor.getGreen() / 255f, labelColor.getBlue() / 255f, labelColor.getAlpha() / 255f);
        }

        //Attributes
        if (edge.getEdgeData().getAttributes() != null) {
            AttributeRow row = (AttributeRow) edge.getEdgeData().getAttributes();
            for (AttributeValue val : edgeDraft.getAttributeValues()) {
                row.setValue(val.getColumn(), val.getValue());
            }
        }
    }
}
