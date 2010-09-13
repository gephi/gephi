/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Mathieu Jacomy, Julian Bilcke, Eduardo Ramos
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
package org.gephi.desktop.datalab.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * <p>Class to draw time intervals as graphics, being able to indicate the colors to use (or default colors).
 * The result graphics are like:</p>
 *
 * <p>|{background color}|time-interval{fill color}|{background color}|</p>
 *
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class TimeIntervalGraphics {

    private static final Color DEFAULT_FILL = new Color(153, 255, 255);
    private static final Color DEFAULT_BORDER = new Color(2, 104, 255);
    private double min;
    private double max;
    private BigDecimal range;

    /**
     * Create a new TimeIntervalGraphics with the given minimum and maximum times to render intervals later.
     * @param min Minimum time of all intervals
     * @param max Maximum time of all intervals
     */
    public TimeIntervalGraphics(double min, double max) {
        min=normalize(min);
        max=normalize(max);        
        if (min > max) {
            throw new IllegalArgumentException("min should be less or equal than max");
        }
        this.min = min;
        this.max = max;
        calculateRange();
    }

    private void calculateRange() {
        range = new BigDecimal(max);
        range = range.subtract(new BigDecimal(min));
    }

    /**
     * Creates a time interval graphic representation with default colors
     * @param start Start of the interval (must be greater or equal than minimum time)
     * @param end End of the interval (must be lesser or equal than maximum time)
     * @param width Image width
     * @param height Image height
     * @return Generated image for the interval
     */
    public BufferedImage createTimeIntervalImage(double start, double end, int width, int height) {
        return createTimeIntervalImage(start, end, width, height, null, null, null);
    }

    /**
     * Creates a time interval graphic representation with the indicated fill and border colors (or null to use default colors)
     * @param start Start of the interval (must be greater or equal than minimum time)
     * @param end End of the interval (must be lesser or equal than maximum time)
     * @param width Image width
     * @param height Image height
     * @param fill Fill color for the interval
     * @param border Border color for the interval
     * @return Generated image for the interval
     */
    public BufferedImage createTimeIntervalImage(double start, double end, int width, int height, Color fill, Color border) {
        return createTimeIntervalImage(start, end, width, height, fill, border, null);
    }

    /**
     * Creates a time interval graphic representation with the indicated fill, border and background colors (or null to use default colors)
     * @param start Start of the interval (must be greater or equal than minimum time)
     * @param end End of the interval (must be lesser or equal than maximum time)
     * @param width Image width
     * @param height Image height
     * @param fill Fill color for the interval
     * @param border Border color for the interval
     * @param background Background color
     * @return Generated image for the interval
     */
    public BufferedImage createTimeIntervalImage(double start, double end, int width, int height, Color fill, Color border, Color background) {
        start=normalize(start);
        end=normalize(end);

        if (start > end) {
            throw new IllegalArgumentException("start should be less or equal than end");
        }
        if (start < min) {
            throw new IllegalArgumentException("start should be greater or equal than the minimum time set");
        }
        if (end > max) {
            throw new IllegalArgumentException("end should be lesser or equal than the maximum time set");
        }

        if (fill == null) {
            fill = DEFAULT_FILL;
        }
        if (border == null) {
            border = DEFAULT_BORDER;
        }

        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        final Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final int startPixel = (int) (width * new BigDecimal(start).subtract(new BigDecimal(min)).divide(range, 5, RoundingMode.HALF_EVEN).doubleValue());
        final int endPixel = (int) (width * new BigDecimal(end).subtract(new BigDecimal(min)).divide(range, 5, RoundingMode.HALF_EVEN).doubleValue());

        //Draw brackground if any:
        if (background != null) {
            g.setColor(background);
            g.fillRect(0, 0, startPixel, height);
            g.fillRect(endPixel + 1, 0, width - endPixel - 1, height);
        }

        //Draw time interval fill:
        g.setColor(fill);
        g.fillRect(startPixel + 1, 0, endPixel - startPixel - 2, height);

        //Draw borders:
        g.setColor(border);
        g.drawLine(startPixel, 0, startPixel, height);
        g.drawLine(endPixel - 1, 0, endPixel - 1, height);

        return image;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        max=normalize(max);
        if (max < min) {
            throw new IllegalArgumentException("min should be less or equal than max");
        }
        this.max = max;
        calculateRange();
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        min=normalize(min);
        if (max < min) {
            throw new IllegalArgumentException("min should be less or equal than max");
        }
        this.min = min;
        calculateRange();
    }

    private double normalize(double d){
        if(d==Double.NEGATIVE_INFINITY){
            return -Double.MAX_VALUE;
        }
        if(d==Double.POSITIVE_INFINITY){
            return Double.MAX_VALUE;
        }
        return d;
    }
}
