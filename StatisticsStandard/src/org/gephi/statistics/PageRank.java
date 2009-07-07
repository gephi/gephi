/*
Copyright 2008 WebAtlas
Authors : Patrick J. McSweeney (pjmcswee@syr.edu)
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
package org.gephi.statistics;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import org.gephi.data.attributes.api.AttributeClass;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.statistics.api.Statistics;
import org.gephi.statistics.ui.PageRankPanel;
import org.gephi.statistics.ui.api.StatisticsUI;
import org.gephi.utils.longtask.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author pjmcswee
 */
public class PageRank implements Statistics, LongTask {

    private ProgressTicket progress;
    private boolean isCanceled;
    private double epsilon = 0.001;
    private double probability = 0.5;
    private double[] pageranks;
    private boolean useUndirected;



    /**
     *
     * @param pUndirected
     */
    public void setUndirected(boolean pUndirected){
        useUndirected = pUndirected;
    }

    /**
     * 
     * @return
     */
    public boolean getUndirected(){
        return useUndirected;
    }

    /**
     * 
     * @return
     */
    public String toString() {
        return "Page Rank";
    }

    /**
     *
     * @return
     */
    public String getName() {
        return NbBundle.getMessage(PageRank.class, "PageRank_name");
    }

    /**
     *
     * @param graphController
     */
    public void execute(GraphController graphController) {
        isCanceled = false;

        Graph graph;
        if(useUndirected)
            graph = graphController.getUndirectedGraph();
        else
            graph = graphController.getDirectedGraph();

        //DirectedGraph digraph = graphController.getDirectedGraph();
        int N = graph.getNodeCount();
        pageranks = new double[N];
        double[] temp = new double[N];
        Hashtable<Node, Integer> indicies = new Hashtable<Node, Integer>();
        int index = 0;

        progress.start();
        for (Node s : graph.getNodes()) {
            indicies.put(s, index);
            pageranks[index] = 1.0f / N;
            index++;
        }

        while (true) {
            double r = 0;
            for (Node s : graph.getNodes()) {
                int s_index = indicies.get(s);
                boolean out;
                if(useUndirected)
                    out = graph.getDegree(s) > 0;
                else
                    out = ((DirectedGraph)graph).getOutDegree(s) > 0;

                if (out) {
                    r += (1.0 - probability) * (pageranks[s_index] / N);
                } else {
                    r += (pageranks[s_index] / N);
                }
                if (isCanceled) {
                    return;
                }
            }

            boolean done = true;
            for (Node s : graph.getNodes()) {
                int s_index = indicies.get(s);
                temp[s_index] = r;
                 
                EdgeIterable eIter;
                if(useUndirected)
                    eIter = ((UndirectedGraph)graph).getEdges(s);
                else
                    eIter = ((DirectedGraph)graph).getInEdges(s);

                for (Edge edge : eIter) {
                    Node neighbor = graph.getOpposite(s, edge);
                    int neigh_index = indicies.get(neighbor);
                    int normalize;
                    if(useUndirected)
                        normalize = ((UndirectedGraph)graph).getDegree(neighbor);
                    else
                        normalize = ((DirectedGraph)graph).getOutDegree(neighbor);

                    temp[s_index] += probability * (pageranks[neigh_index] / normalize);
                }

                if ((temp[s_index] - pageranks[s_index]) / pageranks[s_index] >= epsilon) {
                    done = false;
                }

                if (isCanceled) {
                    return;
                }

            }
            pageranks = temp;
            temp = new double[N];
            if ((done) || (isCanceled)) {
                break;
            }

        }

        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        AttributeClass nodeClass = ac.getTemporaryAttributeManager().getNodeClass();
        AttributeColumn pangeRanksCol = nodeClass.addAttributeColumn("pageranks", "Page Ranks", AttributeType.DOUBLE, AttributeOrigin.COMPUTED, new Double(0));

        for (Node s : graph.getNodes()) {
            int s_index = indicies.get(s);
            AttributeRow row = (AttributeRow) s.getNodeData().getAttributes();
            row.setValue(pangeRanksCol, pageranks[s_index]);
        }


    }

    /**
     *
     * @return
     */
    public boolean isParamerizable() {
        return true;
    }

    /**
     *
     * @return
     */
    public String getReport() {

        double max = 0;
        XYSeries series = new XYSeries("Series 2");
        for (int i = 0; i < pageranks.length; i++) {
            series.add(i, pageranks[i]);

        }


        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Page Ranks",
                "Node",
                "Page Rank",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        XYPlot plot = (XYPlot) chart.getPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesLinesVisible(1, false);
        renderer.setSeriesShapesVisible(1, true);
        renderer.setSeriesShape(1, new java.awt.geom.Ellipse2D.Double(0, 0, 1, 1));
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        plot.setDomainGridlinePaint(java.awt.Color.GRAY);
        plot.setRangeGridlinePaint(java.awt.Color.GRAY);

        plot.setRenderer(renderer);


        String imageFile = "";
        try {
            final ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
            final File file1 = new File("pageranks.png");
            String fullPath = file1.getAbsolutePath();

            fullPath = fullPath.replaceAll("\\\\", "\\\\\\\\");

            imageFile = "<IMG SRC=\"file:\\\\\\\\" + fullPath + "\" " + "WIDTH=\"600\" HEIGHT=\"400\" BORDER=\"0\" USEMAP=\"#chart\"></IMG>";

            File f2 = new File(fullPath);
            ChartUtilities.saveChartAsPNG(file1, chart, 600, 400, info);
        } catch (IOException e) {
            System.out.println(e.toString());
        }

        String report = "<HTML> <BODY> " + imageFile + "</BODY> </HTML>";
        return report;

    }

    /**
     *
     * @return
     */
    public boolean cancel() {
        isCanceled = true;
        return true;
    }

    /**
     *
     * @param progressTicket
     */
    public void setProgressTicket(ProgressTicket progressTicket) {
        progress = progressTicket;
    }

    /**
     * 
     * @param prob
     */
    public void setProbability(double prob) {
        probability = prob;
    }

    /**
     *
     * @param eps
     */
    public void setEpsilon(double eps) {
        epsilon = eps;
    }

    /**
     *
     * @return
     */
    public double getProbability() {
        return probability;
    }

    /**
     *
     * @return
     */
    public double getEpsilon() {
        return epsilon;
    }

    /**
     *
     * @return
     */
    public StatisticsUI getUI() {
        return new PageRankPanel.PageRankUI();
    }
}
