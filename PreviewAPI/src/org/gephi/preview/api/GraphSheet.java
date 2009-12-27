package org.gephi.preview.api;

/**
 * Interface of a preview graph sheet.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface GraphSheet {

    /**
     * Returns the preview graph.
     *
     * @return the preview graph
     */
    public Graph getGraph();

    /**
     * Returns the top left position of the graph sheet.
     *
     * @return the top left position of the graph sheet
     */
    public Point getTopLeftPosition();

    /**
     * Returns the bottom right position of the graph sheet.
     *
     * @return the bottom right position of the graph sheet
     */
    public Point getBottomRightPosition();
}
