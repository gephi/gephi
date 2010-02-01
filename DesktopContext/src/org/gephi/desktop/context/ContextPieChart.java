/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.desktop.context;

import java.awt.Color;
import java.awt.FlowLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.ui.RectangleInsets;

/**
 *
 * @author Mathieu Bastian
 */
public class ContextPieChart {

    private DefaultPieDataset data;
    private ChartPanel chartPanel;

    public ContextPieChart() {
        data = new DefaultPieDataset();
        final JFreeChart chart = ChartFactory.createPieChart("Employee Survey", data, false, false, false);
        chart.setTitle(new TextTitle());
        chart.setBackgroundPaint(null);
        chart.setPadding(new RectangleInsets(0, 0, 0, 0));
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
        plot.setInteriorGap(0);
        plot.setBackgroundPaint(null);
        plot.setBackgroundAlpha(1f);
        chartPanel = new ChartPanel(chart, 100, 100, 10, 10, 300, 300, true, false, false, false, false, false);
        ((FlowLayout) chartPanel.getLayout()).setHgap(0);
        ((FlowLayout) chartPanel.getLayout()).setVgap(0);
        chartPanel.setOpaque(false);
        chartPanel.setPopupMenu(null);
    }

    public void refreshChart(double visiblePercentage) {
        data.setValue("Visible", visiblePercentage);
        data.setValue("Not visible", 1 - visiblePercentage);
    }

    public void setChartVisible(boolean visible) {
        chartPanel.setVisible(visible);
    }

    public ChartPanel getChartPanel() {
        return chartPanel;
    }
}
