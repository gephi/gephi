package org.gephi.statistics.plugin;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.junit.Assert;
import org.junit.Test;

public class ChartUtilsTest {

    @Test
    public void testScaleChartWithZeroValues() {
        XYSeries series = new XYSeries("zeros");
        series.add(0.0, 0.0);
        series.add(1.0, 0.0);

        JFreeChart chart = chart(series);
        ChartUtils.scaleChart(chart, series, false);

        Assert.assertTrue(chart.getXYPlot().getRangeAxis().getRange().getLength() > 0);
        Assert.assertTrue(chart.getXYPlot().getRangeAxis().getRange().contains(0.0));
    }

    @Test
    public void testScaleChartWithEmptySeries() {
        XYSeries series = new XYSeries("empty");

        JFreeChart chart = chart(series);
        ChartUtils.scaleChart(chart, series, false);

        Assert.assertTrue(chart.getXYPlot().getRangeAxis().getRange().getLength() > 0);
    }

    private static JFreeChart chart(XYSeries series) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        return ChartFactory.createXYLineChart("title", "x", "y", dataset, PlotOrientation.VERTICAL, false, false,
            false);
    }
}
