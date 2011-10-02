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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Class for drawing sparkline graphics.
 * @see SparklineParameters
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class SparklineGraph {

    private static final int HIGHLIGHT_RADIUS = 4;

    /**
     * Draw a sparkline only providing y axis values (1 x tick per number assumed)
     * @param values Y axis values
     * @param parameters Rendering parameters
     * @return Image of the sparkline
     */
    public static BufferedImage draw(Number[] values, SparklineParameters parameters) {
        return draw(null, values, null, null, parameters);
    }

    /**
     * Draw a sparkline with x axis and y axis values.
     * X values <b>must</b> be ordered and not repeated.
     * @param xValues X axis values
     * @param yValues Y axis values
     * @param parameters Rendering parameters
     * @return Image of the sparkline
     */
    public static BufferedImage draw(Number[] xValues, Number[] yValues, SparklineParameters parameters) {
        return draw(xValues, yValues, null, null, parameters);
    }

    /**
     * Draw a sparkline with x axis and y axis values, and pre-calculated min and max of the y axis values.
     * Use this when you already have min/max values calculated and want to avoid extra calculations by <code>SparklineGraph</code>
     * X values <b>must</b> be ordered and not repeated.
     * @param xValues X axis values
     * @param yValues Y axis values
     * @param yMinValue Minimum value of the Y axis, should be correct
     * @param yMaxValue Maximum value of the Y axis, should be correct
     * @param parameters Rendering parameters
     * @return Image of the sparkline
     */
    public static BufferedImage draw(Number[] xValues, Number[] yValues, Number yMinValue, Number yMaxValue, SparklineParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("parameters can't be null");
        }

        if (yValues == null || yValues.length < 2) {
            throw new IllegalArgumentException("Y values can't be null and have a length of at least 2");
        }

        if (xValues != null && xValues.length != yValues.length) {
            throw new IllegalArgumentException("X values should have the same length as Y values");
        }

        final BufferedImage image = new BufferedImage(parameters.getWidth(), parameters.getHeight(), BufferedImage.TYPE_INT_ARGB);
        final Color backgroundColor = parameters.getBackgroundColor() != null ? parameters.getBackgroundColor() : SparklineParameters.DEFAULT_BACKGROUND_COLOR;
        final Color lineColor = parameters.getLineColor() != null ? parameters.getLineColor() : SparklineParameters.DEFAULT_LINE_COLOR;
        Color higlightMinColor = parameters.getHiglightMinColor();
        Color higlightMaxColor = parameters.getHiglightMaxColor();
        int width = parameters.getWidth();
        int height = parameters.getHeight();
        ArrayList<HighlightParameters> highlightsList = new ArrayList<HighlightParameters>();
        Color highlightValueColor = parameters.getHighligtValueColor() != null ? parameters.getHighligtValueColor() : SparklineParameters.DEFAULT_HIGHLIGHT_VALUE_COLOR;
        Integer higlightedValueXPosition = parameters.getHiglightedValueXPosition();
        String highlightedValueText = null;

        //Calculate y min if not provided:
        float yMin;
        if (yMinValue != null) {
            yMin = yMinValue.floatValue();
        } else {
            yMin = calculateMin(yValues);
        }

        //Calculate y max if not provided:
        float yMax;
        if (yMaxValue != null) {
            yMax = yMaxValue.floatValue();
        } else {
            yMax = calculateMax(yValues);
        }

        //Begin drawing:
        final Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //Background:
        if (backgroundColor != null) {
            g.setBackground(backgroundColor);
            g.clearRect(0, 0, width, height);
        }

        //Sparklines:
        g.setPaint(lineColor);
        //Special case, the only 2 values are the same:
        if (yValues.length == 2 && yValues[0].equals(yValues[1])) {
            g.draw(new Line2D.Double(0, height / 2f, width - 1, height / 2f));
        } else {
            if (higlightMaxColor != null || higlightMinColor != null || highlightValueColor != null) {
                height -= HIGHLIGHT_RADIUS;//Highlight circle radius pixels less for not drawing outside bounds
                width -= HIGHLIGHT_RADIUS;//Highlight circle radius pixels less for not drawing outside bounds
                g.translate(HIGHLIGHT_RADIUS / 2, HIGHLIGHT_RADIUS / 2);
            }

            //Calculate x tick width and x min value:
            float xMin;
            float xTickWidth;
            if (xValues == null) {
                xMin = 0;
                xTickWidth = (float) width / (yValues.length - 1);
            } else {
                xMin = xValues[0].floatValue();//X values have to be ordered
                xTickWidth = (float) width / (xValues[xValues.length - 1].floatValue() - xMin);
            }
            //Calculate yTickWidth:
            float yTickWidth = height / (yMax - yMin);

            float bottom = height;

            float x0, y0, x1, y1;
            for (int i = 0; i < yValues.length - 1; i++) {
                if (xValues == null) {
                    x0 = i * xTickWidth;
                    x1 = (i + 1) * xTickWidth;
                } else {
                    x0 = (xValues[i].floatValue() - xMin) * xTickWidth;
                    x1 = (xValues[i + 1].floatValue() - xMin) * xTickWidth;
                }
                y0 = bottom - (yValues[i].floatValue() - yMin) * yTickWidth;
                y1 = bottom - (yValues[i + 1].floatValue() - yMin) * yTickWidth;
                g.draw(new Line2D.Double(x0, y0, x1, y1));

                //Save min/max values highlihgting if enabled:
                if (yValues[i + 1].floatValue() == yMin && higlightMinColor != null) {
                    highlightsList.add(new HighlightParameters(x1, y1, higlightMinColor));
                    higlightMinColor = null;//Only highlight first min
                }
                if (yValues[i + 1].floatValue() == yMax && higlightMaxColor != null) {
                    highlightsList.add(new HighlightParameters(x1, y1, higlightMaxColor));
                    higlightMaxColor = null;//Only highlight first max
                }
                //Save first point special case min/max values highlihgting if enabled:
                if (i == 0) {
                    if (yValues[0].floatValue() == yMin && higlightMinColor != null) {
                        highlightsList.add(new HighlightParameters(x0, y0, higlightMinColor));
                        higlightMinColor = null;//Only highlight first min
                    }
                    if (yValues[0].floatValue() == yMax && higlightMaxColor != null) {
                        highlightsList.add(new HighlightParameters(x0, y0, higlightMaxColor));
                        higlightMaxColor = null;//Only highlight first max
                    }
                }

                //Save x position value highlighting if enabled:
                if (higlightedValueXPosition != null && x1 >= higlightedValueXPosition) {
                    //Highlight closest point:
                    if ((higlightedValueXPosition - x0) < (x1 - x0) / 2f) {//Higlight left point
                        highlightsList.add(new HighlightParameters(x0, y0, highlightValueColor));
                        if (parameters.getHighlightTextColor() != null) {
                            highlightedValueText = "(" + (xValues != null ? xValues[i] : i) + "," + yValues[i] + ")";
                        }
                    } else {//Higlight right point
                        highlightsList.add(new HighlightParameters(x1, y1, highlightValueColor));
                        if (parameters.getHighlightTextColor() != null) {
                            highlightedValueText = "(" + (xValues != null ? xValues[i + 1] : i + 1) + "," + yValues[i + 1] + ")";
                        }
                    }
                    higlightedValueXPosition = null;
                }
            }

            //Draw list of highlights at the end to always draw them on top of lines:
            for (HighlightParameters p : highlightsList) {
                drawHighlight(g, p.x, p.y, p.color);
            }

            //Finally draw value text (x,y) if enabled:
            if (highlightedValueText != null) {
                g.setFont(g.getFont().deriveFont(Font.BOLD));
                int textWidth = g.getFontMetrics().stringWidth(highlightedValueText);
                int textHeight = g.getFontMetrics().getHeight();
                g.translate((width - textWidth) / 2f, (height + textHeight / 2f) / 2f);
                //Draw bounding box if enabled:
                if (parameters.getHighlightTextBoxColor() != null) {
                    g.setPaint(parameters.getHighlightTextBoxColor());
                    g.fill(g.getFontMetrics().getStringBounds(highlightedValueText, g));
                }
                g.setPaint(parameters.getHighlightTextColor());
                g.drawString(highlightedValueText, 0, 0);
            }
        }

        return image;
    }

    private static void drawHighlight(Graphics2D g, float x, float y, Color highlightColor) {
        Paint oldPaint = g.getPaint();
        g.setPaint(highlightColor);
        g.fill(new Ellipse2D.Double(x - HIGHLIGHT_RADIUS / 2f, y - HIGHLIGHT_RADIUS / 2f, HIGHLIGHT_RADIUS, HIGHLIGHT_RADIUS));
        g.setPaint(oldPaint);
    }

    private static float calculateMin(Number[] yValues) {
        float min = yValues[0].floatValue();
        for (Number d : yValues) {
            min = Math.min(min, d.floatValue());
        }

        return min;
    }

    private static float calculateMax(Number[] yValues) {
        float max = yValues[0].floatValue();
        for (Number d : yValues) {
            max = Math.max(max, d.floatValue());
        }

        return max;
    }

    private static class HighlightParameters {

        float x, y;
        Color color;

        public HighlightParameters(float x, float y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }
}
