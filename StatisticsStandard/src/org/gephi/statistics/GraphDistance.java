/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.statistics;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import javax.swing.JPanel;
import org.gephi.statistics.api.Statistics;
import org.gephi.graph.api.*;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Stack;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import org.gephi.data.attributes.api.AttributeClass;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
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
public class GraphDistance implements Statistics, LongTask {

    private double[] betweeness;
    private int[][] dist;
    private double[] closeness;
    private double[] eccentricity;
    private int diameter;
    private double avgDist;
    private int N;
    private boolean directed;
    private ProgressTicket progress;
    private boolean isCanceled;

    /**
     * 
     * @return
     */
    public String getName() {
        return NbBundle.getMessage(GraphDistance.class, "GraphDistance_name");

    }

    /**
     * 
     * @param synchReader
     */
    public void brandes(GraphController graphController) {
        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        AttributeClass nodeClass = ac.getTemporaryAttributeManager().getNodeClass();
        AttributeColumn eccentricityCol = nodeClass.addAttributeColumn("eccentricity", "Eccentricity", AttributeType.DOUBLE, AttributeOrigin.COMPUTED, new Double(0));
        AttributeColumn closenessCol = nodeClass.addAttributeColumn("closnesscentrality", "Closeness Centrality", AttributeType.DOUBLE, AttributeOrigin.COMPUTED, new Double(0));
        AttributeColumn betweenessCol = nodeClass.addAttributeColumn("betweenesscentrality", "Betweeness Centrality", AttributeType.DOUBLE, AttributeOrigin.COMPUTED, new Double(0));


        Graph graph = null;
        if (directed) {
            graph = graphController.getDirectedGraph();
        } else {
            graph = graphController.getUndirectedGraph();
        }

        N = graph.getNodeCount();

        betweeness = new double[N];
        dist = new int[N][N];
        eccentricity = new double[N];
        closeness = new double[N];
        diameter = 0;
        avgDist = 0;

        Hashtable<Node, Integer> indicies = new Hashtable<Node, Integer>();
        int index = 0;
        for (Node s : graph.getNodes()) {
            indicies.put(s, index);
            index++;
        }

        progress.start(graph.getNodeCount());
        int count = 0;
        for (Node s : graph.getNodes()) {
            Stack<Node> S = new Stack<Node>();

            LinkedList<Node>[] P = new LinkedList[N];
            double[] theta = new double[N];
            double[] d = new double[N];
            for (int j = 0; j < N; j++) {
                P[j] = new LinkedList<Node>();
                theta[j] = 0;
                d[j] = -1;
            }

            int s_index = indicies.get(s);

            theta[s_index] = 1;
            d[s_index] = 0;

            LinkedList<Node> Q = new LinkedList<Node>();
            Q.addLast(s);
            while (!Q.isEmpty()) {
                Node v = Q.removeFirst();
                S.push(v);
                int v_index = indicies.get(v);

                EdgeIterable edgeIter = null;
                if (directed) {
                    edgeIter = ((DirectedGraph) graph).getOutEdges(v);
                } else {
                    edgeIter = graph.getEdges(v);
                }

                for (Edge edge : edgeIter) {
                    Node reachable = graph.getOpposite(v, edge);

                    int r_index = indicies.get(reachable);
                    if (d[r_index] < 0) {
                        Q.addLast(reachable);
                        d[r_index] = d[v_index] + 1;
                    }
                    if (d[r_index] == (d[v_index] + 1)) {
                        theta[r_index] = theta[r_index] + theta[v_index];
                        P[r_index].addLast(v);
                    }
                }
            }
            for (int i = 0; i < N; i++) {
                if (d[i] > 0) {
                    avgDist += d[i];
                    dist[s_index][i] = (int) d[i];
                    eccentricity[s_index] = (int) Math.max(eccentricity[s_index], d[i]);
                    closeness[s_index] += d[i];
                    diameter = Math.max(diameter, dist[s_index][i]);
                }
            }
            closeness[s_index] /= N;

            double[] delta = new double[N];
            while (!S.empty()) {
                Node w = S.pop();
                int w_index = indicies.get(w);
                ListIterator<Node> iter1 = P[w_index].listIterator();
                while (iter1.hasNext()) {
                    Node u = iter1.next();
                    int u_index = indicies.get(u);
                    delta[u_index] += (theta[u_index] / theta[w_index]) * (1 + delta[w_index]);
                }
                if (w != s) {
                    betweeness[w_index] += delta[w_index];
                }
            }
            count++;
            if (isCanceled) {
                return;
            }
            progress.progress(count);
        }



        avgDist /= N * (N - 1.0f);

        for (Node s : graph.getNodes()) {
            AttributeRow row = (AttributeRow) s.getNodeData().getAttributes();
            int s_index = indicies.get(s);
            row.setValue(eccentricityCol, eccentricity[s_index]);
            row.setValue(closenessCol, closeness[s_index]);
            row.setValue(betweenessCol, betweeness[s_index]);
        }
    }

    /**
     *
     * @param graphController
     * @param progressMonitor
     */
    public void execute(GraphController graphController) {
        isCanceled = false;
        brandes(graphController);
    }

    /**
     *
     * @return
     */
    public String toString() {
        return new String("Graph Distance");
    }

    /**
     *
     *  @return
     */
    public boolean isParamerizable() {
        return true;
    }
    final JRadioButton directedButton = new JRadioButton("Directed: ");
    final JRadioButton undirectedButton = new JRadioButton("Undirected: ");

    /**
     *
     * @return
     */
    public JPanel getPanel() {
        JPanel panel = new JPanel();

        directedButton.setSelected(true);
        ButtonGroup group = new ButtonGroup();
        group.add(directedButton);
        group.add(undirectedButton);
        panel.add(directedButton);
        panel.add(undirectedButton);
        return panel;
    }

    /**
     * 
     * @param pVals
     * @param pName
     * @param pX
     * @param pY
     * @return
     */
    private String createImageFile(double[] pVals, String pName, String pX, String pY) {
        XYSeries series = new XYSeries(pName);
        for (int i = 0; i < N; i++) {
            series.add(i, pVals[i]);
        }
        XYSeriesCollection dataSet = new XYSeriesCollection();
        dataSet.addSeries(series);

        JFreeChart chart = ChartFactory.createXYLineChart(
                pName,
                pX,
                pY,
                dataSet,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        XYPlot plot = (XYPlot) chart.getPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(0, 0, 1, 1));
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        plot.setDomainGridlinePaint(java.awt.Color.GRAY);
        plot.setRangeGridlinePaint(java.awt.Color.GRAY);
        plot.setRenderer(renderer);

        String imageFile = "";
        try {
            final ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
            final File file1 = new File(pY + ".png");
            String fullPath = file1.getAbsolutePath();

            fullPath = fullPath.replaceAll("\\\\", "\\\\\\\\");

            imageFile = "<IMG SRC=\"file:\\\\\\\\" + fullPath + "\" " + "WIDTH=\"600\" HEIGHT=\"400\" BORDER=\"0\" USEMAP=\"#chart\"></IMG>";

            File f2 = new File(fullPath);
            ChartUtilities.saveChartAsPNG(file1, chart, 600, 400, info);
        } catch (IOException e) {
            System.out.println(e.toString());
        }

        return imageFile;
    }

    /**
     *
     * @return
     */
    public String getReport() {

        String htmlIMG1 = createImageFile(betweeness, "Betweeness Centrality", "Nodes", "Betweeness");
        String htmlIMG2 = createImageFile(closeness, "Closness Centrality", "Nodes", "Closness");
        String htmlIMG3 = createImageFile(eccentricity, "Eccentricty", "Nodes", "Eccentricity");
        return new String("<HTML><head><META NAME=\"keywords\" CONTENT=\"TEXT_HTML\"> </head> <body> <b>Average Degree:</b> " + avgDist + "<br> <b>Diameter : </b>" + diameter +
                "<br>" + htmlIMG1 + "<br>" +
                "<br>" + htmlIMG2 + "<br>" +
                "<br>" + htmlIMG3 + "<br>" +
                "</body></HTML>");
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
}
