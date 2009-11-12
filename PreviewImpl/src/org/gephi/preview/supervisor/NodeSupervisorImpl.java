package org.gephi.preview.supervisor;

import java.awt.Font;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import org.gephi.preview.NodeImpl;
import org.gephi.preview.NodeLabelBorderImpl;
import org.gephi.preview.NodeLabelImpl;
import org.gephi.preview.api.color.colorizer.GenericColorizer;
import org.gephi.preview.api.color.colorizer.NodeChildColorizer;
import org.gephi.preview.api.color.colorizer.NodeColorizer;
import org.gephi.preview.api.supervisor.NodeSupervisor;
import org.gephi.preview.color.colormode.CustomColorMode;
import org.gephi.preview.color.colormode.NodeOriginalColorMode;
import org.gephi.preview.util.LabelShortener;

/**
 * Node supervisor implementation.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class NodeSupervisorImpl implements NodeSupervisor {

    private Boolean showNodes = true;
    private Float nodeBorderWidth = 1f;
    private NodeColorizer nodeColorizer = new NodeOriginalColorMode();
    private GenericColorizer nodeBorderColorizer = new CustomColorMode(0, 0, 0);
    private Boolean showNodeLabels = true;
    private Font nodeLabelfont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    private Integer nodeLabelMaxChar = 10;
    private NodeChildColorizer nodeLabelColorizer = new CustomColorMode(0, 0, 0);
    private Boolean showNodeLabelBorders = true;
    private NodeChildColorizer nodeLabelBorderColorizer = new CustomColorMode(255, 255, 255);
    private final Set<NodeImpl> supervisedNodes = Collections.newSetFromMap(new WeakHashMap<NodeImpl, Boolean>());

    public void addNode(NodeImpl node) {
        supervisedNodes.add(node);

        colorNodes();
        shortenNodeLabels();
        colorNodeLabels();
        colorNodeLabelBorders();
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

    public Boolean getShowNodeLabels() {
        return showNodeLabels;
    }

    public void setShowNodeLabels(Boolean value) {
        showNodeLabels = value;
    }

    public Font getNodeLabelFont() {
        return nodeLabelfont;
    }

    public void setNodeLabelFont(Font value) {
        nodeLabelfont = value;
    }

    public Integer getNodeLabelMaxChar() {
        return nodeLabelMaxChar;
    }

    public void setNodeLabelMaxChar(Integer value) {
        nodeLabelMaxChar = value;
        shortenNodeLabels();
    }

    public NodeChildColorizer getNodeLabelColorizer() {
        return nodeLabelColorizer;
    }

    public void setNodeLabelColorizer(NodeChildColorizer value) {
        nodeLabelColorizer = value;
        colorNodeLabels();
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

    private void colorNode(NodeImpl node) {
        nodeColorizer.color(node);
    }

    private void colorNodes() {
        for (NodeImpl n : supervisedNodes) {
            colorNode(n);
        }
    }

    private void shortenNodeLabel(NodeLabelImpl nodeLabel) {
        LabelShortener.shortenLabel(nodeLabel, nodeLabelMaxChar);
    }

    private void shortenNodeLabels() {
        for (NodeImpl n : supervisedNodes) {
            shortenNodeLabel(n.getLabel());
        }
    }

    private void colorNodeLabel(NodeLabelImpl nodeLabel) {
        nodeLabelColorizer.color(nodeLabel);
    }

    private void colorNodeLabels() {
        for (NodeImpl n : supervisedNodes) {
            colorNodeLabel(n.getLabel());
        }
    }

    private void colorNodeLabelBorder(NodeLabelBorderImpl nodeLabelBorder) {
        nodeLabelBorderColorizer.color(nodeLabelBorder);
    }

    private void colorNodeLabelBorders() {
        for (NodeImpl n : supervisedNodes) {
            colorNodeLabelBorder(n.getLabelBorder());
        }
    }
}
