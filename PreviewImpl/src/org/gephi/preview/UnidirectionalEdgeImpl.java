package org.gephi.preview;

import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.UnidirectionalEdge;
import org.gephi.preview.supervisors.DirectedEdgeSupervisorImpl;
import org.openide.util.Lookup;

/**
 * Implementation of an unidirectional preview edge.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class UnidirectionalEdgeImpl extends DirectedEdgeImpl
        implements UnidirectionalEdge {

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
    public UnidirectionalEdgeImpl(GraphImpl parent, float thickness, NodeImpl node1, NodeImpl node2, String label, float labelSize) {
        super(parent, thickness, node1, node2, label, labelSize);

        getDirectedEdgeSupervisor().addEdge(this);
    }

    @Override
    public DirectedEdgeSupervisorImpl getDirectedEdgeSupervisor() {
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        return (DirectedEdgeSupervisorImpl) controller.getUniEdgeSupervisor();
    }
}
