package org.gephi.preview.controller;

import org.gephi.preview.GraphImpl;
import org.gephi.preview.NodeImpl;
import org.gephi.preview.supervisor.GraphSupervisor;

/**
 *
 * @author jeremy
 */
public class PreviewGraphFactory {

	public GraphImpl createPreviewGraph(org.gephi.graph.api.Graph sourceGraph, GraphSupervisor supervisor) {
		GraphImpl previewGraph = new GraphImpl(supervisor);

		for (org.gephi.graph.api.Node sourceNode : sourceGraph.getNodes()) {
			org.gephi.graph.api.NodeData sourceNodeData = sourceNode.getNodeData();

			NodeImpl previewNode = new NodeImpl(
					previewGraph,
					sourceNodeData.getLabel(),
					sourceNodeData.x(),
					sourceNodeData.y(),
					sourceNodeData.getRadius(),
					sourceNodeData.r(),
					sourceNodeData.g(),
					sourceNodeData.b());

			previewGraph.addNode(previewNode);
		}

		return previewGraph;
	}
}
