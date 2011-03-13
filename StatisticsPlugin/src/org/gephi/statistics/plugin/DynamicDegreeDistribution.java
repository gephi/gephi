/*
Copyright 2008-2011 Gephi
Authors : Cezary Bartosiak
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
package org.gephi.statistics.plugin;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.type.DynamicInteger;
import org.gephi.data.attributes.type.Interval;
import org.gephi.dynamic.DynamicUtilities;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.utils.TempDirUtils;
import org.gephi.utils.TempDirUtils.TempDir;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleInsets;
import org.openide.util.Exceptions;

/**
 * This class measures how closely the degree distribution of a
 * network follows a power-law scale.  An alpha value between 2 and 3
 * implies a power law. It's a dynamic version that means the statistic
 * is measured for every part (year, month etc. it depends on user's
 * settings) of the given time interval.
 *
 * @author Cezary Bartosiak
 */
public class DynamicDegreeDistribution extends DynamicStatisticsImpl {

    private boolean directed;
    private List<Interval<Double>> inDegrees = new ArrayList<Interval<Double>>();
    private List<Interval<Double>> outDegrees = new ArrayList<Interval<Double>>();
    private List<Interval<Double>> combinedDegrees = new ArrayList<Interval<Double>>();

    /**
     * Indicates if this network should be directed or undirected.
     *
     * @param directed indicates the metric's interpretation of this network
     */
    public void setDirected(boolean directed) {
        this.directed = directed;
    }

    /**
     * Returns {@code true} if this network should be directed,
     * otherwise {@code false}.
     *
     * @return {@code true} if this network should be directed,
     *         otherwise {@code false}.
     */
    public boolean isDirected() {
        return directed;
    }

    @Override
    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        HierarchicalGraph graph;
        if (directed) {
            graph = graphModel.getHierarchicalDirectedGraphVisible();
        } else {
            graph = graphModel.getHierarchicalUndirectedGraphVisible();
        }
        execute(graph, attributeModel);
    }

    @Override
    protected void preloop() {
        inDegrees.clear();
        outDegrees.clear();
        combinedDegrees.clear();
    }

    @Override
    protected void inloop(double low, double high, HierarchicalGraph g, AttributeModel attributeModel) {
        /*DegreeDistribution dd = new DegreeDistribution();
        dd.setDirected(directed);
        dd.execute(g, attributeModel);

        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn dynamicInDegree = nodeTable.getColumn("dynamicInDegree");
        AttributeColumn dynamicOutDegree = nodeTable.getColumn("dynamicOutDegree");
        AttributeColumn dynamicDegree = nodeTable.getColumn("dynamicDegree");
        if (dynamicInDegree == null) {
            dynamicInDegree = nodeTable.addColumn("dynamicInDegree", "Dynamic In Degree",
                    AttributeType.DYNAMIC_INT, AttributeOrigin.COMPUTED, new DynamicInteger());
        }
        if (dynamicOutDegree == null) {
            dynamicOutDegree = nodeTable.addColumn("dynamicOutDegree", "Dynamic Out Degree",
                    AttributeType.DYNAMIC_INT, AttributeOrigin.COMPUTED, new DynamicInteger());
        }
        if (dynamicDegree == null) {
            dynamicDegree = nodeTable.addColumn("dynamicDegree", "Dynamic Degree",
                    AttributeType.DYNAMIC_INT, AttributeOrigin.COMPUTED, new DynamicInteger());
        }

        for (Node node : g.getNodes()) {
            AttributeRow row = (AttributeRow) node.getNodeData().getAttributes();
            if (directed) {
                row.setValue(dynamicInDegree, new DynamicInteger((DynamicInteger) row.getValue(dynamicInDegree),
                        new Interval<Integer>(low, high, ((DirectedGraph) g).getInDegree(node))));
                row.setValue(dynamicOutDegree, new DynamicInteger((DynamicInteger) row.getValue(dynamicOutDegree),
                        new Interval<Integer>(low, high, ((DirectedGraph) g).getOutDegree(node))));
            } else {
                row.setValue(dynamicDegree, new DynamicInteger((DynamicInteger) row.getValue(dynamicDegree),
                        new Interval<Integer>(low, high, ((UndirectedGraph) g).getDegree(node))));
            }
        }

        inDegrees.add(new Interval<Double>(low, high, dd.getInPowerLaw()));
        outDegrees.add(new Interval<Double>(low, high, dd.getOutPowerLaw()));
        combinedDegrees.add(new Interval<Double>(low, high, dd.getCombinedPowerLaw()));*/
    }

    @Override
    protected String getReportName() {
        return "Dynamic Degree Distribution Metric Report";
    }

    @Override
    protected String getAdditionalParameters() {
        return "Network Interpretation: " + (directed ? "directed" : "undirected");
    }

    @Override
    protected String getResults() {
        if (directed) {
            return getDirectedReport();
        } else {
            return getUndirectedReport();
        }
    }

    private String getDirectedReport() {
        String tableContent = "";
        for (int i = 0; i < inDegrees.size(); ++i) {
            String interval = "[";
            interval += DynamicUtilities.getXMLDateStringFromDouble(inDegrees.get(i).getLow()).replace('T', ' ').
                    substring(0, 19) + ", ";
            interval += DynamicUtilities.getXMLDateStringFromDouble(inDegrees.get(i).getHigh()).replace('T', ' ').
                    substring(0, 19);
            interval += "]";

            tableContent += "<tr>";
            tableContent += "<td>";
            tableContent += interval;
            tableContent += "</td>";
            tableContent += "<td>";
            tableContent += inDegrees.get(i).getValue();
            tableContent += "</td>";
            tableContent += "<td>";
            tableContent += outDegrees.get(i).getValue();
            tableContent += "</td>";
            tableContent += "</tr>";
        }
        String table =
                "<table>"
                + "<tr>"
                + "<td><b>Time interval</b></td>"
                + "<td><b>In-Degree Power Law</b></td>"
                + "<td><b>Out-Degree Power Law</b></td>"
                + "</tr>"
                + tableContent
                + "</table>";

        String inImage = "";
        try {
            double inMin = Double.POSITIVE_INFINITY;
            double inMax = Double.NEGATIVE_INFINITY;
            DefaultCategoryDataset inDataset = new DefaultCategoryDataset();
            for (int i = 0; i < inDegrees.size(); ++i) {
                if (inMin > inDegrees.get(i).getValue()) {
                    inMin = inDegrees.get(i).getValue();
                }
                if (inMax < inDegrees.get(i).getValue()) {
                    inMax = inDegrees.get(i).getValue();
                }
                String interval = "[";
                interval += DynamicUtilities.getXMLDateStringFromDouble(inDegrees.get(i).getLow()).replace('T', ' ').
                        substring(0, 19) + ", ";
                interval += DynamicUtilities.getXMLDateStringFromDouble(inDegrees.get(i).getHigh()).replace('T', ' ').
                        substring(0, 19);
                interval += "]";
                inDataset.addValue(inDegrees.get(i).getValue(), "in degrees", interval);
            }

            JFreeChart inChart = ChartFactory.createLineChart(
                    "In-Degree Power Law",
                    "Time intervals",
                    "In-Degree Power Law",
                    inDataset,
                    PlotOrientation.VERTICAL,
                    true,
                    false,
                    false);
            inChart.setPadding(new RectangleInsets(0, 80, 0, 0));
            CategoryPlot inPlot = (CategoryPlot) inChart.getPlot();
            inPlot.setBackgroundPaint(Color.WHITE);
            inPlot.setDomainGridlinePaint(Color.GRAY);
            inPlot.setRangeGridlinePaint(Color.GRAY);
            CategoryAxis inDomainAxis = inPlot.getDomainAxis();
            inDomainAxis.setLowerMargin(0.0);
            inDomainAxis.setUpperMargin(0.0);
            inDomainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
            NumberAxis inRangeAxis = (NumberAxis) inPlot.getRangeAxis();
            inRangeAxis.setTickUnit(new NumberTickUnit(0.05));
            inRangeAxis.setRange(inMin - 0.01 * inMin, inMax + 0.01 * inMax);

            ChartRenderingInfo inInfo = new ChartRenderingInfo(new StandardEntityCollection());
            TempDir inTempDir = TempDirUtils.createTempDir();
            String inFileName = "inDynamicDistribution.png";
            File inFile = inTempDir.createFile(inFileName);
            inImage = "<img src=\"file:" + inFile.getAbsolutePath() + "\" " + "width=\"600\" height=\"600\" "
                    + "border=\"0\" usemap=\"#chart\"></img>";
            ChartUtilities.saveChartAsPNG(inFile, inChart, 600, 600, inInfo);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        String outImage = "";
        try {
            double outMin = Double.POSITIVE_INFINITY;
            double outMax = Double.NEGATIVE_INFINITY;
            DefaultCategoryDataset outDataset = new DefaultCategoryDataset();
            for (int i = 0; i < outDegrees.size(); ++i) {
                if (outMin > outDegrees.get(i).getValue()) {
                    outMin = outDegrees.get(i).getValue();
                }
                if (outMax < outDegrees.get(i).getValue()) {
                    outMax = outDegrees.get(i).getValue();
                }
                String interval = "[";
                interval += DynamicUtilities.getXMLDateStringFromDouble(outDegrees.get(i).getLow()).replace('T', ' ').
                        substring(0, 19) + ", ";
                interval += DynamicUtilities.getXMLDateStringFromDouble(outDegrees.get(i).getHigh()).replace('T', ' ').
                        substring(0, 19);
                interval += "]";
                outDataset.addValue(outDegrees.get(i).getValue(), "out degrees", interval);
            }

            JFreeChart outChart = ChartFactory.createLineChart(
                    "Out-Degree Power Law",
                    "Time intervals",
                    "Out-Degree Power Law",
                    outDataset,
                    PlotOrientation.VERTICAL,
                    true,
                    false,
                    false);
            outChart.setPadding(new RectangleInsets(0, 80, 0, 0));
            CategoryPlot outPlot = (CategoryPlot) outChart.getPlot();
            outPlot.setBackgroundPaint(Color.WHITE);
            outPlot.setDomainGridlinePaint(Color.GRAY);
            outPlot.setRangeGridlinePaint(Color.GRAY);
            CategoryAxis outDomainAxis = outPlot.getDomainAxis();
            outDomainAxis.setLowerMargin(0.0);
            outDomainAxis.setUpperMargin(0.0);
            outDomainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
            NumberAxis outRangeAxis = (NumberAxis) outPlot.getRangeAxis();
            outRangeAxis.setTickUnit(new NumberTickUnit(0.05));
            outRangeAxis.setRange(outMin - 0.01 * outMin, outMax + 0.01 * outMax);

            ChartRenderingInfo outInfo = new ChartRenderingInfo(new StandardEntityCollection());
            TempDir outTempDir = TempDirUtils.createTempDir();
            String outFileName = "outDynamicDistribution.png";
            File outFile = outTempDir.createFile(outFileName);
            outImage = "<img src=\"file:" + outFile.getAbsolutePath() + "\" " + "width=\"600\" height=\"600\" "
                    + "border=\"0\" usemap=\"#chart\"></img>";
            ChartUtilities.saveChartAsPNG(outFile, outChart, 600, 600, outInfo);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        String report = new String(
                table + "<br>"
                + inImage + "<br>"
                + outImage + "<br>");

        return report;
    }

    private String getUndirectedReport() {
        String tableContent = "";
        for (int i = 0; i < combinedDegrees.size(); ++i) {
            String interval = "[";
            interval += DynamicUtilities.getXMLDateStringFromDouble(combinedDegrees.get(i).getLow()).replace('T', ' ').
                    substring(0, 19) + ", ";
            interval += DynamicUtilities.getXMLDateStringFromDouble(combinedDegrees.get(i).getHigh()).replace('T', ' ').
                    substring(0, 19);
            interval += "]";

            tableContent += "<tr>";
            tableContent += "<td>";
            tableContent += interval;
            tableContent += "</td>";
            tableContent += "<td>";
            tableContent += combinedDegrees.get(i).getValue();
            tableContent += "</td>";
            tableContent += "</tr>";
        }
        String table =
                "<table>"
                + "<tr>"
                + "<td><b>Time interval</b></td>"
                + "<td><b>Combined-Degree Power Law</b></td>"
                + "</tr>"
                + tableContent
                + "</table>";

        String image = "";
        try {
            double min = Double.POSITIVE_INFINITY;
            double max = Double.NEGATIVE_INFINITY;
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (int i = 0; i < combinedDegrees.size(); ++i) {
                if (min > combinedDegrees.get(i).getValue()) {
                    min = combinedDegrees.get(i).getValue();
                }
                if (max < combinedDegrees.get(i).getValue()) {
                    max = combinedDegrees.get(i).getValue();
                }
                String interval = "[";
                interval += DynamicUtilities.getXMLDateStringFromDouble(combinedDegrees.get(i).getLow()).
                        replace('T', ' ').substring(0, 19) + ", ";
                interval += DynamicUtilities.getXMLDateStringFromDouble(combinedDegrees.get(i).getHigh()).
                        replace('T', ' ').substring(0, 19);
                interval += "]";
                dataset.addValue(combinedDegrees.get(i).getValue(), "combined degrees", interval);
            }

            JFreeChart chart = ChartFactory.createLineChart(
                    "Combined-Degree Power Law",
                    "Time intervals",
                    "Combined-Degree Power Law",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    false,
                    false);
            chart.setPadding(new RectangleInsets(0, 80, 0, 0));
            CategoryPlot plot = (CategoryPlot) chart.getPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setDomainGridlinePaint(Color.GRAY);
            plot.setRangeGridlinePaint(Color.GRAY);
            CategoryAxis domainAxis = plot.getDomainAxis();
            domainAxis.setLowerMargin(0.0);
            domainAxis.setUpperMargin(0.0);
            domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setTickUnit(new NumberTickUnit(0.05));
            rangeAxis.setRange(min - 0.01 * min, max + 0.01 * max);

            ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
            TempDir tempDir = TempDirUtils.createTempDir();
            String fileName = "combinedDynamicDistribution.png";
            File file = tempDir.createFile(fileName);
            image = "<img src=\"file:" + file.getAbsolutePath() + "\" " + "width=\"600\" height=\"600\" "
                    + "border=\"0\" usemap=\"#chart\"></img>";
            ChartUtilities.saveChartAsPNG(file, chart, 600, 600, info);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        String report = new String(
                table + "<br>"
                + image + "<br>");

        return report;
    }
}
