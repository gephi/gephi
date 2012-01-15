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
 * Sparkline rendering options:
 * <ul>
 * <li>Width and height of the graphic in pixels</li>
 * <li>Line color. Blue by default if null</li>
 * <li>Color for the background of the graphic. White by default if null</li>
 * <li>Highlight colors for min and max values, null (no highlight) by default</li>
 * <li>Highlight colors for a value closest to the given x pixel position(<code>higlightedValueXPosition</code>), magenta by default if null and <code>higlightedValueXPosition</code> is not null</li>
 * <li>Highlighted value text color, used if <code>higlightedValueXPosition</code> is provided. By default (x,y) values are shown, use <code>highlightTextMode</code> to control this.</li>
 * <li>Highlighted value text box color, used if <code>higlightedValueXPosition</code> and <code>highlightTextColor</code> are provided</li>
 * </ul>
 * Several constructors are provided for various use cases.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class SparklineParameters {
    
    enum HighlightTextMode {
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
    private Color higlightMinColor, higlightMaxColor;
    private Integer higlightedValueXPosition;
    private Color highligtValueColor;
    private Color highlightTextColor;
    private Color highlightTextBoxColor;
    private HighlightTextMode highlightTextMode;

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
        this(width, height, lineColor, backgroundColor, higlightMinColor, higlightMaxColor, null, null, null, null, null);
    }

    /**
     * Create a sparkline parameters specifying colors for line, background, and max/min highlight colors (no highlight if null)
     * and a x pixel position to highlight closest value with default highlight text and text box colors and default <code>HighlightTextMode</code>.
     * @param width Width in pixels
     * @param height Height in pixels
     * @param lineColor Lines color
     * @param backgroundColor Background color
     * @param higlightMinColor Min value highlight color or null
     * @param higlightMaxColor Max value highlight color or null
     * @param higlightedValueXPosition X position in pixels to find closest value in the sparkline
     */
    public SparklineParameters(int width, int height, Color lineColor, Color backgroundColor, Color higlightMinColor, Color higlightMaxColor, Integer higlightedValueXPosition) {
        this(width, height, lineColor, backgroundColor, higlightMinColor, higlightMaxColor, higlightedValueXPosition, null, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_BOX_COLOR, DEFAULT_HIGHLIGHT_TEXT_MODE);
    }

    /**
     * Create a sparkline parameters specifying colors for line, background, and max/min highlight colors (no highlight if null)
     * and a x pixel position to highlight closest value with specific highlight text and text box colors and <code>HighlightTextMode</code>.
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
     * @param highlightTextMode What to show on the highlight text (x and/or y values)
     */
    public SparklineParameters(int width, int height, Color lineColor, Color backgroundColor, Color higlightMinColor, Color higlightMaxColor, Integer higlightedValueXPosition, Color highligtValueColor, Color highlightTextColor, Color highlightTextBoxColor, HighlightTextMode highlightTextMode) {
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
        this.highlightTextMode = highlightTextMode;
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

    public HighlightTextMode getHighlightTextMode() {
        return highlightTextMode;
    }

    public void setHighlightTextMode(HighlightTextMode highlightTextMode) {
        this.highlightTextMode = highlightTextMode;
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

    public boolean isTransparentBackground() {
        return transparentBackground;
    }

    public void setTransparentBackground(boolean transparentBackground) {
        this.transparentBackground = transparentBackground;
    }

    public void setAreaColor(Color areaColor) {
        this.areaColor = areaColor;
    }

    public Color getAreaColor() {
        return areaColor;
    }

    public void setDrawArea(boolean drawArea) {
        this.drawArea = drawArea;
    }

    public boolean isDrawArea() {
        return drawArea;
    }
}
