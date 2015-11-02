/*
 Copyright 2008-2010 Gephi
 Authors : Eduardo Ramos
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
package org.gephi.desktop.datalab.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.gephi.graph.api.TimeFormat;
import org.gephi.graph.api.types.IntervalSet;
import org.gephi.utils.TimeIntervalGraphics;

/**
 * TableCellRenderer for drawing time intervals graphics from cells that have a IntervalSet as their value.
 *
 * @author Eduardo Ramos
 */
public class TimeIntervalsRenderer extends DefaultTableCellRenderer {

    private static final Color SELECTED_BACKGROUND = new Color(225, 255, 255);
    private static final Color UNSELECTED_BACKGROUND = Color.white;
    private static final Color FILL_COLOR = new Color(153, 255, 255);
    private static final Color BORDER_COLOR = new Color(2, 104, 255);
    private boolean drawGraphics = false;
    private final TimeIntervalGraphics timeIntervalGraphics;
    private TimeFormat timeFormat = TimeFormat.DOUBLE;

    public TimeIntervalsRenderer() {
        this(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    public TimeIntervalsRenderer(double min, double max) {
        timeIntervalGraphics = new TimeIntervalGraphics(min, max);
    }
    
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
        IntervalSet intervalSet = (IntervalSet) value;
        String stringRepresentation = intervalSet.toString(timeFormat);
        if (drawGraphics) {
            JLabel label = new JLabel();
            Color background;
            if (isSelected) {
                background = SELECTED_BACKGROUND;
            } else {
                background = UNSELECTED_BACKGROUND;
            }
            
            double[] intervals = intervalSet.getIntervals();

            double starts[] = new double[intervals.length / 2];
            double ends[] = new double[intervals.length / 2];
            for (int i = 0; i < intervals.length; i+=2) {
                starts[i] = intervals[i];
                ends[i] = intervals[i + 1];
            }

            final BufferedImage i = timeIntervalGraphics.createTimeIntervalImage(starts, ends, table.getColumnModel().getColumn(column).getWidth() - 1, table.getRowHeight(row) - 1, FILL_COLOR, BORDER_COLOR, background);
            label.setIcon(new ImageIcon(i));
            label.setToolTipText(stringRepresentation);//String representation as tooltip
            return label;
        } else {
            return super.getTableCellRendererComponent(table, stringRepresentation, isSelected, hasFocus, row, column);
        }
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
        timeIntervalGraphics.setMin(min);
        timeIntervalGraphics.setMax(max);
    }
}
