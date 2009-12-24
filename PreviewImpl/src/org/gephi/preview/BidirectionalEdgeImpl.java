package org.gephi.preview;

import org.gephi.preview.api.BidirectionalEdge;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.supervisors.EdgeSupervisorImpl;
import org.openide.util.Lookup;
import processing.core.PVector;

/**
 * Implementation of a bidirectional edge.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class BidirectionalEdgeImpl extends EdgeImpl
        implements BidirectionalEdge {

   /**
     * Constructor.
     *
     * @param parent     the parent graph of the edge
     * @param thickness  the edge's thickness
     * @param node1      the edge's node 1
     * @param node2      the edge's node 2
     * @param label      the edge's label
     */
    public BidirectionalEdgeImpl(GraphImpl parent, float thickness, NodeImpl node1, NodeImpl node2, String label) {
        super(parent, thickness, node1, node2, label);

        // generate arrows
        arrows.add(new EdgeArrowB2Out(this));
        arrows.add(new EdgeArrowB1In(this));

        getEdgeSupervisor().addEdge(this);
    }

    /**
     * Returns the bidirectional edge supervisor.
     *
     * @return the controller's bidirectional edge supervisor
     */
    public EdgeSupervisorImpl getEdgeSupervisor() {
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        return (EdgeSupervisorImpl) controller.getBiEdgeSupervisor();
    }

    /**
     * Generates a curve from node 1 to node 2 and a another one from node 2 to
     * node 1, then adds them to the curve list.
     */
    @Override
    protected void genCurves() {
        super.genCurves();

        float factor = BEZIER_CURVE_FACTOR * length;
        PVector v, n;

        // normal vector to the edge
        n = new PVector(direction.y, -direction.x);
        n.mult(factor);

        // first control point
        v = PVector.mult(direction, factor);
        v.add(node1.getPosition());
        PVector cp1 = PVector.sub(v, n);

        // second control point
        v = PVector.mult(direction, -factor);
        v.add(node2.getPosition());
        PVector cp2 = PVector.sub(v, n);

        curves.add(new CubicBezierCurveImpl(
                node1.getPosition(),
                cp1,
                cp2,
                node2.getPosition()));
    }
}
