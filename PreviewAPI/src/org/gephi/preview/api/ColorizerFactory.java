package org.gephi.preview.api;

/**
 * Interface of a colorizer factory.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface ColorizerFactory {

    /**
     * Returns whether or not the given string matches with the
     * CustomColorMode's identifier.
     *
     * @param s  the string to compare
     * @return   true if the given string matches with the CustomColorMode's identifier
     */
    public boolean matchCustomColorMode(String s);

    /**
     * Returns whether or not the given string matches with the
     * NodeOriginalColorMode's identifier.
     *
     * @param s  the string to compare
     * @return   true if the given string matches with the NodeOriginalColorMode's identifier
     */
    public boolean matchNodeOriginalColorMode(String s);

    /**
     * Returns whether or not the given string matches with the
     * ParentColorMode's identifier.
     *
     * @param s  the string to compare
     * @return   true if the given string matches with the ParentColorMode's identifier
     */
    public boolean matchParentColorMode(String s);

    /**
     * Returns whether or not the given string matches with the
     * EdgeB1ColorMode's identifier.
     *
     * @param s  the string to compare
     * @return   true if the given string matches with the EdgeB1ColorMode's identifier
     */
    public boolean matchEdgeB1ColorMode(String s);

    /**
     * Returns whether or not the given string matches with the
     * EdgeB2ColorMode's identifier.
     *
     * @param s  the string to compare
     * @return   true if the given string matches with the EdgeB2ColorMode's identifier
     */
    public boolean matchEdgeB2ColorMode(String s);

    /**
     * Returns whether or not the given string matches with the
     * EdgeBothBColorMode's identifier.
     *
     * @param s  the string to compare
     * @return   true if the given string matches with the EdgeBothBColorMode's identifier
     */
    public boolean matchEdgeBothBColorMode(String s);

    /**
     * Returns whether or not the given colorizer is a CustomColorMode.
     *
     * @param colorizer  the colorizer to identify
     * @return           true if the given colorizer is a CustomColorMode
     */
    public boolean isCustomColorMode(Colorizer colorizer);

    /**
     * Returns whether or not the given colorizer is a NodeOriginalColorMode.
     *
     * @param colorizer  the colorizer to identify
     * @return           true if the given colorizer is a NodeOriginalColorMode
     */
    public boolean isNodeOriginalColorMode(Colorizer colorizer);

    /**
     * Returns whether or not the given colorizer is a ParentColorMode.
     *
     * @param colorizer  the colorizer to identify
     * @return           true if the given colorizer is a ParentColorMode
     */
    public boolean isParentColorMode(Colorizer colorizer);

    /**
     * Returns whether or not the given colorizer is a EdgeB1ColorMode.
     *
     * @param colorizer  the colorizer to identify
     * @return           true if the given colorizer is a EdgeB1ColorMode
     */
    public boolean isEdgeB1ColorMode(Colorizer colorizer);

    /**
     * Returns whether or not the given colorizer is a EdgeB2ColorMode.
     *
     * @param colorizer  the colorizer to identify
     * @return           true if the given colorizer is a EdgeB2ColorMode
     */
    public boolean isEdgeB2ColorMode(Colorizer colorizer);

    /**
     * Returns whether or not the given colorizer is a EdgeBothBColorMode.
     *
     * @param colorizer  the colorizer to identify
     * @return           true if the given colorizer is a EdgeBothBColorMode
     */
    public boolean isEdgeBothBColorMode(Colorizer colorizer);

    /**
     * Creates a CustomColorMode colorizer from the given color components.
     *
     * @param r  the red color component
     * @param g  the green color component
     * @param b  the blue color component
     * @return   a CustomColorMode colorizer
     */
    public Colorizer createCustomColorMode(int r, int g, int b);

    /**
     * Creates a CustomColorMode colorizer from the given color object.
     *
     * @param color  the color object
     * @return       a CustomColorMode colorizer
     */
    public Colorizer createCustomColorMode(java.awt.Color color);

    /**
     * Creates a NodeOriginalColorMode colorizer.
     *
     * @return a NodeOriginalColorMode colorizer
     */
    public Colorizer createNodeOriginalColorMode();

    /**
     * Creates a ParentColorMode colorizer.
     *
     * @return a ParentColorMode colorizer
     */
    public Colorizer createParentColorMode();

    /**
     * Creates a EdgeB1ColorMode colorizer.
     *
     * @return a EdgeB1ColorMode colorizer
     */
    public Colorizer createEdgeB1ColorMode();

    /**
     * Creates a EdgeB2ColorMode colorizer.
     *
     * @return a EdgeB2ColorMode colorizer
     */
    public Colorizer createEdgeB2ColorMode();

    /**
     * Creates a EdgeBothBColorMode colorizer.
     *
     * @return a EdgeBothBColorMode colorizer
     */
    public Colorizer createEdgeBothBColorMode();
}
