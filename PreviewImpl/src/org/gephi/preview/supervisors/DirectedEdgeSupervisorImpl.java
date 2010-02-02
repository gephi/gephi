package org.gephi.preview.supervisors;

import java.awt.Font;
import java.util.HashSet;
import java.util.Set;
import org.gephi.preview.DirectedEdgeImpl;
import org.gephi.preview.EdgeArrowImpl;
import org.gephi.preview.EdgeMiniLabelImpl;
import org.gephi.preview.api.EdgeArrow;
import org.gephi.preview.api.EdgeChildColorizer;
import org.gephi.preview.api.EdgeColorizer;
import org.gephi.preview.api.EdgeMiniLabel;
import org.gephi.preview.api.SupervisorPropery;
import org.gephi.preview.api.supervisors.DirectedEdgeSupervisor;
import org.gephi.preview.propertyeditors.EdgeChildColorizerPropertyEditor;
import org.gephi.preview.propertyeditors.EdgeColorizerPropertyEditor;
import org.gephi.preview.updaters.LabelShortener;

/**
 * Directed edge supervisor implementation.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public abstract class DirectedEdgeSupervisorImpl extends EdgeSupervisorImpl
        implements DirectedEdgeSupervisor {

    protected Boolean showMiniLabelsFlag;
    protected Boolean shortenMiniLabelsFlag;
    protected Integer miniLabelMaxChar;
    protected Font miniLabelFont;
    protected Float miniLabelAddedRadius;
    protected EdgeChildColorizer miniLabelColorizer;
    protected Boolean showArrowsFlag;
    protected Float arrowAddedRadius;
    protected Float arrowSize;
    protected EdgeChildColorizer arrowColorizer;
    protected Set<DirectedEdgeImpl> supervisedEdges = new HashSet<DirectedEdgeImpl>();

    /**
     * @see EdgeSupervisorImpl#addEdge(org.gephi.preview.EdgeImpl)
     */
    public void addEdge(DirectedEdgeImpl edge) {
        super.addEdge(edge);
        positionEdgeMiniLabels(edge);
        colorEdgeMiniLabels(edge);
        updateEdgeMiniLabelValues(edge);
        positionEdgeArrows(edge);
        colorEdgeArrows(edge);
    }

    public Boolean getShowMiniLabelsFlag() {
        return showMiniLabelsFlag;
    }

    public void setShowMiniLabelsFlag(Boolean value) {
        showMiniLabelsFlag = value;
    }

    public Font getMiniLabelFont() {
        return miniLabelFont;
    }

    public void setMiniLabelFont(Font value) {
        miniLabelFont = value;
    }

    public Integer getMiniLabelMaxChar() {
        return miniLabelMaxChar;
    }

    public void setMiniLabelMaxChar(Integer value) {
        miniLabelMaxChar = value;
        updateEdgeMiniLabelValues();
    }

    public Boolean getShortenMiniLabelsFlag() {
        return shortenMiniLabelsFlag;
    }

    public void setShortenMiniLabelsFlag(Boolean value) {
        shortenMiniLabelsFlag = value;
        updateEdgeMiniLabelValues();
    }

    public Float getMiniLabelAddedRadius() {
        return miniLabelAddedRadius;
    }

    public void setMiniLabelAddedRadius(Float value) {
        miniLabelAddedRadius = value;
        positionEdgeMiniLabels();
    }

    public EdgeChildColorizer getMiniLabelColorizer() {
        return miniLabelColorizer;
    }

    public void setMiniLabelColorizer(EdgeChildColorizer value) {
        miniLabelColorizer = value;
        colorEdgeMiniLabels();
    }

    public Boolean getShowArrowsFlag() {
        return showArrowsFlag;
    }

    public void setShowArrowsFlag(Boolean value) {
        showArrowsFlag = value;
    }

    public Float getArrowAddedRadius() {
        return arrowAddedRadius;
    }

    public void setArrowAddedRadius(Float value) {
        arrowAddedRadius = value;
        positionEdgeArrows();
    }

    public Float getArrowSize() {
        return arrowSize;
    }

    public void setArrowSize(Float value) {
        arrowSize = value;
        positionEdgeArrows();
    }

    public EdgeChildColorizer getArrowColorizer() {
        return arrowColorizer;
    }

    public void setArrowColorizer(EdgeChildColorizer value) {
        arrowColorizer = value;
        colorEdgeArrows();
    }

    @Override
    protected Set getSupervisedEdges() {
        return supervisedEdges;
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
    private void positionEdgeMiniLabels(DirectedEdgeImpl edge) {
        for (EdgeMiniLabel ml : edge.getMiniLabels()) {
            positionEdgeMiniLabel((EdgeMiniLabelImpl) ml);
        }
    }

    /**
     * Generates the position of the supervised edges's mini-labels.
     */
    private void positionEdgeMiniLabels() {
        for (DirectedEdgeImpl e : supervisedEdges) {
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
    private void colorEdgeMiniLabels(DirectedEdgeImpl edge) {
        for (EdgeMiniLabel ml : edge.getMiniLabels()) {
            colorEdgeMiniLabel((EdgeMiniLabelImpl) ml);
        }
    }

    /**
     * Colors the mini-labels of the supervised edges.
     */
    private void colorEdgeMiniLabels() {
        for (DirectedEdgeImpl e : supervisedEdges) {
            colorEdgeMiniLabels(e);
        }
    }

    /**
     * Updates the edge mini-labels by shortening their value or by reverting
     * their original one.
     */
    private void updateEdgeMiniLabelValues(DirectedEdgeImpl edge) {
        if (shortenMiniLabelsFlag) {
            shortenEdgeMiniLabels(edge);
        } else {
            revertEdgeMiniLabels(edge);
        }
    }

    /**
     * Updates the edge mini-labels by shortening their value or by reverting
     * their original ones.
     */
    private void updateEdgeMiniLabelValues() {
        if (shortenMiniLabelsFlag) {
            shortenEdgeMiniLabels();
        } else {
            revertEdgeMiniLabels();
        }
    }

    /**
     * Shortens the given edge mini-label.
     *
     * @param edgeMiniLabel  the edge mini-label to shorten
     */
    private void shortenEdgeMiniLabel(EdgeMiniLabelImpl edgeMiniLabel) {
        LabelShortener.shortenLabel(edgeMiniLabel, miniLabelMaxChar);
    }

    /**
     * Shortens the mini-labels of the given edge.
     *
     * @param edge  the edge to shorten the mini-labels
     */
    private void shortenEdgeMiniLabels(DirectedEdgeImpl edge) {
        for (EdgeMiniLabel ml : edge.getMiniLabels()) {
            shortenEdgeMiniLabel((EdgeMiniLabelImpl) ml);
        }
    }

    /**
     * Shortens the mini-labels of the supervised edges.
     */
    private void shortenEdgeMiniLabels() {
        for (DirectedEdgeImpl e : supervisedEdges) {
            shortenEdgeMiniLabels(e);
        }
    }

    /**
     * Reverts the original value of the given edge mini-label.
     *
     * @param edgeMiniLabel  the edge mini-label to revert the original value
     */
    private void revertEdgeMiniLabel(EdgeMiniLabelImpl edgeMiniLabel) {
        LabelShortener.revertLabel(edgeMiniLabel);
    }

    /**
     * Reverts the mini-labels of the given edge.
     *
     * @param edge  the edge to revert the mini-labels
     */
    private void revertEdgeMiniLabels(DirectedEdgeImpl edge) {
        for (EdgeMiniLabel ml : edge.getMiniLabels()) {
            revertEdgeMiniLabel((EdgeMiniLabelImpl) ml);
        }
    }

    /**
     * Reverts the mini-labels of the supervised edges.
     */
    private void revertEdgeMiniLabels() {
        for (DirectedEdgeImpl e : supervisedEdges) {
            revertEdgeMiniLabels(e);
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
    private void positionEdgeArrows(DirectedEdgeImpl edge) {
        for (EdgeArrow a : edge.getArrows()) {
            positionEdgeArrow((EdgeArrowImpl) a);
        }
    }

    /**
     * Generates the position of the supervised edges's arrows.
     */
    private void positionEdgeArrows() {
        for (DirectedEdgeImpl e : supervisedEdges) {
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
    private void colorEdgeArrows(DirectedEdgeImpl edge) {
        for (EdgeArrow a : edge.getArrows()) {
            colorEdgeArrow((EdgeArrowImpl) a);
        }
    }

    /**
     * Colors the arrows of the supervised edges.
     */
    private void colorEdgeArrows() {
        for (DirectedEdgeImpl e : supervisedEdges) {
            colorEdgeArrows(e);
        }
    }

    public SupervisorPropery[] getProperties() {
        final String CATEGORY = "Directed";
        try {
            return new SupervisorPropery[]{
                        SupervisorPropery.createProperty(this, Boolean.class, "curvedFlag", CATEGORY, "Curved"),
                        SupervisorPropery.createProperty(this, Float.class, "edgeScale", CATEGORY, "Thickness"),
                        SupervisorPropery.createProperty(this, EdgeColorizer.class, "colorizer", CATEGORY, "Color", EdgeColorizerPropertyEditor.class),
                        SupervisorPropery.createProperty(this, Boolean.class, "showLabelsFlag", CATEGORY, "Labels"),
                        SupervisorPropery.createProperty(this, Boolean.class, "shortenLabelsFlag", CATEGORY, "Shorten labels"),
                        SupervisorPropery.createProperty(this, Integer.class, "labelMaxChar", CATEGORY, "Shorten limit"),
                        SupervisorPropery.createProperty(this, Font.class, "baseLabelFont", CATEGORY, "Font"),
                        SupervisorPropery.createProperty(this, EdgeChildColorizer.class, "labelColorizer", CATEGORY, "Label color", EdgeChildColorizerPropertyEditor.class),
                        SupervisorPropery.createProperty(this, Boolean.class, "showMiniLabelsFlag", CATEGORY, "Mini-Labels"),
                        SupervisorPropery.createProperty(this, Float.class, "miniLabelAddedRadius", CATEGORY, "Mini-Label radius"),
                        SupervisorPropery.createProperty(this, Boolean.class, "shortenMiniLabelsFlag", CATEGORY, "Shorten Mini-Labels"),
                        SupervisorPropery.createProperty(this, Integer.class, "miniLabelMaxChar", CATEGORY, "Mini-Label limit"),
                        SupervisorPropery.createProperty(this, Font.class, "miniLabelFont", CATEGORY, "Mini-Label font"),
                        SupervisorPropery.createProperty(this, EdgeChildColorizer.class, "miniLabelColorizer", CATEGORY, "Mini-Label color", EdgeChildColorizerPropertyEditor.class),
                        SupervisorPropery.createProperty(this, Boolean.class, "showArrowsFlag", CATEGORY, "Arrows"),
                        SupervisorPropery.createProperty(this, Float.class, "arrowAddedRadius", CATEGORY, "Arrow added radius"),
                        SupervisorPropery.createProperty(this, Float.class, "arrowSize", CATEGORY, "Arrow size"),
                        SupervisorPropery.createProperty(this, EdgeChildColorizer.class, "arrowColorizer", CATEGORY, "Arrow color", EdgeChildColorizerPropertyEditor.class)};
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new SupervisorPropery[0];
    }
}
