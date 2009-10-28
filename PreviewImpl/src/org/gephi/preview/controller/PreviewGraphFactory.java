package org.gephi.preview.controller;

import java.util.HashMap;
import org.gephi.preview.GraphImpl;
import org.gephi.preview.NodeImpl;
import org.gephi.preview.supervisor.GraphSupervisor;

/**
 *
 * @author jeremy
 */
public class PreviewGraphFactory {

	private final HashMap<org.gephi.graph.api.Node, NodeImpl> nodeMap = new HashMap<org.gephi.graph.api.Node, NodeImpl>();

	public GraphImpl createPreviewGraph(org.gephi.graph.api.Graph sourceGraph, GraphSupervisor supervisor) {
		GraphImpl previewGraph = new GraphImpl(supervisor);

		for (org.gephi.graph.api.Node sourceNode : sourceGraph.getNodes()) {
			NodeImpl previewNode = createPreviewNode(previewGraph, sourceNode);
			previewGraph.addNode(previewNode);
		}

//		for (org.gephi.graph.api.Edge sourceEdge : sourceGraph.getEdges()) {
//			if (sourceEdge.isVisible()) {
//				if (sourceEdge.isSelfLoop()) {
//					// create self-loop
//					// add self-loop
//				}
//				else {
//					// add edge
//				}
//			}
//		}

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

		nodeMap.put(sourceNode, previewNode);

		return previewNode;
	}
}
