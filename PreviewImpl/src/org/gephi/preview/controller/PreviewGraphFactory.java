package org.gephi.preview.controller;

import java.util.HashMap;
import org.gephi.preview.GraphImpl;
import org.gephi.preview.NodeImpl;
import org.gephi.preview.SelfLoopImpl;
import org.gephi.preview.UnidirectionalEdgeImpl;
import org.gephi.preview.supervisor.GraphSupervisor;

/**
 *
 * @author jeremy
 */
public class PreviewGraphFactory {

	private final HashMap<Integer, NodeImpl> nodeMap = new HashMap<Integer, NodeImpl>();

	public GraphImpl createPreviewGraph(org.gephi.graph.api.Graph sourceGraph, GraphSupervisor supervisor) {
		GraphImpl previewGraph = new GraphImpl(supervisor);

		for (org.gephi.graph.api.Node sourceNode : sourceGraph.getNodes()) {
			NodeImpl previewNode = createPreviewNode(previewGraph, sourceNode);
			previewGraph.addNode(previewNode);
		}

		for (org.gephi.graph.api.Edge sourceEdge : sourceGraph.getEdges()) {
            if (sourceEdge.isSelfLoop()) {
                SelfLoopImpl previewSelfLoop = createPreviewSelfLoop(previewGraph, sourceEdge);
                previewGraph.addSelfLoop(previewSelfLoop);
            }
            else {
                UnidirectionalEdgeImpl previewUniEdge = createPreviewUnidirectionalEdge(previewGraph, sourceEdge);
                previewGraph.addUnidirectionalEdge(previewUniEdge);
            }
		}

		return previewGraph;
	}

	private NodeImpl createPreviewNode(GraphImpl parent, org.gephi.graph.api.Node sourceNode) {
		org.gephi.graph.api.NodeData sourceNodeData = sourceNode.getNodeData();

		NodeImpl previewNode = new NodeImpl(
				parent,
				sourceNodeData.getLabel(),
				sourceNodeData.x(),
				sourceNodeData.y(),
				sourceNodeData.getRadius(),
				sourceNodeData.r(),
				sourceNodeData.g(),
				sourceNodeData.b());

		nodeMap.put(sourceNode.getId(), previewNode);

		return previewNode;
	}

    private SelfLoopImpl createPreviewSelfLoop(GraphImpl parent, org.gephi.graph.api.Edge sourceSelfLoop) {
		org.gephi.graph.api.EdgeData sourceEdgeData = sourceSelfLoop.getEdgeData();

		SelfLoopImpl previewSelfLoop = new SelfLoopImpl(
				parent,
				sourceEdgeData.getSize(),
                0,
				nodeMap.get(sourceSelfLoop.getSource().getId()));

		return previewSelfLoop;
	}

    private UnidirectionalEdgeImpl createPreviewUnidirectionalEdge(GraphImpl parent, org.gephi.graph.api.Edge sourceEdge) {
		org.gephi.graph.api.EdgeData sourceEdgeData = sourceEdge.getEdgeData();

		UnidirectionalEdgeImpl previewUniEdge = new UnidirectionalEdgeImpl(
				parent,
				sourceEdgeData.getSize(),
                0,
				nodeMap.get(sourceEdge.getSource().getId()),
                nodeMap.get(sourceEdge.getTarget().getId()),
                sourceEdgeData.getLabel());

		return previewUniEdge;
	}
}
