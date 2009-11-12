package org.gephi.preview;

import org.gephi.preview.api.Node;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.color.Color;
import org.gephi.preview.api.color.colorizer.NodeColorizerClient;
import org.gephi.preview.color.SimpleColor;
import org.gephi.preview.supervisor.NodeSupervisorImpl;
import org.gephi.preview.util.HolderImpl;
import org.openide.util.Lookup;
import processing.core.PVector;

/**
 *
 * @author jeremy
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

    public GraphImpl getParentGraph() {
        return parent;
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
        return getNodeSupervisor().getNodeBorderColorizer().getColor();
    }

    public Float getBorderWidth() {
        return getNodeSupervisor().getNodeBorderWidth();
    }

    public Boolean showLabels() {
        return getNodeSupervisor().getShowNodeLabels();
    }

    public Boolean showLabelBorders() {
        return getNodeSupervisor().getShowNodeLabelBorders();
    }
}
