/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke, Eduardo Ramos
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.desktop.datalab.utils;

import java.awt.Component;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;
import org.gephi.data.attributes.type.Interval;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.DynamicUtilities;
import org.gephi.dynamic.api.DynamicModel.TimeFormat;

/**
 * TableCellRenderer for drawing time intervals graphics from cells that have a TimeInterval as their value.
 */
public class TimeIntervalsRenderer extends DefaultTableCellRenderer {

    private static final Color SELECTED_BACKGROUND = new Color(225, 255, 255);
    private static final Color UNSELECTED_BACKGROUND = Color.white;
    private static final Color FILL_COLOR = new Color(153, 255, 255);
    private static final Color BORDER_COLOR = new Color(2, 104, 255);
    private boolean drawGraphics;
    private TimeIntervalGraphics timeIntervalGraphics;
    private TimeFormat timeFormat = TimeFormat.DOUBLE;

    public TimeIntervalsRenderer(double min, double max, boolean drawGraphics) {
        timeIntervalGraphics = new TimeIntervalGraphics(min, max);
        this.drawGraphics = drawGraphics;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value == null) {
            //Render empty string when null
            return super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);
        }
        TimeInterval timeInterval = (TimeInterval) value;
        String stringRepresentation = null;
        switch (timeFormat) {
            case DOUBLE:
                stringRepresentation = timeInterval.toString();
                break;
            case DATE:
                stringRepresentation = timeIntervalToDatesString(timeInterval);
                break;
        }
        if (drawGraphics) {
            JLabel label = new JLabel();
            Color background;
            if (isSelected) {
                background = SELECTED_BACKGROUND;
            } else {
                background = UNSELECTED_BACKGROUND;
            }

            final BufferedImage i = timeIntervalGraphics.createTimeIntervalImage(timeInterval.getLow(), timeInterval.getHigh(), table.getColumnModel().getColumn(column).getWidth() - 1, table.getRowHeight(row) - 1, FILL_COLOR, BORDER_COLOR, background);
            label.setIcon(new ImageIcon(i));
            label.setToolTipText(stringRepresentation);//String representation as tooltip
            return label;
        } else {
            return super.getTableCellRendererComponent(table, stringRepresentation, isSelected, hasFocus, row, column);
        }
    }

    public String timeIntervalToDatesString(TimeInterval timeInterval) {
        List<Interval<Double[]>> list = timeInterval.getIntervals(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        if (!list.isEmpty()) {
            StringBuilder sb = new StringBuilder("<");
            sb.append(list.get(0).isLowExcluded() ? "(" : "[").append(doubleToDateString(list.get(0).getLow())).append(", ").
                    append(doubleToDateString(list.get(0).getHigh())).append(list.get(0).isHighExcluded() ? ")" : "]");
            for (int i = 1; i < list.size(); ++i) {
                sb.append(", ").append(list.get(i).isLowExcluded() ? "(" : "[").append(doubleToDateString(list.get(i).getLow())).append(", ").
                        append(doubleToDateString(list.get(i).getHigh())).append(list.get(i).isHighExcluded() ? ")" : "]");
            }
            sb.append(">");
            return sb.toString();
        }
        return "<empty>";
    }

    private String doubleToDateString(double d) {
        return DynamicUtilities.getXMLDateStringFromDouble(d);
    }

    public boolean isDrawGraphics() {
        return drawGraphics;
    }

    public void setDrawGraphics(boolean drawGraphics) {
        this.drawGraphics = drawGraphics;
    }

    public TimeFormat getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(TimeFormat timeFormat) {
        this.timeFormat = timeFormat;
    }

    public double getMax() {
        return timeIntervalGraphics.getMax();
    }

    public void setMax(double max) {
        timeIntervalGraphics.setMax(max);
    }

    public double getMin() {
        return timeIntervalGraphics.getMin();
    }

    public void setMin(double min) {
        timeIntervalGraphics.setMin(min);
    }

    public void setMinMax(double min, double max) {
        timeIntervalGraphics = new TimeIntervalGraphics(min, max);
    }
}
