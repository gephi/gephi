/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.processor.plugin;

import java.util.HashMap;
import java.util.Map;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.properties.PropertiesColumn;
import org.gephi.graph.api.Edge;
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
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.timeline.api.TimelineController;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = Processor.class)
public class AppendProcessor extends AbstractProcessor implements Processor {

    public String getDisplayName() {
        return "Append graph";
    }

    public void process(ContainerUnloader container, Workspace workspace) {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        //Workspace
        if (workspace == null) {
            workspace = pc.getCurrentWorkspace();
            if (workspace == null) {
                //Append mode but no workspace
                workspace = pc.newWorkspace(pc.getCurrentProject());
                pc.openWorkspace(workspace);
            }
        }
        if (container.getSource() != null) {
            pc.setSource(workspace, container.getSource());
        }

        //Architecture
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        this.timelineController = Lookup.getDefault().lookup(TimelineController.class);

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

        //Dynamic
        if (timelineController != null) {
            timelineController.setMin(workspace, container.getTimeIntervalMin());
            timelineController.setMax(workspace, container.getTimeIntervalMax());
        }

        //Attributes - Creates columns for properties
        AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
        attributeModel.mergeModel(container.getAttributeModel());

        //Index existing graph
        Map<String, Node> map = new HashMap<String, Node>();
        for (Node n : graph.getNodes()) {
            Object id = n.getNodeData().getAttributes().getValue(PropertiesColumn.NODE_ID.getIndex());
            if (id != null) {
                map.put((String) id, n);
            }
            if (n.getNodeData().getLabel() != null && !n.getNodeData().getLabel().isEmpty()) {
                map.put(n.getNodeData().getLabel(), n);
            }
        }

        int nodeCount = 0;
        //Create all nodes
        for (NodeDraftGetter draftNode : container.getNodes()) {
            Node n;
            String id = draftNode.getId();
            if (id != null && map.get(id) != null) {
                n = map.get(id);
            } else {
                n = factory.newNode();
            }
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
        }

        //Create all edges and push to data structure
        int edgeCount = 0;
        for (EdgeDraftGetter edge : container.getEdges()) {
            Node source = edge.getSource().getNode();
            Node target = edge.getTarget().getNode();
            Edge e = null;
            switch (container.getEdgeDefault()) {
                case DIRECTED:
                    e = factory.newEdge(edge.getId(), source, target, edge.getWeight(), true);
                    break;
                case UNDIRECTED:
                    e = factory.newEdge(edge.getId(), source, target, edge.getWeight(), false);
                    break;
                case MIXED:
                    e = factory.newEdge(edge.getId(), source, target, edge.getWeight(), edge.getType().equals(EdgeType.UNDIRECTED) ? false : true);
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
}
