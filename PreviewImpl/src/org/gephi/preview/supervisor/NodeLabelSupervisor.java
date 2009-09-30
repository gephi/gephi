package org.gephi.preview.supervisor;

import java.awt.Font;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import org.gephi.preview.NodeLabelImpl;
import org.gephi.preview.api.color.colorizer.NodeChildColorizer;
import org.gephi.preview.color.colormode.CustomColorMode;
import org.gephi.preview.util.LabelShortener;

/**
 *
 * @author jeremy
 */
public class NodeLabelSupervisor {

    private final NodeSupervisor parent;
    private Boolean showNodeLabels = true;
    private Font nodeLabelfont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    private Integer nodeLabelMaxChar = 10;
    private NodeChildColorizer nodeLabelColorizer = new CustomColorMode(0, 0, 0);
    private final Set<NodeLabelImpl> supervisedNodeLabels = Collections.newSetFromMap(new WeakHashMap<NodeLabelImpl, Boolean>());

    public void addNodeLabel(NodeLabelImpl nodeLabel) {
        supervisedNodeLabels.add(nodeLabel);

        shortenNodeLabels();
        colorNodeLabels();
    }

    public NodeLabelSupervisor(NodeSupervisor parent) {
        this.parent = parent;
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

    private void shortenNodeLabel(NodeLabelImpl nodeLabel) {
        LabelShortener.shortenLabel(nodeLabel, nodeLabelMaxChar);
    }

    private void shortenNodeLabels() {
        for (NodeLabelImpl nl : supervisedNodeLabels) {
            shortenNodeLabel(nl);
        }
    }

    private void colorNodeLabel(NodeLabelImpl nodeLabel) {
        nodeLabelColorizer.color(nodeLabel);
    }

    private void colorNodeLabels() {
        for (NodeLabelImpl nl : supervisedNodeLabels) {
            colorNodeLabel(nl);
        }
    }
}
