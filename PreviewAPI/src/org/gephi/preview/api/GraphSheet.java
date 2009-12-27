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

    /**
     * Returns the sheet's width.
     *
     * @return the sheet's width
     */
    public Float getWidth();

    /**
     * Returns the sheet's height.
     *
     * @return the sheet's height
     */
    public Float getHeight();

    /**
     * Defines the sheet's margin.
     *
     * @param value  the value of the sheet's margin
     */
    public void setMargin(float value);
}
