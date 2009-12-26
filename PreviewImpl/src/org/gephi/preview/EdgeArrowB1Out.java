package org.gephi.preview;

import org.gephi.preview.api.supervisors.DirectedEdgeSupervisor;
import processing.core.PVector;

/**
 * An edge arrow outgoing from the parent edge's first boundary.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
class EdgeArrowB1Out extends EdgeArrowImpl {

    /**
     * Constructor.
     *
     * @param parent  the parent edge of the edge arrow
     */
    public EdgeArrowB1Out(DirectedEdgeImpl parent) {
        super(parent);
        refNode = parent.getNode1();
        direction = new PVector(parent.getDirection().x, parent.getDirection().y);
    }

    /**
     * Generates the edge arrow's added radius.
     */
    protected void genAddedRadius() {
        DirectedEdgeSupervisor supervisor = getDirectedEdgeSupervisor();
        addedRadius = supervisor.getArrowAddedRadius() + supervisor.getArrowSize() + refNode.getRadius();
    }
}
