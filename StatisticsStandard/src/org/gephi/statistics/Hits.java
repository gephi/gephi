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

import java.util.Hashtable;
import java.util.LinkedList;
import org.gephi.data.attributes.api.AttributeClass;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.statistics.api.Statistics;
import org.gephi.statistics.ui.HitsPanel;
import org.gephi.statistics.ui.api.StatisticsUI;
import org.gephi.utils.longtask.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author pjmcswee
 */
public class Hits implements Statistics, LongTask {

    private boolean isCanceled;
    private ProgressTicket progress;
    private double[] authority;
    private double[] hubs;
    private double probability;
    private double epsilon;

    /**
     *
     * @return
     */
    public String getName() {
        return NbBundle.getMessage(Hits.class, "Hits_name");
    }

    /**
     * 
     * @param graphController
     */
    public void execute(GraphController graphController) {

        DirectedGraph digraph = graphController.getDirectedGraph();
        int N = digraph.getNodeCount();

        authority = new double[N];
        hubs = new double[N];
        double[] temp_authority = new double[N];
        double[] temp_hubs = new double[N];

        LinkedList<Node> hub_list = new LinkedList<Node>();
        LinkedList<Node> auth_list = new LinkedList<Node>();

        progress.start();

        Hashtable<Node, Integer> indicies = new Hashtable<Node, Integer>();
        int index = 0;
        for (Node node : digraph.getNodes()) {
            indicies.put(node, new Integer(index));
            index++;

            if (digraph.getOutDegree(node) > 0) {
                hub_list.add(node);
            }
            if (digraph.getInDegree(node) > 0) {
                auth_list.add(node);
            }
        }


        for (Node node : hub_list) {
            int n_index = indicies.get(node);
            hubs[n_index] = 1.0f / hub_list.size();
        }
        for (Node node : auth_list) {
            int n_index = indicies.get(node);
            authority[n_index] = 1.0f / auth_list.size();
        }

        while (true) {

            boolean done = true;
            for (Node node : auth_list) {

                int n_index = indicies.get(node);
                temp_authority[n_index] = (1.0 - probability) / auth_list.size();

                EdgeIterable edge_iter = digraph.getInEdges(node);
                for (Edge edge : edge_iter) {
                    Node target = digraph.getOpposite(node, edge);
                    int target_index = indicies.get(target);
                    temp_authority[n_index] += probability * (hubs[target_index] / digraph.getInDegree(node));
                }

                if (((temp_authority[n_index] - authority[n_index]) / authority[n_index]) >= epsilon) {
                    done = false;
                }
                if (isCanceled) {
                    break;
                }

            }

            for (Node node : hub_list) {

                int n_index = indicies.get(node);
                temp_hubs[n_index] = (1.0 - probability) / hub_list.size();

                EdgeIterable edge_iter = digraph.getOutEdges(node);
                for (Edge edge : edge_iter) {
                    Node target = digraph.getOpposite(node, edge);
                    int target_index = indicies.get(target);
                    temp_hubs[n_index] += probability * (authority[target_index] / digraph.getOutDegree(node));
                }

                if (((temp_hubs[n_index] - hubs[n_index]) / hubs[n_index]) >= epsilon) {
                    done = false;
                }
                if (isCanceled) {
                    break;
                }
            }

            authority = temp_authority;
            hubs = temp_hubs;
            temp_authority = new double[N];
            temp_hubs = new double[N];

            if ((done) || (isCanceled)) {
                break;
            }
        }

        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        AttributeClass nodeClass = ac.getTemporaryAttributeManager().getNodeClass();

        AttributeColumn authorityCol = nodeClass.addAttributeColumn("authority", "Authortiy", AttributeType.FLOAT, AttributeOrigin.COMPUTED, new Float(0));
        AttributeColumn hubsCol = nodeClass.addAttributeColumn("hub", "Hub", AttributeType.FLOAT, AttributeOrigin.COMPUTED, new Float(0));

        for (Node s : digraph.getNodes()) {
            int s_index = indicies.get(s);
            AttributeRow row = (AttributeRow) s.getNodeData().getAttributes();
            row.setValue(authorityCol, (float)authority[s_index]);
            row.setValue(hubsCol, (float) hubs[s_index]);
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
    public StatisticsUI getUI() {
        return new HitsPanel.HitsUI();
    }

    /**
     *
     * @return
     */
    public String getReport() {
        return new String();
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
}
