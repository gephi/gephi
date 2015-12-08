/*
 Copyright 2008-2011 Gephi
 Authors : Mathieu Bastian
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
package org.gephi.desktop.timeline;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import org.gephi.graph.api.TimeFormat;
import org.gephi.timeline.api.TimelineModel;
import org.joda.time.Interval;

/**
 *
 * @author Mathieu Bastian
 */
public class TickGraph {

    private double min;
    private double max;
    private TickParameters parameters;
    private BufferedImage image;

    public BufferedImage getImage(TimelineModel model, int width, int height) {
        double newMin = model.getCustomMin();
        double newMax = model.getCustomMax();
        TickParameters.TickType timeFormat = model.getTimeFormat().equals(TimeFormat.DOUBLE) ? TickParameters.TickType.DOUBLE : TickParameters.TickType.DATE;
        if (parameters == null || newMax != max || newMin != min || parameters.getWidth() != width || parameters.getHeight() != height || !parameters.getType().equals(timeFormat)) {
            min = newMin;
            max = newMax;
            parameters = new TickParameters(timeFormat);
            parameters.setWidth(width);
            parameters.setHeight(height);
            image = draw();
        }
        return image;
    }

    private BufferedImage draw() {

        final BufferedImage image = new BufferedImage(parameters.getWidth(), parameters.getHeight(), BufferedImage.TYPE_INT_ARGB);

        final Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (parameters.getType().equals(TickParameters.TickType.DATE)) {
            drawDate(g);
        } else {
            drawReal(g);
        }

        return image;
    }

    private void drawDate(Graphics2D g) {
        int width = parameters.getWidth();
        int height = parameters.getHeight();

        //Font
        int fontSize = Math.min(parameters.getFontSize(), (int) (height / 4.));
        fontSize = fontSize > parameters.getFontSize() / 4 && fontSize <= parameters.getFontSize() / 2 ? parameters.getFontSize() / 2 : fontSize;
        FontMetrics smallMetrics = null;
        Font smallFont = parameters.getFont();
        Font bigFont;
        FontMetrics bigMetrics;
        if (smallFont != null && fontSize > parameters.getFontSize() / 4) {
            smallFont = smallFont.deriveFont(Font.PLAIN, fontSize);
            smallMetrics = g.getFontMetrics(smallFont);
            bigFont = smallFont.deriveFont(Font.PLAIN, (int) (fontSize * 2.5));
            bigMetrics = g.getFontMetrics(bigFont);
        } else {
            smallFont = null;
            bigFont = null;
        }

        DateTick dateTick = DateTick.create(min, max, width);


        int TOP_TICK = 0;
        int LOWER_TICK = 1;

        //Lower tick
        if (dateTick.getTypeCount() > 1) {
            g.setFont(smallFont);
            g.setColor(parameters.getDateColor(LOWER_TICK));
            Interval[] intervals = dateTick.getIntervals(LOWER_TICK);
            int labelWidth = smallMetrics != null ? smallMetrics.stringWidth("0000") : 0;
            for (Interval interval : intervals) {
                long ms = interval.getStartMillis();
                int x = dateTick.getTickPixelPosition(ms, width);
                if (x >= 0) {
                    g.setColor(parameters.getDateColor(LOWER_TICK));

                    //Height
                    int h = (int) (Math.min(40, (int) (height / 15.0)));

                    //Draw line
                    g.drawLine(x, 0, x, h);

                    //Label                       
                    if (smallFont != null && width / intervals.length > labelWidth) {
                        String label = dateTick.getTickValue(LOWER_TICK, interval.getStart());
                        int xLabel = x + 4;
                        g.setColor(parameters.getDateColor(1));
                        int y = (int) (fontSize * 1.2);

                        g.drawString(label, xLabel, y);
                    }
                }
            }
        }

        //Top tick
        if (dateTick.getTypeCount() > 0) {
            g.setFont(bigFont);
            g.setColor(parameters.getDateColor(TOP_TICK));
            Interval[] intervals = dateTick.getIntervals(TOP_TICK);
            for (Interval interval : intervals) {
                long ms = interval.getStartMillis();
                int x = dateTick.getTickPixelPosition(ms, width);
                if (x >= 0) {
                    g.setColor(parameters.getDateColor(TOP_TICK));

                    //Height
                    int h = height;

                    //Draw Line
                    g.drawLine(x, 0, x, h);

                    //Label
                    if (bigFont != null) {
                        String label = dateTick.getTickValue(TOP_TICK, interval.getStart());
                        int xLabel = x + 4;
                        g.setColor(parameters.getDateColor(TOP_TICK));
                        int y = (int) (fontSize * 4);

                        g.drawString(label, xLabel, y);
                    }
                } else if (x > ((dateTick.getTickPixelPosition(interval.getEndMillis(), width) - x) / -2)) {

                    if (bigFont != null) {
                        String label = dateTick.getTickValue(TOP_TICK, interval.getStart());
                        g.setColor(parameters.getDateColor(TOP_TICK));
                        int y = (int) (fontSize * 4);

                        g.drawString(label, 4, y);
                    }
                }
            }
        }
    }

    private void drawStartEnd(Graphics2D g) {
        int width = parameters.getWidth();
        int height = parameters.getHeight();

        //Font
        Font font = parameters.getFont();
        FontMetrics fontMetrics = null;
        int fontSize = Math.min(parameters.getFontSize(), (int) (height / 4.));
        fontSize = fontSize > parameters.getFontSize() / 4 && fontSize <= parameters.getFontSize() / 2 ? parameters.getFontSize() / 2 : fontSize;
        if (font != null && fontSize > parameters.getFontSize() / 4) {
            font = font.deriveFont(Font.PLAIN, fontSize);
            fontMetrics = g.getFontMetrics(font);
            g.setFont(font);
        } else {
            font = null;
        }

        if (font != null) {
            g.setColor(parameters.getRealColor(2));
            StartEndTick startEnd = StartEndTick.create(min, max);
            String labelStart = startEnd.getStartValue();
            String labelEnd = startEnd.getEndValue();
            int xEnd = width - (fontMetrics.stringWidth(labelEnd)) - (int) (fontSize * 0.3);
            g.drawString(labelStart, (int) (fontSize * 0.3), (int) (fontSize * 1.2));
            g.drawString(labelEnd, xEnd, (int) (fontSize * 1.2));
        }
    }

    private void drawReal(Graphics2D g) {
        int width = parameters.getWidth();
        int height = parameters.getHeight();

        //Font
        Font font = parameters.getFont();
        FontMetrics fontMetrics = null;
        double factor = parameters.getFontFactor();
        int fontSize = Math.min(parameters.getFontSize(), (int) (height / factor));
        fontSize = fontSize > parameters.getFontSize() / (factor * 2) && fontSize <= parameters.getFontSize() / (factor / 4) ? (int) (parameters.getFontSize() / (factor / 4)) : fontSize;
        if (font != null && fontSize > parameters.getFontSize() / (factor / 2)) {
            font = font.deriveFont(Font.PLAIN, fontSize);
            fontMetrics = g.getFontMetrics(font);
            g.setFont(font);
        } else {
            font = null;
        }

        //50
//        int fifty = (int) ((50 - min) * (width / (max - min)));
//        g.setColor(Color.BLUE);
//        g.drawLine(fifty, 0, fifty, height);

        RealTick graduation = RealTick.create(min, max, width);
        int numberTicks = graduation.getNumberTicks();
        for (int i = 0; i <= numberTicks; i++) {
            int x = graduation.getTickPixelPosition(i, width);
            int rank = graduation.getTickRank(i);
            int h = Math.min(40, (int) (height / 15.0));
            h = rank == 2 ? (int) (h + h) : rank == 1 ? (int) (h + h / 2.) : h;
            if (x > 0) {
                g.setColor(parameters.getRealColor(rank));
                g.drawLine(x, 0, x, h);
                if (font != null && rank >= 1) {
                    String label = graduation.getTickValue(i);
                    int xLabel = x - (fontMetrics.stringWidth(label) / 2);
                    g.drawString(label, xLabel, (int) (h + fontSize * 1.2));
                }
            }
        }
    }
}
