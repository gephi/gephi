package org.gephi.preview;

import org.gephi.preview.util.HAlignImpl;
import org.gephi.preview.util.Vector;

/**
 * An edge mini-label related to the parent edge's second boundary.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
class EdgeMiniLabelB2 extends EdgeMiniLabelImpl {

    /**
     * Constructor.
     *
     * @param parent  the parent edge of the edge label
     */
    public EdgeMiniLabelB2(DirectedEdgeImpl parent) {
        super(parent, parent.getNode1().getLabel().getOriginalValue());
        hAlign = HAlignImpl.RIGHT;
    }

    /**
     * Generates the edge mini-label's position.
     */
    public void genPosition() {
        NodeImpl n2 = parent.getNode2();

        // relative position from the second boundary
        Vector positionVector = new Vector(n2.getPosition());

        // add the added radius
        Vector move = new Vector(parent.getDirection());
        move.mult(-(getDirectedEdgeSupervisor().getMiniLabelAddedRadius() + n2.getRadius()));
        positionVector.add(move);

        position = new PointImpl(positionVector);

        // set label position above the parent edge
        putPositionAboveEdge(parent.getDirection(), parent.getThickness());
    }
}
