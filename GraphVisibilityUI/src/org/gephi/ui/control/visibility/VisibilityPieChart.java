/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.ui.control.visibility;

import java.awt.Color;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.DefaultPieDataset;

/**
 *
 * @author Mathieu Bastian
 */
public class VisibilityPieChart {

    private DefaultPieDataset data;
    private ChartPanel chartPanel;

    public VisibilityPieChart() {
        data = new DefaultPieDataset();
        data.setValue("Visible", 60);
        data.setValue("Not visible", 40);

        final JFreeChart chart = ChartFactory.createPieChart("Employee Survey", data, false, false, false);
        chart.setTitle(new TextTitle());
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setShadowPaint(null);
        plot.setSimpleLabels(true);
        plot.setLabelBackgroundPaint(null);
        plot.setLabelOutlineStroke(null);
        plot.setLabelShadowPaint(null);
        plot.setOutlineVisible(false);
        plot.setLabelFont(new java.awt.Font("Tahoma", 0, 10));
        plot.setLabelPaint(Color.WHITE);
        plot.setLabelGap(0.5);
        plot.setCircular(true);
        plot.setBackgroundAlpha(0f);
        plot.setExplodePercent("Visible", 0.1);
        chartPanel = new ChartPanel(chart, true);

    }

    public ChartPanel getChartPanel() {
        return chartPanel;
    }
}
