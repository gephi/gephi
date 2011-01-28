/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.ui.utils;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import org.gephi.utils.HTMLEscape;
import org.gephi.utils.TempDirUtils;
import org.gephi.utils.TempDirUtils.TempDir;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Utils class to build and change charts.
 * Scatter plots implemented to be able to draw or not lines and linear regression.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class ChartsUtils {

    /**
     * Prepares a HTML report for the given statistics data and charts.
     * For preparing the statistics data see the method <code>getAllStatistics</code> of the <code>StatisticsUtils</code> class.
     * @param dataName Name of the data
     * @param statistics Statistics obtained from the method <code>getAllStatistics</code> of the <code>StatisticsUtils</code> class
     * @param boxPlot Box-plot jfreechart or null
     * @param scatterPlot Scatter-plot jfreechart or null
     * @param histogram Histogram-plot jfreechart or null
     * @param boxPlotDimension Dimension for the box-plot or null to use a default dimension
     * @param scatterPlotDimension Dimension for the scatter plot or null to use a default dimension
     * @param histogramDimension Dimension for the histogram or null to use a default dimension
     * @return
     */
    public static String getStatisticsReportHTML(final String dataName, final BigDecimal[] statistics, final JFreeChart boxPlot, final JFreeChart scatterPlot, final JFreeChart histogram, final Dimension boxPlotDimension, final Dimension scatterPlotDimension, final Dimension histogramDimension) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append(NbBundle.getMessage(ChartsUtils.class, "ChartsUtils.report.header", HTMLEscape.stringToHTMLString(dataName)));
        sb.append("<hr>");
        if (statistics != null) {//There are numbers and statistics can be shown:
            sb.append("<ul>");
            writeStatistic(sb, "ChartsUtils.report.average", statistics[0]);
            writeStatistic(sb, "ChartsUtils.report.Q1", statistics[1]);
            writeStatistic(sb, "ChartsUtils.report.median", statistics[2]);
            writeStatistic(sb, "ChartsUtils.report.Q3", statistics[3]);
            writeStatistic(sb, "ChartsUtils.report.IQR", statistics[4]);
            writeStatistic(sb, "ChartsUtils.report.sum", statistics[5]);
            writeStatistic(sb, "ChartsUtils.report.min", statistics[6]);
            writeStatistic(sb, "ChartsUtils.report.max", statistics[7]);
            sb.append("</ul>");
            try {
                if (boxPlot != null) {
                    sb.append("<hr>");
                    writeBoxPlot(sb, boxPlot, boxPlotDimension);
                }
                if (scatterPlot != null) {
                    sb.append("<hr>");
                    writeScatterPlot(sb, scatterPlot, scatterPlotDimension);
                }
                if (histogram != null) {
                    sb.append("<hr>");
                    writeHistogram(sb, histogram, histogramDimension);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            sb.append(getMessage("ChartsUtils.report.empty"));
        }
        sb.append("</html>");
        return sb.toString();
    }

    /**
     * Build a new box-plot from an array of numbers using a default title and yLabel.
     * String dataName will be used for xLabel.
     * @param numbers Numbers for building box-plot
     * @param dataName Name of the numbers data
     * @return Prepared box-plot
     */
    public static JFreeChart buildBoxPlot(final Number[] numbers, final String dataName) {
        if (numbers == null || numbers.length == 0) {
            return null;
        }
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        final ArrayList<Number> list = new ArrayList<Number>();
        list.addAll(Arrays.asList(numbers));

        final String valuesString = getMessage("ChartsUtils.report.box-plot.values");
        dataset.add(list, valuesString, "");

        final BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
        renderer.setMeanVisible(false);
        renderer.setFillBox(false);
        renderer.setMaximumBarWidth(0.5);

        final CategoryAxis xAxis = new CategoryAxis(dataName);
        final NumberAxis yAxis = new NumberAxis(getMessage("ChartsUtils.report.box-plot.values-range"));
        yAxis.setAutoRangeIncludesZero(false);
        renderer.setBaseToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
        final CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);
        plot.setRenderer(renderer);

        JFreeChart boxPlot = new JFreeChart(getMessage("ChartsUtils.report.box-plot.title"), plot);
        return boxPlot;
    }

    /**
     * Build new scatter plot from numbers array using a default title and xLabel.
     * String dataName will be used for yLabel.
     * Appearance can be changed later with the other methods of ChartsUtils.
     * @param numbers Numbers for the scatter plot
     * @param dataName Name of the numbers data
     * @param useLines Indicates if lines have to be drawn instead of shapes
     * @param useLinearRegression Indicates if the scatter plot has to have linear regreesion line drawn
     * @return Scatter plot for the data and appearance options
     */
    public static JFreeChart buildScatterPlot(final Number[] numbers, final String dataName, final boolean useLines, final boolean useLinearRegression) {
        if (numbers == null || numbers.length == 0) {
            return null;
        }
        XYSeriesCollection dataset = new XYSeriesCollection();

        XYSeries series = new XYSeries(dataName);
        for (int i = 0; i < numbers.length; i++) {
            series.add(i, numbers[i]);
        }
        dataset.addSeries(series);
        JFreeChart scatterPlot = buildScatterPlot(dataset,
                getMessage("ChartsUtils.report.scatter-plot.title"),
                getMessage("ChartsUtils.report.scatter-plot.xLabel"),
                dataName,
                useLines,
                useLinearRegression);

        return scatterPlot;
    }

    /**
     * Build new Scatter plot. Appearance can be changed later with the other methods of ChartsUtils.
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
     * Build new histogram from the given numbers array using a default title and xLabel.
     * String dataName will be used for yLabel.
     * @param numbers Numbers for the histogram
     * @param dataName Name of the numbers data
     * @param divisions Divisions for the histogram
     * @return Prepared histogram
     */
    public static JFreeChart buildHistogram(final Number[] numbers, final String dataName, final int divisions) {
        if (numbers == null || numbers.length == 0) {
            return null;
        }

        HistogramDataset dataset = new HistogramDataset();
        dataset.setType(HistogramType.FREQUENCY);
        double[] doubleNumbers = new double[numbers.length];
        for (int i = 0; i < doubleNumbers.length; i++) {
            doubleNumbers[i] = numbers[i].doubleValue();
        }

        dataset.addSeries(dataName, doubleNumbers, divisions > 0 ? divisions : 10);//Use 10 divisions if divisions number is invalid.

        JFreeChart histogram = ChartFactory.createHistogram(
                getMessage("ChartsUtils.report.histogram.title"),
                dataName,
                getMessage("ChartsUtils.report.histogram.yLabel"),
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);

        return histogram;
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

    /********Private methods*********/

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

    private static void writeStatistic(StringBuilder sb, String resName, BigDecimal number) {
        sb.append("<li>");
        sb.append(getMessage(resName));
        sb.append(": ");
        sb.append(number);
        sb.append("</li>");
    }

    private static void writeBoxPlot(final StringBuilder sb, JFreeChart boxPlot, Dimension dimension) throws IOException {
        if (dimension == null) {
            dimension = new Dimension(300, 500);
        }
        writeChart(sb, boxPlot, dimension, "box-plot.png");
    }

    private static void writeScatterPlot(final StringBuilder sb, JFreeChart scatterPlot, Dimension dimension) throws IOException {
        if (dimension == null) {
            dimension = new Dimension(600, 400);
        }
        writeChart(sb, scatterPlot, dimension, "scatter-plot.png");
    }

    private static void writeHistogram(final StringBuilder sb, final JFreeChart histogram, Dimension dimension) throws IOException {
        if (dimension == null) {
            dimension = new Dimension(600, 400);
        }
        writeChart(sb, histogram, dimension, "histogram.png");
    }

    private static void writeChart(final StringBuilder sb, final JFreeChart chart, final Dimension dimension, final String fileName) throws IOException {
        TempDir tempDir = TempDirUtils.createTempDir();
        String imageFile = "";
        File file = tempDir.createFile(fileName);
        imageFile = "<center><img src=\"file:" + file.getAbsolutePath() + "\"</img></center>";
        ChartUtilities.saveChartAsPNG(file, chart, dimension.width, dimension.height);

        sb.append(imageFile);
    }

    private static String getMessage(String resName) {
        return NbBundle.getMessage(ChartsUtils.class, resName);
    }
}
