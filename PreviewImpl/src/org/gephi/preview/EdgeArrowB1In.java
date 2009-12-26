package org.gephi.preview;

import org.gephi.preview.api.supervisors.DirectedEdgeSupervisor;
import processing.core.PVector;

/**
 * An edge arrow incoming to the parent edge's first boundary.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
class EdgeArrowB1In extends EdgeArrowImpl {

    /**
     * Constructor.
     *
     * @param parent  the parent edge of the edge arrow
     */
    public EdgeArrowB1In(DirectedEdgeImpl parent) {
        super(parent);
        direction = new PVector(-parent.getDirection().x, -parent.getDirection().y);
        refNode = parent.getNode1();
    }

    /**
     * Generates the edge arrow's added radius.
     */
    protected void genAddedRadius() {
        DirectedEdgeSupervisor supervisor = getDirectedEdgeSupervisor();
        addedRadius = -(supervisor.getArrowAddedRadius() + supervisor.getArrowSize() + refNode.getRadius());
    }
}
