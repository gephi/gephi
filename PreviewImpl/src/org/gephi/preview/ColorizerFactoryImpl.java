package org.gephi.preview;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gephi.preview.api.Colorizer;
import org.gephi.preview.api.ColorizerFactory;
import org.gephi.preview.updaters.CustomColorMode;
import org.gephi.preview.updaters.EdgeB1ColorMode;
import org.gephi.preview.updaters.EdgeB2ColorMode;
import org.gephi.preview.updaters.EdgeBothBColorMode;
import org.gephi.preview.updaters.NodeOriginalColorMode;
import org.gephi.preview.updaters.ParentColorMode;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of the colorizer factory.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
@ServiceProvider(service = ColorizerFactory.class)
public class ColorizerFactoryImpl implements ColorizerFactory {

    /**
     * Returns whether or not the given string matches with the given color mode
     * identifier.
     *
     * @param s           the string to compare
     * @param identifier  the color mode identifier
     * @return            true if the given string matches with the color mode identifier
     */
    private boolean matchColorMode(String s, String identifier) {
        String regexp = String.format("\\s*%s\\s*", identifier);
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(s);
        return m.lookingAt();
    }

    /**
     * Returns whether or not the given string matches with the
     * CustomColorMode's identifier.
     *
     * @param s  the string to compare
     * @return   true if the given string matches with the CustomColorMode's identifier
     */
    public boolean matchCustomColorMode(String s) {
        return matchColorMode(s, CustomColorMode.getIdentifier());
    }

    /**
     * Returns whether or not the given string matches with the
     * NodeOriginalColorMode's identifier.
     *
     * @param s  the string to compare
     * @return   true if the given string matches with the NodeOriginalColorMode's identifier
     */
    public boolean matchNodeOriginalColorMode(String s) {
        return matchColorMode(s, NodeOriginalColorMode.getIdentifier());
    }

    /**
     * Returns whether or not the given string matches with the
     * ParentColorMode's identifier.
     *
     * @param s  the string to compare
     * @return   true if the given string matches with the ParentColorMode's identifier
     */
    public boolean matchParentColorMode(String s) {
        return matchColorMode(s, ParentColorMode.getIdentifier());
    }

    /**
     * Returns whether or not the given string matches with the
     * EdgeB1ColorMode's identifier.
     *
     * @param s  the string to compare
     * @return   true if the given string matches with the EdgeB1ColorMode's identifier
     */
    public boolean matchEdgeB1ColorMode(String s) {
        return matchColorMode(s, EdgeB1ColorMode.getIdentifier());
    }

    /**
     * Returns whether or not the given string matches with the
     * EdgeB2ColorMode's identifier.
     *
     * @param s  the string to compare
     * @return   true if the given string matches with the EdgeB2ColorMode's identifier
     */
    public boolean matchEdgeB2ColorMode(String s) {
        return matchColorMode(s, EdgeB2ColorMode.getIdentifier());
    }

    /**
     * Returns whether or not the given string matches with the
     * EdgeBothBColorMode's identifier.
     *
     * @param s  the string to compare
     * @return   true if the given string matches with the EdgeBothBColorMode's identifier
     */
    public boolean matchEdgeBothBColorMode(String s) {
        return matchColorMode(s, EdgeBothBColorMode.getIdentifier());
    }

    /**
     * Returns whether or not the given colorizer is a CustomColorMode.
     *
     * @param colorizer  the colorizer to identify
     * @return           true if the given colorizer is a CustomColorMode
     */
    public boolean isCustomColorMode(Colorizer colorizer) {
        return matchCustomColorMode(colorizer.toString());
    }

    /**
     * Returns whether or not the given colorizer is a NodeOriginalColorMode.
     *
     * @param colorizer  the colorizer to identify
     * @return           true if the given colorizer is a NodeOriginalColorMode
     */
    public boolean isNodeOriginalColorMode(Colorizer colorizer) {
        return matchNodeOriginalColorMode(colorizer.toString());
    }

    /**
     * Returns whether or not the given colorizer is a ParentColorMode.
     *
     * @param colorizer  the colorizer to identify
     * @return           true if the given colorizer is a ParentColorMode
     */
    public boolean isParentColorMode(Colorizer colorizer) {
        return matchParentColorMode(colorizer.toString());
    }

    /**
     * Returns whether or not the given colorizer is a EdgeB1ColorMode.
     *
     * @param colorizer  the colorizer to identify
     * @return           true if the given colorizer is a EdgeB1ColorMode
     */
    public boolean isEdgeB1ColorMode(Colorizer colorizer) {
        return matchEdgeB1ColorMode(colorizer.toString());
    }

    /**
     * Returns whether or not the given colorizer is a EdgeB2ColorMode.
     *
     * @param colorizer  the colorizer to identify
     * @return           true if the given colorizer is a EdgeB2ColorMode
     */
    public boolean isEdgeB2ColorMode(Colorizer colorizer) {
        return matchEdgeB2ColorMode(colorizer.toString());
    }

    /**
     * Returns whether or not the given colorizer is a EdgeBothBColorMode.
     *
     * @param colorizer  the colorizer to identify
     * @return           true if the given colorizer is a EdgeBothBColorMode
     */
    public boolean isEdgeBothBColorMode(Colorizer colorizer) {
        return matchEdgeBothBColorMode(colorizer.toString());
    }

    /**
     * Creates a CustomColorMode colorizer from the given color components.
     *
     * @param r  the red color component
     * @param g  the green color component
     * @param b  the blue color component
     * @return   a CustomColorMode colorizer
     */
    public Colorizer createCustomColorMode(int r, int g, int b) {
        return new CustomColorMode(r, g, b);
    }

    /**
     * Creates a CustomColorMode colorizer from the given color object.
     *
     * @param color  the color object
     * @return       a CustomColorMode colorizer
     */
    public Colorizer createCustomColorMode(java.awt.Color color) {
        return createCustomColorMode(color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Creates a NodeOriginalColorMode colorizer.
     *
     * @return a NodeOriginalColorMode colorizer
     */
    public Colorizer createNodeOriginalColorMode() {
        return new NodeOriginalColorMode();
    }

    /**
     * Creates a ParentColorMode colorizer.
     *
     * @return a ParentColorMode colorizer
     */
    public Colorizer createParentColorMode() {
        return new ParentColorMode();
    }

    /**
     * Creates a EdgeB1ColorMode colorizer.
     *
     * @return a EdgeB1ColorMode colorizer
     */
    public Colorizer createEdgeB1ColorMode() {
        return new EdgeB1ColorMode();
    }

    /**
     * Creates a EdgeB2ColorMode colorizer.
     *
     * @return a EdgeB2ColorMode colorizer
     */
    public Colorizer createEdgeB2ColorMode() {
        return new EdgeB2ColorMode();
    }

    /**
     * Creates a EdgeBothBColorMode colorizer.
     *
     * @return a EdgeBothBColorMode colorizer
     */
    public Colorizer createEdgeBothBColorMode() {
        return new EdgeBothBColorMode();
    }
}
