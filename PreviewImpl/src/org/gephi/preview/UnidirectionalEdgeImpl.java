package org.gephi.preview;

import org.gephi.preview.api.UnidirectionalEdge;
import org.gephi.preview.api.controller.PreviewController;
import org.gephi.preview.supervisor.EdgeSupervisorImpl;
import org.openide.util.Lookup;

/**
 * Implementation of an unidirectional preview edge.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class UnidirectionalEdgeImpl extends EdgeImpl
        implements UnidirectionalEdge {

    /**
     * Constructor.
     *
     * @param parent     the parent graph of the edge
     * @param thickness  the edge's thickness
     * @param alpha      the edge's alpha color component
     * @param node1      the edge's node 1
     * @param node2      the edge's node 2
     * @param label      the edge's label
     */
    public UnidirectionalEdgeImpl(GraphImpl parent, float thickness, int alpha, NodeImpl node1, NodeImpl node2, String label) {
        super(parent, thickness, alpha, node1, node2, label);

        getEdgeSupervisor().addEdge(this);
    }

    /**
     * Returns the unidirectional edge supervisor.
     *
     * @return the controller's unidirectional edge supervisor
     */
    public EdgeSupervisorImpl getEdgeSupervisor() {
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        return (EdgeSupervisorImpl) controller.getUniEdgeSupervisor();
    }
}
