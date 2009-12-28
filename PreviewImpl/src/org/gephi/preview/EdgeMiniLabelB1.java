package org.gephi.preview;

import org.gephi.preview.util.HAlignImpl;
import org.gephi.preview.util.Vector;

/**
 * An edge mini-label related to the parent edge's first boundary.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
class EdgeMiniLabelB1 extends EdgeMiniLabelImpl {

    /**
     * Constructor.
     *
     * @param parent  the parent edge of the edge label
     */
    public EdgeMiniLabelB1(DirectedEdgeImpl parent) {
        super(parent, parent.getNode2().getLabel().getOriginalValue());
        hAlign = HAlignImpl.LEFT;
    }

    /**
     * Generates the edge mini-label's position.
     */
    public void genPosition() {
        NodeImpl n1 = parent.getNode1();

        // relative position from the first boundary
        Vector positionVector = new Vector(n1.getPosition());

        // adds the added radius
        Vector move = new Vector(parent.getDirection());
        move.mult(getDirectedEdgeSupervisor().getMiniLabelAddedRadius() + n1.getRadius());
        positionVector.add(move);

        position = new PointImpl(positionVector);

        // sets label position above the parent edge
        putPositionAboveEdge(parent.getDirection(), parent.getThickness());
    }
}
