package org.gephi.preview.api;

/**
 * Interface of a preview edge.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface Edge {

    /**
     * Returns whether or not the edge has a label.
     *
     * @return true if the edge has a label
     */
    public boolean hasLabel();

    /**
     * Returns the edge's thickness.
     *
     * @return the edge's thickness
     */
    public Float getThickness();

    /**
     * Returns the edge's color.
     *
     * @return the edge's color
     */
    public Color getColor();

    /**
     * Returns the edge's label.
     *
     * @return the edge's label
     */
    public EdgeLabel getLabel();

    /**
     * Returns the edge's node 1.
     *
     * @return the edge's node 1
     */
    public Node getNode1();

    /**
     * Returns the edge's node 2.
     *
     * @return the edge's node 2
     */
    public Node getNode2();

    /**
     * Returns an iterable on the arrow list of the edge.
     *
     * @return an iterable on the arrow list of the edge
     */
    public Iterable<EdgeArrow> getArrows();

    /**
     * Returns an iterable on the mini-label list of the edge.
     *
     * @return an iterable on the mini-label list of the edge
     */
    public Iterable<EdgeMiniLabel> getMiniLabels();

    /**
     * Returns an iterable on the curve list of the edge.
     *
     * @return an iterable on the curve list of the edge
     */
    public Iterable<CubicBezierCurve> getCurves();

    /**
     * Returns whether or not the edge should be displayed as a curve.
     *
     * @return true if the edge should be displayed as a curve
     */
    public Boolean isCurved();

    /**
     * Returns whether or not the edge's arrows should be displayed.
     *
     * @return true if the edge's arrows should be displayed
     */
    public Boolean showArrows();

    /**
     * Returns whether or not the edge's label should be displayed.
     *
     * @return true if the edge's label should be displayed
     */
    public Boolean showLabel();

    /**
     * Returns whether or not the edge's mini-labels should be displayed.
     * 
     * @return true if the edge's mini-labels should be displayed
     */
    public Boolean showMiniLabels();
}
