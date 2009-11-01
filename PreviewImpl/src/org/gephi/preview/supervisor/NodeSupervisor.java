package org.gephi.preview.supervisor;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import org.gephi.preview.NodeImpl;
import org.gephi.preview.api.color.colorizer.GenericColorizer;
import org.gephi.preview.api.color.colorizer.NodeColorizer;
import org.gephi.preview.color.colormode.CustomColorMode;
import org.gephi.preview.color.colormode.NodeOriginalColorMode;

/**
 *
 * @author jeremy
 */
public class NodeSupervisor {

    private Boolean showNodes = true;
    private Float nodeBorderWidth = 1f;
    private NodeColorizer nodeColorizer = new NodeOriginalColorMode();
    private GenericColorizer nodeBorderColorizer = new CustomColorMode(0, 0, 0);
    private final NodeLabelSupervisor nlSupervisor = new NodeLabelSupervisor(this);
    private final NodeLabelBorderSupervisor nlbSupervisor = new NodeLabelBorderSupervisor(this);
    private final Set<NodeImpl> supervisedNodes = Collections.newSetFromMap(new WeakHashMap<NodeImpl, Boolean>());

    public void addNode(NodeImpl node) {
        supervisedNodes.add(node);

        colorNodes();
    }

    public NodeLabelSupervisor getNodeLabelSupervisor() {
        return nlSupervisor;
    }
    
    public NodeLabelBorderSupervisor getNodeLabelBorderSupervisor() {
        return nlbSupervisor;
    }

    public Boolean getShowNodes() {
        return showNodes;
    }

    public void setShowNodes(Boolean value) {
        showNodes = value;
    }

    public Float getNodeBorderWidth() {
        return nodeBorderWidth;
    }

    public void setNodeBorderWidth(Float value) {
        nodeBorderWidth = value;
    }

    public NodeColorizer getNodeColorizer() {
        return nodeColorizer;
    }

    public void setNodeColorizer(NodeColorizer value) {
        nodeColorizer = value;
        colorNodes();
    }

    public GenericColorizer getNodeBorderColorizer() {
        return nodeBorderColorizer;
    }

    public void setNodeBorderColorizer(GenericColorizer value) {
        nodeBorderColorizer = value;
    }

    private void colorNode(NodeImpl node) {
        nodeColorizer.color(node);
    }

    private void colorNodes() {
        for (NodeImpl n : supervisedNodes) {
            colorNode(n);
        }
    }
}
