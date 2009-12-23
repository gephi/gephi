package org.gephi.preview;

import java.awt.Font;
import org.gephi.preview.api.Color;
import org.gephi.preview.api.Node;
import org.gephi.preview.api.NodeColorizerClient;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.util.Holder;
import org.gephi.preview.util.color.SimpleColor;
import org.gephi.preview.supervisors.NodeSupervisorImpl;
import org.gephi.preview.util.HolderImpl;
import org.openide.util.Lookup;
import processing.core.PVector;

/**
 * Implementation of a preview node.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class NodeImpl implements Node, NodeColorizerClient {

    private final GraphImpl parent;
    private final PVector position;
    private final Float radius;
    private final Color originalColor;
    private final HolderImpl<Color> colorHolder = new HolderImpl<Color>();
    private final PVector topLeftPosition;
    private final PVector bottomRightPosition;
    private final NodeLabelImpl label;
    private final NodeLabelBorderImpl labelBorder;

    /**
     * Constructor.
     *
     * @param parent  the parent graph
     * @param label   the node label
     * @param x       the 'x' position component
     * @param y       the 'y' position component
     * @param radius  the node radius
     * @param r       the red color component
     * @param g       the green color component
     * @param b       the blue color component
     */
    public NodeImpl(GraphImpl parent, String label, float x, float y, float radius, float r, float g, float b) {
        this.parent = parent;
        this.position = new PVector(x, y);
        this.radius = radius;
        this.originalColor = new SimpleColor(r, g, b, 0);
        this.label = new NodeLabelImpl(this, label);
        this.labelBorder = new NodeLabelBorderImpl(this);

        topLeftPosition = position.get();
        topLeftPosition.sub(radius, radius, 0);

        bottomRightPosition = position.get();
        bottomRightPosition.add(radius, radius, 0);

        getNodeSupervisor().addNode(this);
    }

    /**
     * @see Node#hasLabel()
     */
    public boolean hasLabel() {
        return null != label;
    }

    /**
     * Returns the node supervisor.
     *
     * @return the controller's node supervisor
     */
    public NodeSupervisorImpl getNodeSupervisor() {
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        return (NodeSupervisorImpl) controller.getNodeSupervisor();
    }

    /**
     * Returns the parent graph.
     *
     * @return the parent graph
     */
    public GraphImpl getParentGraph() {
        return parent;
    }

    /**
     * @see Node#getPosition()
     */
    public PVector getPosition() {
        return position;
    }

    /**
     * @see Node#getTopLeftPosition()
     */
    public PVector getTopLeftPosition() {
        return topLeftPosition;
    }

    /**
     * @see Node#getBottomRightPosition()
     */
    public PVector getBottomRightPosition() {
        return bottomRightPosition;
    }

    /**
     * @see Node#getLabel()
     */
    public NodeLabelImpl getLabel() {
        return label;
    }

    /**
     * Returns the label font.
     *
     * @return the label font
     */
    public Font getLabelFont() {
        return getNodeSupervisor().getNodeLabelFont();
    }

    /**
     * @see Node#getLabelBorder()
     */
    public NodeLabelBorderImpl getLabelBorder() {
        return labelBorder;
    }

    /**
     * @see Node#getRadius()
     */
    public Float getRadius() {
        return radius;
    }

    /**
     * @see Node#getDiameter()
     */
    public Float getDiameter() {
        return radius * 2;
    }

    /**
     * @see NodeColorizerClient#getOriginalColor()
     */
    public Color getOriginalColor() {
        return originalColor;
    }

    /**
     * @see Node#getColor()
     */
    public Color getColor() {
        return colorHolder.getComponent();
    }

    /**
     * @see Node#getColorHolder()
     */
    public Holder<Color> getColorHolder() {
        return colorHolder;
    }

    /**
     * @see NodeColorizerClient#setColor(org.gephi.preview.api.Color)
     */
    public void setColor(Color color) {
        colorHolder.setComponent(color);
    }

    /**
     * @see Node#getBorderColor()
     */
    public Color getBorderColor() {
        return getNodeSupervisor().getNodeBorderColorizer().getColor();
    }

    /**
     * @see Node#getBorderWidth()
     */
    public Float getBorderWidth() {
        return getNodeSupervisor().getNodeBorderWidth();
    }

    /**
     * @see Node#showLabel()
     */
    public Boolean showLabel() {
        return getNodeSupervisor().getShowNodeLabels();
    }

    /**
     * @see Node#showLabelBorders()
     */
    public Boolean showLabelBorders() {
        return getNodeSupervisor().getShowNodeLabelBorders();
    }
}
