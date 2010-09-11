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
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;
import org.gephi.data.attributes.type.TimeInterval;

/**
 * TableCellRenderer for drawing time intervals graphics from cells that have a TimeInterval as their value.
 */
public class TimeIntervalsRenderer extends DefaultTableCellRenderer {

    private static final Color SELECTED_BACKGROUND = new Color(225, 255, 255);
    private static final Color UNSELECTED_BACKGROUND = Color.white;
    private static final Color FILL_COLOR = new Color(153, 255, 255);
    private static final Color BORDER_COLOR = new Color(2, 104, 255);
    private TimeIntervalGraphics timeIntervalGraphics;

    public TimeIntervalsRenderer(double min, double max) {
        timeIntervalGraphics = new TimeIntervalGraphics(min, max);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value == null) {
            //Render empty string when null
            return super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
        }
        TimeInterval timeInterval = (TimeInterval) value;

        JLabel label = new JLabel();

        Color background;
        if (isSelected) {
            background = SELECTED_BACKGROUND;
        } else {
            background = UNSELECTED_BACKGROUND;
        }

        final BufferedImage i = timeIntervalGraphics.createTimeIntervalImage(timeInterval.getLow(), timeInterval.getHigh(), table.getColumnModel().getColumn(column).getWidth(), table.getRowHeight(row) - 1, FILL_COLOR, BORDER_COLOR, background);
        label.setIcon(new ImageIcon(i));
        label.setToolTipText(value.toString());//String representation as tooltip

        return label;
    }

    public double getMax() {
        return timeIntervalGraphics.getMax();
    }

    public void setMax(double max) {
        timeIntervalGraphics = new TimeIntervalGraphics(timeIntervalGraphics.getMin(), max);
    }

    public double getMin() {
        return timeIntervalGraphics.getMin();
    }

    public void setMin(double min) {
        timeIntervalGraphics = new TimeIntervalGraphics(min, timeIntervalGraphics.getMax());
    }

    public void setMinMax(double min, double max) {
        timeIntervalGraphics = new TimeIntervalGraphics(min, max);
    }
}
