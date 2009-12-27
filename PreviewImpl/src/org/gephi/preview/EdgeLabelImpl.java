package org.gephi.preview;

import java.awt.Font;
import org.gephi.preview.api.Color;
import org.gephi.preview.api.EdgeColorizerClient;
import org.gephi.preview.api.EdgeLabel;
import org.gephi.preview.api.util.Holder;
import org.gephi.preview.util.Vector;

/**
 * Implementation of an edge label.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class EdgeLabelImpl extends AbstractEdgeLabel implements EdgeLabel {

    protected final EdgeImpl parent;

    /**
     * Constructor.
     *
     * @param parent  the parent edge of the edge label
     * @param value   the value of the edge label
     */
    public EdgeLabelImpl(EdgeImpl parent, String value) {
        super(value);
        this.parent = parent;
        genPosition();
    }

    /**
     * Generates the edge label's position.
     */
    public void genPosition() {
        // relative position from the first boundary
        Vector positionVector = new Vector(parent.getNode1().getPosition());
        
        // move it to the middle of the edge
        Vector semiLength = new Vector(parent.getDirection());
        semiLength.mult(parent.getLength() / 2);
        positionVector.add(semiLength);

        position = new PointImpl(positionVector);

        // set label position above the parent edge
        putPositionAboveEdge(parent.getDirection(), parent.getThickness());
    }

    public Font getFont() {
        return parent.getLabelFont();
    }

    public Float getAngle() {
        return parent.getAngle();
    }

    public EdgeColorizerClient getParentEdge() {
        return parent;
    }

    public Holder<Color> getParentColorHolder() {
        return parent.getColorHolder();
    }
}
