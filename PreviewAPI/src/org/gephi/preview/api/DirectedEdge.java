package org.gephi.preview.api;

/**
 * Interface of a preview edge.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface DirectedEdge extends Edge {

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
     * Returns whether or not the edge's arrows should be displayed.
     *
     * @return true if the edge's arrows should be displayed
     */
    public Boolean showArrows();

    /**
     * Returns whether or not the edge's mini-labels should be displayed.
     * 
     * @return true if the edge's mini-labels should be displayed
     */
    public Boolean showMiniLabels();
}
