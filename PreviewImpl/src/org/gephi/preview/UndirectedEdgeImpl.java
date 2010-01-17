package org.gephi.preview;

import org.gephi.preview.api.UndirectedEdge;
import org.gephi.preview.supervisors.EdgeSupervisorImpl;
import org.gephi.preview.supervisors.UndirectedEdgeSupervisorImpl;

/**
 * Implementation of an undirected edge.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class UndirectedEdgeImpl extends EdgeImpl implements UndirectedEdge {

    /**
     * Constructor.
     *
     * @param parent     the parent graph of the edge
     * @param thickness  the edge's thickness
     * @param node1      the edge's node 1
     * @param node2      the edge's node 2
     * @param label      the edge's label
     * @param labelSize  the edge's label size
     */
    public UndirectedEdgeImpl(GraphImpl parent, float thickness, NodeImpl node1, NodeImpl node2, String label, float labelSize) {
        super(parent, thickness, node1, node2, label, labelSize);

        getUndirectedEdgeSupervisor().addEdge(this);
    }

    /**
     * Returns the undirected edge supervisor.
     *
     * @return the undirected edge supervisor
     */
    public UndirectedEdgeSupervisorImpl getUndirectedEdgeSupervisor() {
        return (UndirectedEdgeSupervisorImpl) parent.getModel().getUndirectedEdgeSupervisor();
    }

    @Override
    protected EdgeSupervisorImpl getEdgeSupervisor() {
        return getUndirectedEdgeSupervisor();
    }
}
