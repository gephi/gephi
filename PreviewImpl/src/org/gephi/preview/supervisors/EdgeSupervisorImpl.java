package org.gephi.preview.supervisors;

import java.awt.Font;
import java.util.Set;
import org.gephi.preview.EdgeImpl;
import org.gephi.preview.EdgeLabelImpl;
import org.gephi.preview.api.EdgeChildColorizer;
import org.gephi.preview.api.EdgeColorizer;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.supervisors.EdgeSupervisor;
import org.gephi.preview.api.supervisors.GlobalEdgeSupervisor;
import org.gephi.preview.updaters.LabelFontAdjuster;
import org.gephi.preview.updaters.LabelShortener;
import org.openide.util.Lookup;

/**
 * Edge supervisor implementation.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public abstract class EdgeSupervisorImpl implements EdgeSupervisor {

    protected Boolean curvedFlag;
    protected EdgeColorizer colorizer;
    protected Boolean showLabelsFlag;
    protected Boolean shortenLabelsFlag;
    protected Integer labelMaxChar;
    protected Font baseLabelFont;
    protected EdgeChildColorizer labelColorizer;

    /**
     * Adds the given edge to the list of the supervised edges.
     *
     * It updates the edges with the supervisor's values.
     *
     * @param edge  the edge to supervise
     */
    public void addEdge(EdgeImpl edge) {
        getSupervisedEdges().add(edge);
        colorEdge(edge);
        colorEdgeLabel(edge);
        updateEdgeLabelValue(edge);
        adjustEdgeLabelFont(edge);
    }

    public void clearSupervised() {
        getSupervisedEdges().clear();
    }

    /**
     * Returns the global edge supervisor.
     *
     * @return the controller's global edge supervisor
     */
    public GlobalEdgeSupervisor getGlobalEdgeSupervisor() {
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        return controller.getModel().getGlobalEdgeSupervisor();
    }

    public Boolean getShowFlag() {
        return getGlobalEdgeSupervisor().getShowFlag();
    }

    public Boolean getCurvedFlag() {
        return curvedFlag;
    }

    public void setCurvedFlag(Boolean value) {
        curvedFlag = value;
    }

    public EdgeColorizer getColorizer() {
        return colorizer;
    }

    public void setColorizer(EdgeColorizer value) {
        colorizer = value;
        colorEdges();
    }

    public Boolean getShowLabelsFlag() {
        return showLabelsFlag;
    }

    public void setShowLabelsFlag(Boolean value) {
        showLabelsFlag = value;
    }

    public Font getBaseLabelFont() {
        return baseLabelFont;
    }

    public void setBaseLabelFont(Font value) {
        baseLabelFont = value;
        adjustEdgeLabelFonts();
    }

    public Integer getLabelMaxChar() {
        return labelMaxChar;
    }

    public void setLabelMaxChar(Integer value) {
        labelMaxChar = value;
        updateEdgeLabelValues();
    }

    public Boolean getShortenLabelsFlag() {
        return shortenLabelsFlag;
    }

    public void setShortenLabelsFlag(Boolean value) {
        shortenLabelsFlag = value;
        updateEdgeLabelValues();
    }

    public EdgeChildColorizer getLabelColorizer() {
        return labelColorizer;
    }

    public void setLabelColorizer(EdgeChildColorizer value) {
        labelColorizer = value;
        colorEdgeLabels();
    }

    /**
     * Returns the set of the supervised edges.
     *
     * @return the set of the supervised edges
     */
    protected abstract Set<EdgeImpl> getSupervisedEdges();

    /**
     * Colors the given edge.
     *
     * @param edge  the edge to color
     */
    private void colorEdge(EdgeImpl edge) {
        colorizer.color(edge);
    }

    /**
     * Colors the supervised edges.
     */
    private void colorEdges() {
        for (EdgeImpl e : getSupervisedEdges()) {
            colorEdge(e);
        }
    }

    /**
     * Colors the given edge label.
     *
     * @param edgeLabel  the edge label to color
     */
    private void colorEdgeLabel(EdgeLabelImpl edgeLabel) {
        labelColorizer.color(edgeLabel);
    }

    /**
     * Colors the label of the given edge.
     *
     * @param edge  the edge to color the label
     */
    private void colorEdgeLabel(EdgeImpl edge) {
        if (edge.hasLabel()) {
            colorEdgeLabel(edge.getLabel());
        }
    }

    /**
     * Colors the labels of the supervised edges.
     */
    private void colorEdgeLabels() {
        for (EdgeImpl e : getSupervisedEdges()) {
            colorEdgeLabel(e);
        }
    }

    /**
     * Updates the edge label by shortening its value or by reverting its
     * original one.
     */
    private void updateEdgeLabelValue(EdgeImpl edge) {
        if (shortenLabelsFlag) {
            shortenEdgeLabel(edge);
        } else {
            revertEdgeLabel(edge);
        }
    }

    /**
     * Updates the edge labels by shortening their values or by reverting their
     * original ones.
     */
    private void updateEdgeLabelValues() {
        if (shortenLabelsFlag) {
            shortenEdgeLabels();
        } else {
            revertEdgeLabels();
        }
    }

    /**
     * Shortens the given edge label.
     *
     * @param edgeLabel  the edge label to shorten
     */
    private void shortenEdgeLabel(EdgeLabelImpl edgeLabel) {
        LabelShortener.shortenLabel(edgeLabel, labelMaxChar);
    }

    /**
     * Shortens the label of the given edge.
     *
     * @param edge  the edge to shorten the label
     */
    private void shortenEdgeLabel(EdgeImpl edge) {
        if (edge.hasLabel()) {
            shortenEdgeLabel(edge.getLabel());
        }
    }

    /**
     * Shortens the labels of the supervised edges.
     */
    private void shortenEdgeLabels() {
        for (EdgeImpl e : getSupervisedEdges()) {
            shortenEdgeLabel(e);
        }
    }

    /**
     * Reverts the original value of the the given edge label.
     *
     * @param edge  the edge label to revert the original value
     */
    private void revertEdgeLabel(EdgeLabelImpl edgeLabel) {
        LabelShortener.revertLabel(edgeLabel);
    }

    /**
     * Reverts the label of the given edge.
     *
     * @param edge  the edge to revert the label
     */
    private void revertEdgeLabel(EdgeImpl edge) {
        if (edge.hasLabel()) {
            revertEdgeLabel(edge.getLabel());
        }
    }

    /**
     * Reverts the labels of the supervised edges.
     */
    private void revertEdgeLabels() {
        for (EdgeImpl e : getSupervisedEdges()) {
            revertEdgeLabel(e);
        }
    }

    /**
     * Adjusts the font of the given edge label.
     *
     * @param label  the edge label to adjust the font
     */
    private void adjustEdgeLabelFont(EdgeLabelImpl label) {
        LabelFontAdjuster.adjustFont(label);
    }

    /**
     * Adjusts the label font of a given edge.
     *
     * @param edge  the edge to adjust the label font
     */
    private void adjustEdgeLabelFont(EdgeImpl edge) {
        if (edge.hasLabel()) {
            adjustEdgeLabelFont(edge.getLabel());
        }
    }

    /**
     * Adjusts the label fonts of the supervised edges.
     */
    private void adjustEdgeLabelFonts() {
        for (EdgeImpl e : getSupervisedEdges()) {
            adjustEdgeLabelFont(e);
        }
    }
}
