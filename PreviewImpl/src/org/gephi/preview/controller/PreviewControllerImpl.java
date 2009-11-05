package org.gephi.preview.controller;

import java.awt.Font;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.preview.GraphImpl;
import org.gephi.preview.api.Graph;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.color.colorizer.GenericColorizer;
import org.gephi.preview.api.color.colorizer.NodeChildColorizer;
import org.gephi.preview.api.color.colorizer.NodeColorizer;
import org.gephi.preview.api.supervisor.EdgeSupervisor;
import org.gephi.preview.api.supervisor.GlobalEdgeSupervisor;
import org.gephi.preview.api.supervisor.NodeSupervisor;
import org.gephi.preview.api.supervisor.SelfLoopSupervisor;
import org.gephi.preview.supervisor.BidirectionalEdgeSupervisorImpl;
import org.gephi.preview.supervisor.EdgeSupervisorImpl;
import org.gephi.preview.supervisor.GlobalEdgeSupervisorImpl;
import org.gephi.preview.supervisor.NodeSupervisorImpl;
import org.gephi.preview.supervisor.SelfLoopSupervisorImpl;
import org.gephi.preview.supervisor.UnidirectionalEdgeSupervisorImpl;
import org.openide.util.Lookup;

/**
 *
 * @author jeremy
 */
public class PreviewControllerImpl implements PreviewController {

    private GraphImpl previewGraph = null;
    private final NodeSupervisorImpl nodeSupervisor = new NodeSupervisorImpl();
    private final GlobalEdgeSupervisorImpl globalEdgeSupervisor = new GlobalEdgeSupervisorImpl();
	private final SelfLoopSupervisorImpl selfLoopSupervisor = new SelfLoopSupervisorImpl();
    private final EdgeSupervisorImpl uniEdgeSupervisor = new UnidirectionalEdgeSupervisorImpl();
    private final EdgeSupervisorImpl biEdgeSupervisor = new BidirectionalEdgeSupervisorImpl();
	private final PreviewGraphFactory factory = new PreviewGraphFactory();

    public Graph getGraph() {
		if (null == previewGraph) {
			buildGraph();
		}

        return previewGraph;
    }

	private void buildGraph() {
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		previewGraph = factory.createPreviewGraph(graphModel.getGraph());
	}

    /**
	 * Returns the node supervisor.
	 *
	 * @return the controller's node supervisor
	 */
	public NodeSupervisor getNodeSupervisor() {
		return nodeSupervisor;
	}

    /**
	 * Returns the global edge supervisor.
	 *
	 * @return the controller's global edge supervisor
	 */
	public GlobalEdgeSupervisor getGlobalEdgeSupervisor() {
		return globalEdgeSupervisor;
	}

	/**
	 * Returns the self-loop supervisor.
	 *
	 * @return the controller's self-loop supervisor
	 */
	public SelfLoopSupervisor getSelfLoopSupervisor() {
		return selfLoopSupervisor;
	}

    /**
	 * Returns the unidirectional edge supervisor.
	 *
	 * @return the controller's unidirectional edge supervisor
	 */
	public EdgeSupervisor getUniEdgeSupervisor() {
		return uniEdgeSupervisor;
	}

    /**
	 * Returns the bidirectional edge supervisor.
	 *
	 * @return the controller's bidirectional edge supervisor
	 */
	public EdgeSupervisor getBiEdgeSupervisor() {
		return biEdgeSupervisor;
	}
}
