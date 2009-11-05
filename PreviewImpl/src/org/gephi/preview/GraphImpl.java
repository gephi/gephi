package org.gephi.preview;

import java.util.ArrayList;
import java.util.Iterator;
import org.gephi.preview.api.BidirectionalEdge;
import org.gephi.preview.api.Graph;
import org.gephi.preview.api.Node;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.SelfLoop;
import org.gephi.preview.api.UnidirectionalEdge;
import org.gephi.preview.supervisor.GraphSupervisor;
import org.openide.util.Lookup;
import processing.core.PVector;

/**
 *
 * @author jeremy
 */
public class GraphImpl implements Graph {

    private final GraphSupervisor supervisor;
    private final ArrayList<Node> m_nodes = new ArrayList<Node>();
	private final ArrayList<SelfLoop> selfLoops = new ArrayList<SelfLoop>();
    private final ArrayList<UnidirectionalEdge> uniEdges = new ArrayList<UnidirectionalEdge>();
    private final ArrayList<BidirectionalEdge> biEdges = new ArrayList<BidirectionalEdge>();

    private final PVector m_minPos = new PVector();
    private final PVector m_maxPos =  new PVector();

    public GraphImpl(GraphSupervisor supervisor) {
        this.supervisor = supervisor;
    }

    public GraphSupervisor getSupervisor() {
        return supervisor;
    }

    public final Iterator<Node> getNodes() {
        return m_nodes.iterator();
    }

	/**
	 * Returns an iterator on the graph's self-loops.
	 *
	 * @return an iterator on the graph's self-loops
	 */
	public Iterator<SelfLoop> getSelfLoops() {
		return selfLoops.iterator();
	}

    public Iterator<UnidirectionalEdge> getUnidirectionalEdges() {
		return uniEdges.iterator();
	}

    public Iterator<BidirectionalEdge> getBidirectionalEdges() {
		return biEdges.iterator();
	}

    public final PVector getMinPos() {
        return m_minPos;
    }

    public final PVector getMaxPos() {
        return m_maxPos;
    }

    public void addNode(NodeImpl node) {
        m_nodes.add(node);

        // update graph's bounding box
        {
            PVector topLeftPos = node.getTopLeftPosition();
            PVector bottomRightPos = node.getBottomRightPosition();

            if (m_nodes.size() == 1) {
                // first initialization
                m_minPos.set(topLeftPos);
                m_maxPos.set(bottomRightPos);
            } else {
                if (topLeftPos.x < m_minPos.x) {
                    m_minPos.x = topLeftPos.x;

                }
                if (topLeftPos.y < m_minPos.y) {
                    m_minPos.y = topLeftPos.y;

                }
                if (bottomRightPos.x > m_maxPos.x) {
                    m_maxPos.x = bottomRightPos.x;

                }
                if (bottomRightPos.y > m_maxPos.y) {
                    m_maxPos.y = bottomRightPos.y;

                }
            }
        }
    }

	/**
	 * Adds the given self-loop to the graph.
	 *
	 * @param selfLoop  the self-loop to add to the graph
	 */
	public void addSelfLoop(SelfLoop selfLoop) {
        selfLoops.add(selfLoop);
    }

    /**
	 * Adds the given unidirectional edge to the graph.
	 *
	 * @param edge  the unidirectional edge to add to the graph
	 */
	public void addUnidirectionalEdge(UnidirectionalEdge edge) {
        uniEdges.add(edge);
    }

    /**
	 * Adds the given bidirectional edge to the graph.
	 *
	 * @param edge  the bidirectional edge to add to the graph
	 */
	public void addBidirectionalEdge(BidirectionalEdge edge) {
        biEdges.add(edge);
    }

    public boolean showNodes() {
        return supervisor.getNodeSupervisor().getShowNodes();
    }

	public boolean showEdges() {
		PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
		return controller.getGlobalEdgeSupervisor().getShowFlag();
	}

	/**
	 * Returns true if the self-loops must be displayed in the preview.
	 *
	 * @return true if the self-loops must be displayed in the preview
	 */
	public boolean showSelfLoops() {
		PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
		return controller.getSelfLoopSupervisor().getShowFlag();
	}
}
