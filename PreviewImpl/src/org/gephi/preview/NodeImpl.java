package org.gephi.preview;

import org.gephi.preview.api.Node;
import org.gephi.preview.api.color.Color;
import org.gephi.preview.api.color.colorizer.NodeColorizerClient;
import org.gephi.preview.color.SimpleColor;
import org.gephi.preview.supervisor.NodeLabelBorderSupervisor;
import org.gephi.preview.supervisor.NodeLabelSupervisor;
import org.gephi.preview.supervisor.NodeSupervisor;
import org.gephi.preview.util.HolderImpl;
import processing.core.PVector;

/**
 *
 * @author jeremy
 */
public class NodeImpl implements Node, NodeColorizerClient {

    private final NodeSupervisor supervisor;
    private final GraphImpl parent;
    private final PVector position;
    private final Float radius;
    private final Color originalColor;
    private final HolderImpl<Color> colorHolder = new HolderImpl<Color>();
    private final PVector topLeftPosition;
    private final PVector bottomRightPosition;
    private final NodeLabelImpl label;
    private final NodeLabelBorderImpl labelBorder;

    public NodeImpl(GraphImpl parent, String label, float x, float y, float radius, int r, int g, int b) {
        this.parent = parent;
        this.supervisor = parent.getSupervisor().getNodeSupervisor();
        this.position = new PVector(x, y);
        this.radius = radius;
        this.originalColor = new SimpleColor(r, g, b, 0);
        this.label = new NodeLabelImpl(this, label);
        this.labelBorder = new NodeLabelBorderImpl(this);

        topLeftPosition = position.get();
        topLeftPosition.sub(radius, radius, 0);

        bottomRightPosition = position.get();
        bottomRightPosition.add(radius, radius, 0);

        supervisor.addNode(this);
    }

    public GraphImpl getParentGraph() {
        return parent;
    }

    public NodeSupervisor getSupervisor() {
        return supervisor;
    }
    
    public NodeLabelSupervisor getLabelSupervisor() {
        return supervisor.getNodeLabelSupervisor();
    }
    
    public NodeLabelBorderSupervisor getLabelBorderSupervisor() {
        return supervisor.getNodeLabelBorderSupervisor();
    }

    public final PVector getPosition() {
        return position;
    }

    public final PVector getTopLeftPosition() {
        return topLeftPosition;
    }

    public final PVector getBottomRightPosition() {
        return bottomRightPosition;
    }
    
    public final NodeLabelImpl getLabel() {
        return label;
    }

    public final NodeLabelBorderImpl getLabelBorder() {
        return labelBorder;
    }

    public final float getRadius() {
        return radius;
    }

    public final Float getDiameter() {
        return radius * 2;
    }

    public final Color getOriginalColor() {
        return originalColor;
    }

    @Override
    public final Color getColor() {
        return colorHolder.getComponent();
    }

    @Override
    public final HolderImpl<Color> getColorHolder() {
        return colorHolder;
    }

    @Override
    public final void setColor(Color color) {
        colorHolder.setComponent(color);
    }

    public Color getBorderColor() {
        return supervisor.getNodeBorderColorizer().getColor();
    }

    public Float getBorderWidth() {
        return supervisor.getNodeBorderWidth();
    }

    public Boolean showLabels() {
        return supervisor.getNodeLabelSupervisor().getShowNodeLabels();
    }

    public Boolean showLabelBorders() {
        return supervisor.getNodeLabelBorderSupervisor().getShowNodeLabelBorders();
    }
}
