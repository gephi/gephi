package org.gephi.preview.supervisors;

import java.awt.Font;
import java.util.HashSet;
import java.util.Set;
import org.gephi.preview.NodeImpl;
import org.gephi.preview.NodeLabelBorderImpl;
import org.gephi.preview.NodeLabelImpl;
import org.gephi.preview.api.GenericColorizer;
import org.gephi.preview.api.NodeChildColorizer;
import org.gephi.preview.api.NodeColorizer;
import org.gephi.preview.api.SupervisorPropery;
import org.gephi.preview.api.supervisors.NodeSupervisor;
import org.gephi.preview.propertyeditors.GenericColorizerPropertyEditor;
import org.gephi.preview.propertyeditors.NodeChildColorizerPropertyEditor;
import org.gephi.preview.propertyeditors.NodeColorizerPropertyEditor;
import org.gephi.preview.updaters.CustomColorMode;
import org.gephi.preview.updaters.LabelFontAdjuster;
import org.gephi.preview.updaters.LabelShortener;
import org.gephi.preview.updaters.NodeOriginalColorMode;

/**
 * Node supervisor implementation.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class NodeSupervisorImpl implements NodeSupervisor {

    //Properties
    private Boolean showNodes;
    private Float nodeBorderWidth;
    private NodeColorizer nodeColorizer;
    private GenericColorizer nodeBorderColorizer;
    private Boolean showNodeLabels;
    private Font baseNodeLabelfont;
    private Boolean shortenLabelsFlag;
    private Integer nodeLabelMaxChar;
    private NodeChildColorizer nodeLabelColorizer;
    private Boolean showNodeLabelBorders;
    private NodeChildColorizer nodeLabelBorderColorizer;
    private Boolean proportionalLabelSize;

    //Architecture
    private final Set<NodeImpl> supervisedNodes = new HashSet<NodeImpl>();

    public NodeSupervisorImpl() {
        defaultValues();
    }

    public void defaultValues() {
        showNodes = true;
        nodeBorderWidth = 1f;
        nodeColorizer = new NodeOriginalColorMode();
        nodeBorderColorizer = new CustomColorMode(0, 0, 0);
        showNodeLabels = false;
        baseNodeLabelfont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
        shortenLabelsFlag = false;
        nodeLabelMaxChar = 10;
        nodeLabelColorizer = new CustomColorMode(0, 0, 0);
        showNodeLabelBorders = false;
        nodeLabelBorderColorizer = new CustomColorMode(255, 255, 255);
        proportionalLabelSize = true;
    }

    /**
     * Adds the given node to the list of the supervised nodes.
     *
     * It updates the node with the supervisor's values.
     *
     * @param node  the node to supervise
     */
    public void addNode(NodeImpl node) {
        supervisedNodes.add(node);

        colorNode(node);
        colorNodeLabel(node.getLabel());
        colorNodeLabelBorder(node.getLabelBorder());
        updateLabelValue(node.getLabel());
        adjustNodeLabelFont(node);
    }

    public void clearSupervised() {
        supervisedNodes.clear();
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

    public Font getBaseNodeLabelFont() {
        return baseNodeLabelfont;
    }

    public void setBaseNodeLabelFont(Font value) {
        baseNodeLabelfont = value;
        adjustNodeLabelFonts();
    }

    public Integer getNodeLabelMaxChar() {
        return nodeLabelMaxChar;
    }

    public void setNodeLabelMaxChar(Integer value) {
        nodeLabelMaxChar = value;
        updateLabelValues();
    }

    /**
     * Returns whether the node labels must be shortened.
     *
     * @return true to shorten the node labels
     */
    public Boolean getShortenLabelsFlag() {
        return shortenLabelsFlag;
    }

    /**
     * Defines if the node labels must be shortened.
     *
     * @param value  true to shorten the node labels
     */
    public void setShortenLabelsFlag(Boolean value) {
        shortenLabelsFlag = value;
        updateLabelValues();
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

    public Boolean getProportionalLabelSize() {
        return proportionalLabelSize;
    }

    public void setProportionalLabelSize(Boolean proportionnalLabelSize) {
        this.proportionalLabelSize = proportionnalLabelSize;
    }

    /**
     * Colors the given node with the node colorizer.
     *
     * @param node  the node to color
     */
    private void colorNode(NodeImpl node) {
        nodeColorizer.color(node);
    }

    /**
     * Colors the supervised nodes with the node colorizer.
     */
    private void colorNodes() {
        for (NodeImpl n : supervisedNodes) {
            colorNode(n);
        }
    }

    /**
     * Updates the node label by shortening its value or by reverting its
     * original one.
     */
    private void updateLabelValue(NodeLabelImpl nodeLabel) {
        if (nodeLabel != null) {
            if (shortenLabelsFlag) {
                shortenNodeLabel(nodeLabel);
            } else {
                revertLabel(nodeLabel);
            }
        }
    }

    /**
     * Updates the node labels by shortening their values or by reverting their
     * original ones.
     */
    private void updateLabelValues() {
        if (shortenLabelsFlag) {
            shortenNodeLabels();
        } else {
            revertLabels();
        }
    }

    /**
     * Shortens the given node label.
     *
     * @param nodeLabel  the node label to shorten
     */
    private void shortenNodeLabel(NodeLabelImpl nodeLabel) {
        LabelShortener.shortenLabel(nodeLabel, nodeLabelMaxChar);
    }

    /**
     * Shortens the labels of the supervised nodes.
     */
    private void shortenNodeLabels() {
        for (NodeImpl n : supervisedNodes) {
            if (n.hasLabel()) {
                shortenNodeLabel(n.getLabel());
            }
        }
    }

    /**
     * Reverts the original value of the given node label.
     *
     * @param nodeLabel  the node label to revert the original value
     */
    private void revertLabel(NodeLabelImpl nodeLabel) {
        LabelShortener.revertLabel(nodeLabel);
    }

    /**
     * Reverts the labels of the supervised nodes.
     */
    private void revertLabels() {
        for (NodeImpl n : supervisedNodes) {
            if (n.hasLabel()) {
                revertLabel(n.getLabel());
            }
        }
    }

    /**
     * Colors the given node label with the node label colorizer.
     *
     * @param node  the node label to color
     */
    private void colorNodeLabel(NodeLabelImpl nodeLabel) {
        if (nodeLabel != null) {
            nodeLabelColorizer.color(nodeLabel);
        }
    }

    /**
     * Colors the label of the supervised nodes with the node label colorizer.
     */
    private void colorNodeLabels() {
        for (NodeImpl n : supervisedNodes) {
            if (n.hasLabel()) {
                colorNodeLabel(n.getLabel());
            }
        }
    }

    /**
     * Colors the given node label border with the node label border colorizer.
     *
     * @param node  the node label border to color
     */
    private void colorNodeLabelBorder(NodeLabelBorderImpl nodeLabelBorder) {
        if (nodeLabelBorder != null) {
            nodeLabelBorderColorizer.color(nodeLabelBorder);
        }
    }

    /**
     * Colors the label border of the supervised nodes with the node label
     * border colorizer.
     */
    private void colorNodeLabelBorders() {
        for (NodeImpl n : supervisedNodes) {
            colorNodeLabelBorder(n.getLabelBorder());
        }
    }

    /**
     * Adjusts the font of the given node label.
     *
     * @param label  the node label to adjust the font
     */
    private void adjustNodeLabelFont(NodeLabelImpl label) {
        LabelFontAdjuster.adjustFont(label);
    }

    /**
     * Adjusts the label font of a given node.
     *
     * @param edge  the node to adjust the label font
     */
    private void adjustNodeLabelFont(NodeImpl node) {
        if (node.hasLabel()) {
            adjustNodeLabelFont(node.getLabel());
        }
    }

    /**
     * Adjusts the label fonts of the supervised nodes.
     */
    private void adjustNodeLabelFonts() {
        for (NodeImpl n : supervisedNodes) {
            adjustNodeLabelFont(n);
        }
    }

    public SupervisorPropery[] getProperties() {
        final String CATEGORY = "Node";
        try {
            return new SupervisorPropery[]{
                        SupervisorPropery.createProperty(this, Boolean.class, "showNodes", CATEGORY, "Show"),
                        SupervisorPropery.createProperty(this, Float.class, "nodeBorderWidth", CATEGORY, "Border width"),
                        SupervisorPropery.createProperty(this, NodeColorizer.class, "nodeColorizer", CATEGORY, "Color", NodeColorizerPropertyEditor.class),
                        SupervisorPropery.createProperty(this, GenericColorizer.class, "nodeBorderColorizer", CATEGORY, "Border color", GenericColorizerPropertyEditor.class),
                        SupervisorPropery.createProperty(this, Boolean.class, "showNodeLabels", CATEGORY, "Show labels"),
                        SupervisorPropery.createProperty(this, Boolean.class, "proportionalLabelSize", CATEGORY, "Proportional label size"),
                        SupervisorPropery.createProperty(this, Font.class, "baseNodeLabelFont", CATEGORY, "Label font"),
                        SupervisorPropery.createProperty(this, Boolean.class, "shortenLabelsFlag", CATEGORY, "Shorten labels"),
                        SupervisorPropery.createProperty(this, Integer.class, "nodeLabelMaxChar", CATEGORY, "Shorten limit"),
                        SupervisorPropery.createProperty(this, NodeChildColorizer.class, "nodeLabelColorizer", CATEGORY, "Label color", NodeChildColorizerPropertyEditor.class),
                        SupervisorPropery.createProperty(this, Boolean.class, "showNodeLabelBorders", CATEGORY, "Label border"),
                        SupervisorPropery.createProperty(this, NodeChildColorizer.class, "nodeLabelBorderColorizer", CATEGORY, "Label Border Color", NodeChildColorizerPropertyEditor.class)
                    };
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new SupervisorPropery[0];
    }
}
