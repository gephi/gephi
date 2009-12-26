package org.gephi.preview;

import org.gephi.preview.api.util.HAlign;
import processing.core.PVector;

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
        hAlign = HAlign.LEFT;
    }

    /**
     * Generates the edge mini-label's position.
     */
    public void genPosition() {
        NodeImpl n1 = parent.getNode1();

        // relative position from the first boundary
        position = n1.getPosition().get();

        // add the added radius
        PVector move = PVector.mult(
                parent.getDirection(),
                getDirectedEdgeSupervisor().getMiniLabelAddedRadius() + n1.getRadius());
        position.add(move);

        // set label position above the parent edge
        putPositionAboveEdge(parent.getDirection(), parent.getThickness());
    }
}
