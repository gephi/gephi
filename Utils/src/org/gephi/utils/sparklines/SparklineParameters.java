/*
Copyright 2008-2011 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.utils.sparklines;

import java.awt.Color;

/**
 * Sparkline rendering options:
 * <ul>
 * <li>Width and height of the graphic in pixels</li>
 * <li>Line color. Blue by default if null</li>
 * <li>Color for the background of the graphic. White by default if null</li>
 * <li>Highlight colors for min and max values, null (no highlight) by default</li>
 * <li>Highlight colors for a value closest to the given x pixel position(<code>higlightedValueXPosition</code>), magenta by default if null and <code>higlightedValueXPosition</code> is not null</li>
 * <li>Highlighted value text color, used if <code>higlightedValueXPosition</code> is provided</li>
 * <li>Highlighted value text box color, used if <code>higlightedValueXPosition</code> and <code>highlightTextColor</code> are provided</li>
 * </ul>
 * Several constructors are provided for various use cases.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class SparklineParameters {

    public static final Color DEFAULT_LINE_COLOR = Color.BLUE;
    public static final Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    public static final Color DEFAULT_HIGHLIGHT_VALUE_COLOR = Color.MAGENTA;
    public static final Color DEFAULT_TEXT_COLOR = Color.BLACK;
    public static final Color DEFAULT_TEXT_BOX_COLOR = new Color(1, 1, 1, .75f);//Semi transparent white
    
    private int width, height;
    private Color lineColor;
    private Color backgroundColor;
    private Color higlightMinColor, higlightMaxColor;
    private Integer higlightedValueXPosition;
    private Color highligtValueColor;
    private Color highlightTextColor;
    private Color highlightTextBoxColor;

    /**
     * Create a simple sparkline parameters with only lines
     * @param width Width in pixels
     * @param height Height in pixels
     */
    public SparklineParameters(int width, int height) {
        this(width, height, null, null, null, null);
    }

    /**
     * Create a simple sparkline parameters with only lines and a specific line color
     * @param width Width in pixels
     * @param height Height in pixels
     * @param lineColor Lines color
     */
    public SparklineParameters(int width, int height, Color lineColor) {
        this(width, height, lineColor, null, null, null);
    }

    /**
     * Create a simple sparkline parameters with only lines and a specific line color and background color
     * @param width Width in pixels
     * @param height Height in pixels
     * @param lineColor Lines color
     * @param backgroundColor Background color
     */
    public SparklineParameters(int width, int height, Color lineColor, Color backgroundColor) {
        this(width, height, lineColor, backgroundColor, null, null);
    }

    /**
     * Create a sparkline parameters specifying colors for line, background, and max/min highlight colors (no highlight if null)
     * @param width Width in pixels
     * @param height Height in pixels
     * @param lineColor Lines color
     * @param backgroundColor Background color
     * @param higlightMinColor Min value highlight color or null
     * @param higlightMaxColor Max value highlight color or null
     */
    public SparklineParameters(int width, int height, Color lineColor, Color backgroundColor, Color higlightMinColor, Color higlightMaxColor) {
        this(width, height, lineColor, backgroundColor, higlightMinColor, higlightMaxColor, null, null, null, null);
    }
    
    /**
     * Create a sparkline parameters specifying colors for line, background, and max/min highlight colors (no highlight if null)
     * and a x pixel position to highlight closest value with default highlight, text and text box colors.
     * @param width Width in pixels
     * @param height Height in pixels
     * @param lineColor Lines color
     * @param backgroundColor Background color
     * @param higlightMinColor Min value highlight color or null
     * @param higlightMaxColor Max value highlight color or null
     * @param higlightedValueXPosition X position in pixels to find closest value in the sparkline
     */
    public SparklineParameters(int width, int height, Color lineColor, Color backgroundColor, Color higlightMinColor, Color higlightMaxColor, Integer higlightedValueXPosition) {
        this(width, height, lineColor, backgroundColor, higlightMinColor, higlightMaxColor, higlightedValueXPosition, null, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_BOX_COLOR);
    }

    /**
     * Create a sparkline parameters specifying colors for line, background, and max/min highlight colors (no highlight if null)
     * and a x pixel position to highlight closest value with specific highlight, text and text box colors.
     * @param width Width in pixels
     * @param height Height in pixels
     * @param lineColor Lines color
     * @param backgroundColor Background color
     * @param higlightMinColor Min value highlight color or null
     * @param higlightMaxColor Max value highlight color or null
     * @param higlightedValueXPosition X position in pixels to find closest value in the sparkline
     * @param highligtValueColor Highlighted value color (Magenta if null)
     * @param highlightTextColor Highlighted value text color or null
     * @param highlightTextBoxColor Highlighted value text box color or null
     */
    public SparklineParameters(int width, int height, Color lineColor, Color backgroundColor, Color higlightMinColor, Color higlightMaxColor, Integer higlightedValueXPosition, Color highligtValueColor, Color highlightTextColor, Color highlightTextBoxColor) {
        this.width = width;
        this.height = height;
        this.lineColor = lineColor;
        this.backgroundColor = backgroundColor;
        this.higlightMinColor = higlightMinColor;
        this.higlightMaxColor = higlightMaxColor;
        this.higlightedValueXPosition = higlightedValueXPosition;
        this.highligtValueColor = highligtValueColor;
        this.highlightTextColor = highlightTextColor;
        this.highlightTextBoxColor = highlightTextBoxColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Color getHighlightTextBoxColor() {
        return highlightTextBoxColor;
    }

    public void setHighlightTextBoxColor(Color highlightTextBoxColor) {
        this.highlightTextBoxColor = highlightTextBoxColor;
    }

    public Color getHighlightTextColor() {
        return highlightTextColor;
    }

    public void setHighlightTextColor(Color highlightTextColor) {
        this.highlightTextColor = highlightTextColor;
    }

    public Color getHighligtValueColor() {
        return highligtValueColor;
    }

    public void setHighligtValueColor(Color highligtValueColor) {
        this.highligtValueColor = highligtValueColor;
    }

    public Color getHiglightMaxColor() {
        return higlightMaxColor;
    }

    public void setHiglightMaxColor(Color higlightMaxColor) {
        this.higlightMaxColor = higlightMaxColor;
    }

    public Color getHiglightMinColor() {
        return higlightMinColor;
    }

    public void setHiglightMinColor(Color higlightMinColor) {
        this.higlightMinColor = higlightMinColor;
    }

    public Integer getHiglightedValueXPosition() {
        return higlightedValueXPosition;
    }

    public void setHiglightedValueXPosition(Integer higlightedValueXPosition) {
        this.higlightedValueXPosition = higlightedValueXPosition;
    }

    public Color getLineColor() {
        return lineColor;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
