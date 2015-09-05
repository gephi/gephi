/*
 Copyright 2008-2011 Gephi
 Authors : Eduardo Ramos <eduramiba@gmail.com>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.utils.sparklines;

import java.awt.Color;

/**
 * Sparkline rendering settings: 
 * <ul>
 * <li>Width and height of the graphic in pixels</li>
 * <li>Line color. Blue by default if null</li>
 * <li>Optional area color under the line, no area by default</li>
 * <li>Color for the background of the graphic. White by default if null</li>
 * <li>Highlight colors with a dot for min and max values, null (no highlight) by default</li>
 * <li>Highlight colors with a dot for a value closest to the given x pixel position(<code>highlightedValueXPosition</code>), magenta by default if null and <code>highlightedValueXPosition</code> is not null</li>
 * <li>Highlighted value text color, used if <code>highlightedValueXPosition</code> is provided. By default (x,y) values are shown, use <code>highlightTextMode</code> to control this.</li>
 * <li>Highlighted value text box color, used if <code>highlightedValueXPosition</code> and <code>highlightTextColor</code> are provided</li>
 * </ul>
 * Several constructors are provided for various use cases.
 * @see SparklineGraph
 * @author Eduardo Ramos
 */
public class SparklineParameters {

    /**
     * Defines what text is shown when highlightTextColor is not null.
     */
    public enum HighlightTextMode {

        X_VALUES,
        Y_VALUES,
        X_AND_Y_VALUES;
    }
    public static final Color DEFAULT_LINE_COLOR = Color.BLUE;
    public static final Color DEFAULT_AREA_COLOR = new Color(DEFAULT_LINE_COLOR.getRed(), DEFAULT_LINE_COLOR.getRed(), DEFAULT_LINE_COLOR.getBlue(), 50);
    public static final Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    public static final Color DEFAULT_HIGHLIGHT_VALUE_COLOR = Color.MAGENTA;
    public static final Color DEFAULT_TEXT_COLOR = Color.BLACK;
    public static final Color DEFAULT_TEXT_BOX_COLOR = new Color(1, 1, 1, .75f);//Semi transparent white
    public static final HighlightTextMode DEFAULT_HIGHLIGHT_TEXT_MODE = HighlightTextMode.X_AND_Y_VALUES;
    private int width, height;
    private Color lineColor;
    private Color areaColor;
    private Color backgroundColor;
    private boolean transparentBackground;
    private boolean drawArea;
    private Color highlightMinColor, highlightMaxColor;
    private Integer highlightedValueXPosition;
    private Color highligtValueColor;
    private Color highlightTextColor;
    private Color highlightTextBoxColor;
    private HighlightTextMode highlightTextMode;

    /**
     * Create a simple sparkline parameters with only lines
     *
     * @param width Width in pixels
     * @param height Height in pixels
     */
    public SparklineParameters(int width, int height) {
        this(width, height, null, null, null, null);
    }

    /**
     * Create a simple sparkline parameters with only lines and a specific line color
     *
     * @param width Width in pixels
     * @param height Height in pixels
     * @param lineColor Lines color
     */
    public SparklineParameters(int width, int height, Color lineColor) {
        this(width, height, lineColor, null, null, null);
    }

    /**
     * Create a simple sparkline parameters with only lines and a specific line color and background color
     *
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
     *
     * @param width Width in pixels
     * @param height Height in pixels
     * @param lineColor Lines color
     * @param backgroundColor Background color
     * @param highlightMinColor Min value highlight color or null
     * @param highlightMaxColor Max value highlight color or null
     */
    public SparklineParameters(int width, int height, Color lineColor, Color backgroundColor, Color highlightMinColor, Color highlightMaxColor) {
        this(width, height, lineColor, backgroundColor, highlightMinColor, highlightMaxColor, null, null, null, null, null);
    }

    /**
     * Create a sparkline parameters specifying colors for line, background, and max/min highlight colors (no highlight if null) and a x pixel position to highlight closest value with default
     * highlight text and text box colors and default
     * <code>HighlightTextMode</code>.
     *
     * @param width Width in pixels
     * @param height Height in pixels
     * @param lineColor Lines color
     * @param backgroundColor Background color
     * @param highlightMinColor Min value highlight color or null
     * @param highlightMaxColor Max value highlight color or null
     * @param highlightedValueXPosition X position in pixels to find closest value in the sparkline
     */
    public SparklineParameters(int width, int height, Color lineColor, Color backgroundColor, Color highlightMinColor, Color highlightMaxColor, Integer highlightedValueXPosition) {
        this(width, height, lineColor, backgroundColor, highlightMinColor, highlightMaxColor, highlightedValueXPosition, null, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_BOX_COLOR, DEFAULT_HIGHLIGHT_TEXT_MODE);
    }

    /**
     * Create a sparkline parameters specifying colors for line, background, and max/min highlight colors (no highlight if null) and a x pixel position to highlight closest value with specific
     * highlight text and text box colors and
     * <code>HighlightTextMode</code>.
     *
     * @param width Width in pixels
     * @param height Height in pixels
     * @param lineColor Lines color
     * @param backgroundColor Background color
     * @param highlightMinColor Min value highlight color or null
     * @param highlightMaxColor Max value highlight color or null
     * @param highlightedValueXPosition X position in pixels to find closest value in the sparkline
     * @param highligtValueColor Highlighted value color (Magenta if null)
     * @param highlightTextColor Highlighted value text color or null
     * @param highlightTextBoxColor Highlighted value text box color or null
     * @param highlightTextMode What to show on the highlight text (x and/or y values)
     */
    public SparklineParameters(int width, int height, Color lineColor, Color backgroundColor, Color highlightMinColor, Color highlightMaxColor, Integer highlightedValueXPosition, Color highligtValueColor, Color highlightTextColor, Color highlightTextBoxColor, HighlightTextMode highlightTextMode) {
        this.width = width;
        this.height = height;
        this.lineColor = lineColor;
        this.backgroundColor = backgroundColor;
        this.highlightMinColor = highlightMinColor;
        this.highlightMaxColor = highlightMaxColor;
        this.highlightedValueXPosition = highlightedValueXPosition;
        this.highligtValueColor = highligtValueColor;
        this.highlightTextColor = highlightTextColor;
        this.highlightTextBoxColor = highlightTextBoxColor;
        this.highlightTextMode = highlightTextMode;
    }

    /**
     * Returns current background color.
     *
     * @return Current background color
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Set background color. If null and transparent background is not enabled, white will be used by default.
     *
     * @param backgroundColor New background color
     */
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * Returns current height for the sparkline in pixels.
     *
     * @return Current height for the sparkline in pixels
     */
    public int getHeight() {
        return height;
    }

    /**
     * Set height for the sparkline in pixels.
     *
     * @param height Height in pixels
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Returns current color for the highlighted value text bounding box.
     *
     * @return Current highlightTextBoxColor
     */
    public Color getHighlightTextBoxColor() {
        return highlightTextBoxColor;
    }

    /**
     * Set color for the highlighted value text bounding box, or null to draw no box. Used only if highlightedValueXPosition is not null.
     *
     * @param highlightTextBoxColor New highlightTextBoxColor or null
     */
    public void setHighlightTextBoxColor(Color highlightTextBoxColor) {
        this.highlightTextBoxColor = highlightTextBoxColor;
    }

    /**
     * Returns current color for the highlighted value text.
     *
     * @return Current highlightTextColor
     */
    public Color getHighlightTextColor() {
        return highlightTextColor;
    }

    /**
     * Set color for the highlighted value text, or null to draw no text. Used only if highlightedValueXPosition is not null.
     *
     * @param highlightTextColor New highlightTextColor or null
     */
    public void setHighlightTextColor(Color highlightTextColor) {
        this.highlightTextColor = highlightTextColor;
    }

    /**
     * Returns current color for the highlighted value.
     *
     * @return Current highligtValueColor
     */
    public Color getHighligtValueColor() {
        return highligtValueColor;
    }

    /**
     * Set color for the value highlight, magenta is used if it is null. Used only if highlightedValueXPosition is not null.
     *
     * @param highligtValueColor New highligtValueColor
     */
    public void setHighligtValueColor(Color highligtValueColor) {
        this.highligtValueColor = highligtValueColor;
    }

    /**
     * Returns current highlightMaxColor.
     *
     * @return Current highlightMaxColor
     */
    public Color getHighlightMaxColor() {
        return highlightMaxColor;
    }

    /**
     * Set color for the maximum value highlight, or null to draw no highlight.
     *
     * @param highlightMaxColor New highlightMaxColor
     */
    public void setHighlightMaxColor(Color highlightMaxColor) {
        this.highlightMaxColor = highlightMaxColor;
    }

    /**
     * Returns current highlightMinColor.
     *
     * @return Current highlightMinColor
     */
    public Color getHighlightMinColor() {
        return highlightMinColor;
    }

    /**
     * Set color for the minimum value highlight, or null to draw no highlight.
     *
     * @param highlightMinColor New highlightMinColor
     */
    public void setHighlightMinColor(Color highlightMinColor) {
        this.highlightMinColor = highlightMinColor;
    }

    /**
     * Returns current X position for highlighting the closest value.
     *
     * @return Current highlightedValueXPosition in pixels
     */
    public Integer getHighlightedValueXPosition() {
        return highlightedValueXPosition;
    }

    /**
     * <p>Set a X position in pixels to find closest value in the sparkline and highlight it.</p> <p>If null or out of the sparkline width range, no value is highlighted.</p>
     *
     * @param highlightedValueXPosition New highlightedValueXPosition in pixels
     */
    public void setHighlightedValueXPosition(Integer highlightedValueXPosition) {
        this.highlightedValueXPosition = highlightedValueXPosition;
    }

    /**
     * Return current HighlightTextMode
     *
     * @see HighlightTextMode
     * @return Current HighlightTextMode
     */
    public HighlightTextMode getHighlightTextMode() {
        return highlightTextMode;
    }

    /**
     * Set HighlightTextMode
     *
     * @see HighlightTextMode
     * @param highlightTextMode New HighlightTextMode
     */
    public void setHighlightTextMode(HighlightTextMode highlightTextMode) {
        this.highlightTextMode = highlightTextMode;
    }

    /**
     * Returns current color for the sparkline line.
     *
     * @return Current line color
     */
    public Color getLineColor() {
        return lineColor;
    }

    /**
     * Set color for the sparkline line. If null, blue will be used by default.
     *
     * @param lineColor New lineColor
     */
    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    /**
     * Returns current width for the sparkline in pixels.
     *
     * @return Current width for the sparkline in pixels
     */
    public int getWidth() {
        return width;
    }

    /**
     * Set width for the sparkline in pixels.
     *
     * @param width width in pixels
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Checks if transparent background is enabled.
     *
     * @return transparent background enabled
     */
    public boolean isTransparentBackground() {
        return transparentBackground;
    }

    /**
     * Set transparent background. If true, background color is ignored and no background is drawn.
     *
     * @param transparentBackground transparent background enabled
     */
    public void setTransparentBackground(boolean transparentBackground) {
        this.transparentBackground = transparentBackground;
    }

    /**
     * Set color to fill the area under the line. If null, semi-transparent default line color will be used. Used only if draw area is enabled.
     *
     * @param areaColor New areaColor
     */
    public void setAreaColor(Color areaColor) {
        this.areaColor = areaColor;
    }

    /**
     * Returns current areaColor.
     *
     * @return Current areaColor
     */
    public Color getAreaColor() {
        return areaColor;
    }

    /**
     * Set draw area. If true, the area under the line is filled with areaColor.
     *
     * @param drawArea draw area enabled
     */
    public void setDrawArea(boolean drawArea) {
        this.drawArea = drawArea;
    }

    /**
     * Checks if the area under the line is enabled.
     *
     * @return draw area enabled
     */
    public boolean isDrawArea() {
        return drawArea;
    }
}
