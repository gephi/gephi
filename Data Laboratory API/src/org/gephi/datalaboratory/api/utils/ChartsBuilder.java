/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalaboratory.api.utils;

import java.awt.Color;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Utils class to build and change charts.
 * Scatter plots implemented to be able to draw or not lines and linear regression.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class ChartsBuilder {

    /**
     * Build new Scatter plot. Appearance can be changed later with the other methods of ChartsBuilder.
     * @param data Data for the plot
     * @param title Title for the chart
     * @param xLabel Text for x label
     * @param yLabel Text for y label
     * @param useLines Indicates if lines have to be drawn instead of shapes
     * @param useLinearRegression Indicates if the scatter plot has to have linear regreesion line drawn
     * @return Scatter plot for the data and appearance options
     */
    public static JFreeChart buildScatterPlot(final XYSeriesCollection data, final String title, final String xLabel, final String yLabel, final boolean useLines, final boolean useLinearRegression) {
        JFreeChart scatterPlot = ChartFactory.createXYLineChart(
                title,
                xLabel,
                yLabel,
                data,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);

        XYPlot plot = (XYPlot) scatterPlot.getPlot();
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        plot.setDomainGridlinePaint(java.awt.Color.GRAY);
        plot.setRangeGridlinePaint(java.awt.Color.GRAY);

        setScatterPlotLinesEnabled(scatterPlot, useLines);
        setScatterPlotLinearRegressionEnabled(scatterPlot, useLinearRegression);
        return scatterPlot;
    }

    /**
     * Modify a scatter plot to show lines instead or shapes or not.
     * @param scatterPlot Scatter plot to modify
     * @param enabled Indicates if lines have to be shown
     */
    public static void setScatterPlotLinesEnabled(final JFreeChart scatterPlot, final boolean enabled) {
        XYPlot plot = (XYPlot) scatterPlot.getPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        if (enabled) {
            renderer.setSeriesLinesVisible(0, true);
            renderer.setSeriesShapesVisible(0, false);
        } else {
            renderer.setSeriesLinesVisible(0, false);
            renderer.setSeriesShapesVisible(0, true);
            renderer.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(0, 0, 1, 1));
        }
        renderer.setSeriesPaint(0, Color.RED);

        plot.setRenderer(0, renderer);
    }

    /**
     * Modify a scatter plot to show linear regression or not.
     * @param scatterPlot Scatter plot to modify
     * @param enabled Indicates if linear regression has to be shown
     */
    public static void setScatterPlotLinearRegressionEnabled(final JFreeChart scatterPlot, final boolean enabled) {
        XYPlot plot = (XYPlot) scatterPlot.getPlot();
        if (enabled) {
            StandardXYItemRenderer regressionRenderer = new StandardXYItemRenderer();
            regressionRenderer.setBaseSeriesVisibleInLegend(false);
            plot.setDataset(1, regress((XYSeriesCollection) plot.getDataset(0)));
            plot.setRenderer(1, regressionRenderer);
        } else {
            plot.setDataset(1, null);//Remove linear regression
        }
    }

    /**
     * Calculates linear regression points from a XYSeriesCollection data set
     * Code obtained from http://pwnt.be/2009/08/17/simple-linear-regression-with-jfreechart
     */
    private static XYDataset regress(XYSeriesCollection data) {
        // Determine bounds
        double xMin = Double.MAX_VALUE, xMax = 0;
        for (int i = 0; i < data.getSeriesCount(); i++) {
            XYSeries ser = data.getSeries(i);
            for (int j = 0; j < ser.getItemCount(); j++) {
                double x = ser.getX(j).doubleValue();
                if (x < xMin) {
                    xMin = x;
                }
                if (x > xMax) {
                    xMax = x;
                }
            }
        }
        // Create 2-point series for each of the original series
        XYSeriesCollection coll = new XYSeriesCollection();
        for (int i = 0; i < data.getSeriesCount(); i++) {
            XYSeries ser = data.getSeries(i);
            int n = ser.getItemCount();
            double sx = 0, sy = 0, sxx = 0, sxy = 0, syy = 0;
            for (int j = 0; j < n; j++) {
                double x = ser.getX(j).doubleValue();
                double y = ser.getY(j).doubleValue();
                sx += x;
                sy += y;
                sxx += x * x;
                sxy += x * y;
                syy += y * y;
            }
            double b = (n * sxy - sx * sy) / (n * sxx - sx * sx);
            double a = sy / n - b * sx / n;
            XYSeries regr = new XYSeries(ser.getKey());
            regr.add(xMin, a + b * xMin);
            regr.add(xMax, a + b * xMax);
            coll.addSeries(regr);
        }
        return coll;
    }
}
