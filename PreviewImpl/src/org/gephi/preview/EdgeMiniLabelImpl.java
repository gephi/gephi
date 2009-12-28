package org.gephi.preview;

import java.awt.Font;
import org.gephi.preview.api.Color;
import org.gephi.preview.api.EdgeColorizerClient;
import org.gephi.preview.api.EdgeMiniLabel;
import org.gephi.preview.api.supervisors.DirectedEdgeSupervisor;
import org.gephi.preview.util.HAlignImpl;
import org.gephi.preview.api.util.Holder;

/**
 * Implementation of an edge mini-label.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public abstract class EdgeMiniLabelImpl extends AbstractEdgeLabel
        implements EdgeMiniLabel {

    protected final DirectedEdgeImpl parent;
    protected HAlignImpl hAlign;

    /**
     * Constructor.
     *
     * @param parent  the parent edge of the edge label
     * @param value   the value of the edge label
     */
    public EdgeMiniLabelImpl(DirectedEdgeImpl parent, String value) {
        super(value);
        this.parent = parent;
    }

    /**
     * Returns the directed edge supervisor.
     *
     * @return the directed edge supervisor
     */
    public DirectedEdgeSupervisor getDirectedEdgeSupervisor() {
        return ((DirectedEdgeImpl) parent).getDirectedEdgeSupervisor();
    }

    /**
     * Generates the edge mini-label's position.
     */
    public abstract void genPosition();

    public HAlignImpl getHAlign() {
        return hAlign;
    }

    public Font getFont() {
        return parent.getMiniLabelFont();
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
