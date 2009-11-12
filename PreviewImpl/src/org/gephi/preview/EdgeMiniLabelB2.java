package org.gephi.preview;

import org.gephi.preview.api.util.HAlign;
import processing.core.PVector;

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
    public EdgeMiniLabelB2(EdgeImpl parent) {
        super(parent, parent.getNode1().getLabel().getOriginalValue());
        hAlign = HAlign.RIGHT;
    }

    /**
     * Generates the edge mini-label's position.
     */
    public void genPosition() {
        NodeImpl n2 = parent.getNode2();

        // relative position from the second boundary
        position = n2.getPosition().get();

        // add the added radius
        PVector move = PVector.mult(
                parent.getDirection(),
                -(getEdgeSupervisor().getMiniLabelAddedRadius() + n2.getRadius()));
        position.add(move);

        // set label position above the parent edge
        putPositionAboveEdge();
    }
}
