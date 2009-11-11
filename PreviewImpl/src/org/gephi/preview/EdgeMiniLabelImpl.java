package org.gephi.preview;

import org.gephi.preview.api.EdgeMiniLabel;
import org.gephi.preview.api.util.HAlign;

/**
 * Implementation of an edge mini-label.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public abstract class EdgeMiniLabelImpl extends AbstractEdgeLabel
        implements EdgeMiniLabel {

    protected HAlign hAlign;

    /**
     * Constructor.
     *
     * @param parent  the parent edge of the edge label
     * @param value   the value of the edge label
     */
    public EdgeMiniLabelImpl(EdgeImpl parent, String value) {
        super(parent, value);
    }

    /**
     * Generates the edge mini-label's position.
     */
    public abstract void genPosition();

    /**
     * Returns the edge mini-label's horizontal alignment.
     *
     * @return the edge mini-label's horizontal alignment
     */
    public HAlign getHAlign() {
        return hAlign;
    }
}
