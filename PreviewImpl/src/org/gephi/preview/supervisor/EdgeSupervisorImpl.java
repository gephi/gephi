package org.gephi.preview.supervisor;

import java.awt.Font;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import org.gephi.preview.EdgeArrowImpl;
import org.gephi.preview.EdgeImpl;
import org.gephi.preview.EdgeLabelImpl;
import org.gephi.preview.EdgeMiniLabelImpl;
import org.gephi.preview.api.EdgeArrow;
import org.gephi.preview.api.EdgeMiniLabel;
import org.gephi.preview.api.color.colorizer.EdgeChildColorizer;
import org.gephi.preview.api.color.colorizer.EdgeColorizer;
import org.gephi.preview.api.controller.PreviewController;
import org.gephi.preview.api.supervisor.EdgeSupervisor;
import org.gephi.preview.api.supervisor.GlobalEdgeSupervisor;
import org.gephi.preview.util.LabelShortener;
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
    protected Integer labelMaxChar;
    protected Font labelFont;
    protected EdgeChildColorizer labelColorizer;
    protected Boolean showMiniLabelsFlag;
    protected Integer miniLabelMaxChar;
    protected Font miniLabelFont;
    protected Float miniLabelAddedRadius;
    protected EdgeChildColorizer miniLabelColorizer;
    protected Boolean showArrowsFlag;
    protected Float arrowAddedRadius;
    protected Float arrowSize;
    protected EdgeChildColorizer arrowColorizer;
    protected Set<EdgeImpl> supervisedEdges = Collections.newSetFromMap(new WeakHashMap<EdgeImpl, Boolean>());

    /**
	 * Adds the given edge to the list of the supervised edges.
	 *
	 * It updates the edges with the supervisor's values.
	 *
	 * @param edge  the edge to supervise
	 */
	public void addEdge(EdgeImpl edge) {
        supervisedEdges.add(edge);
        colorEdge(edge);
        colorEdgeLabel(edge);
        shortenEdgeLabel(edge);
        positionEdgeMiniLabels(edge);
        colorEdgeMiniLabels(edge);
        shortenEdgeMiniLabels(edge);
        positionEdgeArrows(edge);
        colorEdgeArrows(edge);
    }

    /**
     * Returns the global edge supervisor.
     *
     * @return the controller's global edge supervisor
     */
    public GlobalEdgeSupervisor getGlobalEdgeSupervisor() {
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        return controller.getGlobalEdgeSupervisor();
    }

    /**
     * Returns true if the edges must be displayed in the preview.
     *
     * @return true if the edges must be displayed in the preview
     */
    public Boolean getShowFlag() {
        return getGlobalEdgeSupervisor().getShowFlag();
    }

    /**
     * Returns true if the edges are curved.
     * 
     * @return true if the edges are curved
     */
    public Boolean getCurvedFlag() {
        return curvedFlag;
    }

    /**
	 * Defines if the edges are curved.
	 *
	 * @param value  true for curved edges
	 */
    public void setCurvedFlag(Boolean value) {
        curvedFlag = value;
    }

    /**
     * Returns the edge colorizer.
     *
     * @return the edge colorizer
     */
    public EdgeColorizer getColorizer() {
        return colorizer;
    }

    /**
	 * Defines the edge colorizer.
	 *
	 * @param value  the edge colorizer to set
	 */
    public void setColorizer(EdgeColorizer value) {
        colorizer = value;
        colorEdges();
    }

    /**
     * Returns true if the edge labels must be displayed in the preview.
     *
     * @return true if the edge labels must be displayed in the preview
     */
    public Boolean getShowLabelsFlag() {
        return showLabelsFlag;
    }

    /**
	 * Defines if the edge labels must be displayed in the preview.
	 *
	 * @param value  true to display the edge labels in the preview
	 */
    public void setShowLabelsFlag(Boolean value) {
        showLabelsFlag = value;
    }

    /**
     * Returns the edge label font.
     *
     * @return the edge label font
     */
    public Font getLabelFont() {
        return labelFont;
    }

    /**
	 * Defines the edge label font.
	 *
	 * @param value  the edge label font to set
	 */
    public void setLabelFont(Font value) {
        labelFont = value;
    }

    /**
     * Returns the edge label character limit.
     *
     * @return the edge label character limit
     */
    public Integer getLabelMaxChar() {
        return labelMaxChar;
    }

    /**
	 * Defines the edge label character limit.
	 *
	 * @param value  the edge label character limit to set
	 */
    public void setLabelMaxChar(Integer value) {
        labelMaxChar = value;
        shortenEdgeLabels();
    }

    /**
     * Returns the edge label colorizer.
     *
     * @return the edge label colorizer
     */
    public EdgeChildColorizer getLabelColorizer() {
        return labelColorizer;
    }

    /**
	 * Defines the edge label colorizer.
	 *
	 * @param value  the edge label colorizer to set
	 */
    public void setLabelColorizer(EdgeChildColorizer value) {
        labelColorizer = value;
        colorEdgeLabels();
    }

    /**
     * Returns true if the edge mini-labels must be displayed in the preview.
     *
     * @return true if the edge mini-labels must be displayed in the preview.
     */
    public Boolean getShowMiniLabelsFlag() {
        return showMiniLabelsFlag;
    }

    /**
	 * Defines if the edge mini-labels must be displayed in the preview.
	 *
	 * @param value  true to display the edge mini-labels in the preview
	 */
    public void setShowMiniLabelsFlag(Boolean value) {
        showMiniLabelsFlag = value;
    }

    /**
     * Returns the edge mini-label font.
     *
     * @return the edge mini-label font
     */
    public Font getMiniLabelFont() {
        return miniLabelFont;
    }

    /**
	 * Defines the edge mini-label font.
	 *
	 * @param value  the edge mini-label font to set
	 */
    public void setMiniLabelFont(Font value) {
        miniLabelFont = value;
    }

    /**
     * Returns the edge mini-label character limit.
     *
     * @return the edge mini-label character limit
     */
    public Integer getMiniLabelMaxChar() {
        return miniLabelMaxChar;
    }

    /**
	 * Defines the edge mini-label character limit.
	 *
	 * @param value  the edge mini-label character limit
	 */
    public void setMiniLabelMaxChar(Integer value) {
        miniLabelMaxChar = value;
        shortenEdgeMiniLabels();
    }

    /**
     * Returns the edge mini-label added radius.
     *
     * @return the edge mini-label added radius
     */
    public Float getMiniLabelAddedRadius() {
        return miniLabelAddedRadius;
    }

    /**
	 * Defines the edge mini-label added radius.
	 *
	 * @param value  the edge mini-label added radius to set
	 */
    public void setMiniLabelAddedRadius(Float value) {
        miniLabelAddedRadius = value;
        positionEdgeMiniLabels();
    }

    /**
     * Returns the edge mini-label colorizer.
     *
     * @return the edge mini-label colorizer
     */
    public EdgeChildColorizer getMiniLabelColorizer() {
        return miniLabelColorizer;
    }

    /**
	 * Defines the edge mini-label colorizer.
	 *
	 * @param value  the edge mini-label colorizer to set
	 */
    public void setMiniLabelColorizer(EdgeChildColorizer value) {
        miniLabelColorizer = value;
        colorEdgeMiniLabels();
    }

    /**
     * Returns true if the edge arrows must be displayed in the preview.
     *
     * @return true if the edge arrows must be displayed in the preview
     */
    public Boolean getShowArrowsFlag() {
        return showArrowsFlag;
    }

    /**
	 * Defines if the edge arrows must be displayed in the preview.
	 *
	 * @param value  true to display the edge arrows in the preview
	 */
    public void setShowArrowsFlag(Boolean value) {
        showArrowsFlag = value;
    }

    /**
     * Returns the edge arrow added radius.
     *
     * @return the edge arrow added radius
     */
    public Float getArrowAddedRadius() {
        return arrowAddedRadius;
    }

    /**
	 * Defines the edge arrow added radius.
	 *
	 * @param value  the edge arrow added radius to set
	 */
    public void setArrowAddedRadius(Float value) {
        arrowAddedRadius = value;
        positionEdgeArrows();
    }

    /**
     * Returns the edge arrow size.
     *
     * @return the edge arrow size
     */
    public Float getArrowSize() {
        return arrowSize;
    }

    /**
	 * Defines the edge arrow size.
	 *
	 * @param value  the edge arrow size to set
	 */
    public void setArrowSize(Float value) {
        arrowSize = value;
        positionEdgeArrows();
    }

    /**
     * Returns the edge arrow colorizer.
     *
     * @return the edge arrow colorizer
     */
    public EdgeChildColorizer getArrowColorizer() {
        return arrowColorizer;
    }

    /**
	 * Defines the edge arrow colorizer.
	 *
	 * @param value  the edge arrow colorizer to set
	 */
    public void setArrowColorizer(EdgeChildColorizer value) {
        arrowColorizer = value;
        colorEdgeArrows();
    }

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
        for (EdgeImpl e : supervisedEdges) {
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
        colorEdgeLabel(edge.getLabel());
    }

    /**
	 * Colors the labels of the supervised edges.
	 */
    private void colorEdgeLabels() {
        for (EdgeImpl e : supervisedEdges) {
            colorEdgeLabel(e);
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
        shortenEdgeLabel(edge.getLabel());
    }

	/**
	 * Shortens the labels of the supervised edges.
	 */
    private void shortenEdgeLabels() {
        for (EdgeImpl e : supervisedEdges) {
            shortenEdgeLabel(e);
        }
    }

    /**
	 * Generates the position of the given edge mini-label.
	 *
	 * @param edgeMiniLabel  the edge mini-label to position
	 */
	private void positionEdgeMiniLabel(EdgeMiniLabelImpl edgeMiniLabel) {
        edgeMiniLabel.genPosition();
    }

    /**
	 * Generates the position of the given edge's mini-labels.
     *
     * @param edge  the edge to position the mini-labels
	 */
    private void positionEdgeMiniLabels(EdgeImpl edge) {
        for (EdgeMiniLabel ml : edge.getMiniLabels()) {
            positionEdgeMiniLabel((EdgeMiniLabelImpl) ml);
        }
    }

	/**
	 * Generates the position of the supervised edges's mini-labels.
	 */
    private void positionEdgeMiniLabels() {
        for (EdgeImpl e : supervisedEdges) {
            positionEdgeMiniLabels(e);
        }
    }

    /**
	 * Colors the given edge mini-label.
	 *
	 * @param edgeMiniLabel  the edge mini-label to color
	 */
	private void colorEdgeMiniLabel(EdgeMiniLabelImpl edgeMiniLabel) {
        miniLabelColorizer.color(edgeMiniLabel);
    }

    /**
	 * Colors the mini-labels of the given edge.
     *
     * @param edge  the edge to color the mini-labels
	 */
    private void colorEdgeMiniLabels(EdgeImpl edge) {
        for (EdgeMiniLabel ml : edge.getMiniLabels()) {
            colorEdgeMiniLabel((EdgeMiniLabelImpl) ml);
        }
    }

	/**
	 * Colors the mini-labels of the supervised edges.
	 */
    private void colorEdgeMiniLabels() {
        for (EdgeImpl e : supervisedEdges) {
            colorEdgeMiniLabels(e);
        }
    }

    /**
	 * Shortens the given edge mini-label.
	 *
	 * @param edgeMiniLabel  the edge mini-label to color
	 */
	private void shortenEdgeMiniLabel(EdgeMiniLabelImpl edgeMiniLabel) {
        LabelShortener.shortenLabel(edgeMiniLabel, miniLabelMaxChar);
    }

    /**
	 * Shortens the mini-labels of the given edge.
     *
     * @param edge  the edge to shorten the mini-labels
	 */
    private void shortenEdgeMiniLabels(EdgeImpl edge) {
        for (EdgeMiniLabel ml : edge.getMiniLabels()) {
            shortenEdgeMiniLabel((EdgeMiniLabelImpl) ml);
        }
    }

	/**
	 * Shortens the mini-labels of the supervised edges.
	 */
    private void shortenEdgeMiniLabels() {
        for (EdgeImpl e : supervisedEdges) {
            shortenEdgeMiniLabels(e);
        }
    }

    /**
	 * Generates the position of the given edge arrow.
	 *
	 * @param edgeArrow  the edge arrow to position
	 */
	private void positionEdgeArrow(EdgeArrowImpl edgeArrow) {
        edgeArrow.genPosition();
    }

    /**
	 * Generates the position of the given edge's arrows.
     *
     * @param edge  the edge to position the arrows
	 */
    private void positionEdgeArrows(EdgeImpl edge) {
        for (EdgeArrow a : edge.getArrows()) {
            positionEdgeArrow((EdgeArrowImpl) a);
        }
    }

	/**
	 * Generates the position of the supervised edges's arrows.
	 */
    private void positionEdgeArrows() {
        for (EdgeImpl e : supervisedEdges) {
            positionEdgeArrows(e);
        }
    }

    /**
	 * Colors the given edge arrow.
	 *
	 * @param edgeArrow  the edge arrow to color
	 */
	private void colorEdgeArrow(EdgeArrowImpl edgeArrow) {
        arrowColorizer.color(edgeArrow);
    }

    /**
	 * Colors the arrows of the given edge.
     *
     * @param edge  the edge to color the arrows
	 */
    private void colorEdgeArrows(EdgeImpl edge) {
        for (EdgeArrow a : edge.getArrows()) {
            colorEdgeArrow((EdgeArrowImpl) a);
        }
    }

	/**
	 * Colors the arrows of the supervised edges.
	 */
    private void colorEdgeArrows() {
        for (EdgeImpl e : supervisedEdges) {
            colorEdgeArrows(e);
        }
    }
}
