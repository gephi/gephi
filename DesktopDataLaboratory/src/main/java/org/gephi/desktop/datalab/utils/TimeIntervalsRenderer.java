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
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.gephi.data.attributes.type.Interval;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.api.DynamicModel.TimeFormat;
import org.gephi.utils.TimeIntervalGraphics;

/**
 * TableCellRenderer for drawing time intervals graphics from cells that have a TimeInterval as their value.
 *
 * @author Eduardo Ramos <eduramiba@gmail.com>
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
        String stringRepresentation = timeInterval.toString(timeFormat == TimeFormat.DOUBLE);
        if (drawGraphics) {
            JLabel label = new JLabel();
            Color background;
            if (isSelected) {
                background = SELECTED_BACKGROUND;
            } else {
                background = UNSELECTED_BACKGROUND;
            }

            List<Interval<Double[]>> intervals = timeInterval.getIntervals();
            double starts[] = new double[intervals.size()];
            double ends[] = new double[intervals.size()];
            for (int i = 0; i < intervals.size(); i++) {
                starts[i] = intervals.get(i).getLow();
                ends[i] = intervals.get(i).getHigh();
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
        timeIntervalGraphics = new TimeIntervalGraphics(min, max);
    }
}
