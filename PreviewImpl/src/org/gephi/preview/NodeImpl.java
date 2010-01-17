package org.gephi.preview;

import java.awt.Font;
import org.gephi.preview.api.Color;
import org.gephi.preview.api.Node;
import org.gephi.preview.api.NodeColorizerClient;
import org.gephi.preview.api.Point;
import org.gephi.preview.api.util.Holder;
import org.gephi.preview.util.color.SimpleColor;
import org.gephi.preview.supervisors.NodeSupervisorImpl;
import org.gephi.preview.util.HolderImpl;
import org.gephi.preview.util.Vector;

/**
 * Implementation of a preview node.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class NodeImpl implements Node, NodeColorizerClient {

    private final GraphImpl parent;
    private final Point position;
    private final Float radius;
    private final Color originalColor;
    private final HolderImpl<Color> colorHolder = new HolderImpl<Color>();
    private final PointImpl topLeftPosition, bottomRightPosition;
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
    public NodeImpl(GraphImpl parent, float x, float y, float radius, String label, float labelSize, float r, float g, float b) {
        this.parent = parent;
        this.position = new PointImpl(x, y);
        this.radius = radius;
        this.originalColor = new SimpleColor(r, g, b, 0);
        this.label = new NodeLabelImpl(this, label, labelSize);
        this.labelBorder = new NodeLabelBorderImpl(this);

        Vector topLeftVector = new Vector(position);
        topLeftVector.sub(radius, radius, 0);
        topLeftPosition = new PointImpl(topLeftVector);

        Vector bottomRightVector = new Vector(position);
        bottomRightVector.add(radius, radius, 0);
        bottomRightPosition = new PointImpl(bottomRightVector);

        getNodeSupervisor().addNode(this);
    }

    public boolean hasLabel() {
        return null != label;
    }

    /**
     * Returns the node supervisor.
     *
     * @return the controller's node supervisor
     */
    public NodeSupervisorImpl getNodeSupervisor() {
        return (NodeSupervisorImpl) parent.getModel().getNodeSupervisor();
    }

    /**
     * Returns the parent graph.
     *
     * @return the parent graph
     */
    public GraphImpl getParentGraph() {
        return parent;
    }

    public Point getPosition() {
        return position;
    }

    public Point getTopLeftPosition() {
        return topLeftPosition;
    }

    public Point getBottomRightPosition() {
        return bottomRightPosition;
    }

    public NodeLabelImpl getLabel() {
        return label;
    }

    /**
     * Returns the base label font.
     *
     * @return the base label font
     */
    public Font getBaseLabelFont() {
        return getNodeSupervisor().getBaseNodeLabelFont();
    }

    public NodeLabelBorderImpl getLabelBorder() {
        return labelBorder;
    }

    public Float getRadius() {
        return radius;
    }

    public Float getDiameter() {
        return radius * 2;
    }

    public Color getOriginalColor() {
        return originalColor;
    }

    public Color getColor() {
        return colorHolder.getComponent();
    }

    public Holder<Color> getColorHolder() {
        return colorHolder;
    }

    public void setColor(Color color) {
        colorHolder.setComponent(color);
    }

    public Color getBorderColor() {
        return getNodeSupervisor().getNodeBorderColorizer().getColor();
    }

    public Float getBorderWidth() {
        return getNodeSupervisor().getNodeBorderWidth();
    }

    public Boolean showLabel() {
        return getNodeSupervisor().getShowNodeLabels();
    }

    public Boolean showLabelBorders() {
        return getNodeSupervisor().getShowNodeLabelBorders();
    }
}
