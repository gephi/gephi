package org.gephi.preview;

import org.gephi.preview.api.BidirectionalEdge;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.supervisors.DirectedEdgeSupervisorImpl;
import org.gephi.preview.util.Vector;
import org.openide.util.Lookup;

/**
 * Implementation of a bidirectional edge.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class BidirectionalEdgeImpl extends DirectedEdgeImpl
        implements BidirectionalEdge {

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
    public BidirectionalEdgeImpl(GraphImpl parent, float thickness, NodeImpl node1, NodeImpl node2, String label, float labelSize) {
        super(parent, thickness, node1, node2, label, labelSize);

        // generate arrows
        arrows.add(new EdgeArrowB2Out(this));
        arrows.add(new EdgeArrowB1In(this));

        getDirectedEdgeSupervisor().addEdge(this);
    }

    @Override
    public DirectedEdgeSupervisorImpl getDirectedEdgeSupervisor() {
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        return (DirectedEdgeSupervisorImpl) controller.getBiEdgeSupervisor();
    }

    /**
     * Generates a curve from node 1 to node 2 and a another one from node 2 to
     * node 1, then adds them to the curve list.
     */
    @Override
    protected void genCurves() {
        super.genCurves();

        float factor = BEZIER_CURVE_FACTOR * length;

        // normal vector to the edge
        Vector n = new Vector(direction.y, -direction.x);
        n.mult(factor);

        // first control point
        Vector v1 = new Vector(direction);
        v1.mult(factor);
        v1.add(new Vector(node1.getPosition()));
        v1.sub(n);

        // second control point
        Vector v2 = new Vector(direction);
        v2.mult(-factor);
        v2.add(new Vector(node2.getPosition()));
        v2.sub(n);

        curves.add(new CubicBezierCurveImpl(
                node1.getPosition(),
                new PointImpl(v1),
                new PointImpl(v2),
                node2.getPosition()));
    }
}
