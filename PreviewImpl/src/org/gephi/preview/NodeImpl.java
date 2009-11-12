package org.gephi.preview;

import org.gephi.preview.api.Node;
import org.gephi.preview.api.color.Color;
import org.gephi.preview.api.color.colorizer.NodeColorizerClient;
import org.gephi.preview.api.controller.PreviewController;
import org.gephi.preview.api.util.Holder;
import org.gephi.preview.color.SimpleColor;
import org.gephi.preview.supervisor.NodeSupervisorImpl;
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
     * Returns the node's position.
     *
     * @return the node's position
     */
    public PVector getPosition() {
        return position;
    }

    /**
     * Returns the node's top left position.
     *
     * @return the node's top left position
     */
    public PVector getTopLeftPosition() {
        return topLeftPosition;
    }

    /**
     * Returns the node's bottom right position.
     *
     * @return the node's bottom right position
     */
    public PVector getBottomRightPosition() {
        return bottomRightPosition;
    }

    /**
     * Returns the node's label.
     *
     * @return the node's label
     */
    public NodeLabelImpl getLabel() {
        return label;
    }

    /**
     * Returns the node's label border.
     *
     * @return the node's label border
     */
    public NodeLabelBorderImpl getLabelBorder() {
        return labelBorder;
    }

    /**
     * Returns the node's radius.
     *
     * @return the node's radius
     */
    public Float getRadius() {
        return radius;
    }

    /**
     * Returns the node's diameter.
     *
     * @return the node's diameter
     */
    public Float getDiameter() {
        return radius * 2;
    }

    /**
     * Returns the node's original color.
     *
     * @return the node's original color
     */
    public Color getOriginalColor() {
        return originalColor;
    }

    /**
     * Returns the node's current color.
     *
     * @return the node's current color
     */
    public Color getColor() {
        return colorHolder.getComponent();
    }

    /**
     * Returns the node's color holder.
     *
     * @return the node's color holder
     */
    public Holder<Color> getColorHolder() {
        return colorHolder;
    }

    /**
     * Defines the node's current color.
     *
     * @param color  the node's current color to set
     */
    public void setColor(Color color) {
        colorHolder.setComponent(color);
    }

    /**
     * Returns the node's border color.
     *
     * @return the node's border color
     */
    public Color getBorderColor() {
        return getNodeSupervisor().getNodeBorderColorizer().getColor();
    }

    /**
     * Returns the node's border width.
     *
     * @return the node's border width
     */
    public Float getBorderWidth() {
        return getNodeSupervisor().getNodeBorderWidth();
    }

    /**
     * Returns whether or not the node's label must be displayed.
     *
     * @return true to display the node's label
     */
    public Boolean showLabel() {
        return getNodeSupervisor().getShowNodeLabels();
    }

    /**
     * Returns whether or not the node's label borders must be displayed.
     *
     * @return true to display the node's label borders
     */
    public Boolean showLabelBorders() {
        return getNodeSupervisor().getShowNodeLabelBorders();
    }
}
