package org.gephi.preview.supervisor;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import org.gephi.preview.NodeLabelBorderImpl;
import org.gephi.preview.api.color.colorizer.NodeChildColorizer;
import org.gephi.preview.color.colormode.CustomColorMode;

/**
 *
 * @author jeremy
 */
public class NodeLabelBorderSupervisor {

    private final NodeSupervisor parent;
    private Boolean showNodeLabelBorders = true;
    private NodeChildColorizer nodeLabelBorderColorizer = new CustomColorMode(255, 255, 255);
    private final Set<NodeLabelBorderImpl> supervisedNodeLabelBorders = Collections.newSetFromMap(new WeakHashMap<NodeLabelBorderImpl, Boolean>());

    public void addNodeLabelBorder(NodeLabelBorderImpl nodeLabelBorder) {
        supervisedNodeLabelBorders.add(nodeLabelBorder);

        colorNodeLabelBorders();
    }

    public NodeLabelBorderSupervisor(NodeSupervisor parent) {
        this.parent = parent;
    }

    public Boolean getShowNodeLabelBorders() {
        return showNodeLabelBorders;
    }

    public void setShowNodeLabelBorders(Boolean value) {
        showNodeLabelBorders = value;
    }

    public NodeChildColorizer getNodeLabelBorderColorizer() {
        return nodeLabelBorderColorizer;
    }

    public void setNodeLabelBorderColorizer(NodeChildColorizer value) {
        nodeLabelBorderColorizer = value;
        colorNodeLabelBorders();
    }

    private void colorNodeLabelBorder(NodeLabelBorderImpl nodeLabelBorder) {
        nodeLabelBorderColorizer.color(nodeLabelBorder);
    }

    private void colorNodeLabelBorders() {
        for (NodeLabelBorderImpl nlb : supervisedNodeLabelBorders) {
            colorNodeLabelBorder(nlb);
        }
    }
}
