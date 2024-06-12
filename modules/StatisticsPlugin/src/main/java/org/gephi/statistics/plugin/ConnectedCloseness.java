/*
 Copyright 2008-2024 Gephi
 Authors : Mathieu Jacomy
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

package org.gephi.statistics.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;

public class ConnectedCloseness implements Statistics, LongTask {

    /**
     * Algorithm settings
     */
    public static final double epsilon = 0.03;
    public static final double grid_size = 10; // This is an optimization thing, it's not the graphical grid

    /**
     * Remembers if the Cancel function has been called.
     */
    private boolean isCanceled;

    /**
     * Keep track of the work done.
     */
    private ProgressTicket progress;

    /**
     * @param graphModel
     */
    @Override
    public void execute(GraphModel graphModel) {
        Graph g = graphModel.getGraphVisible();
        execute(g);
    }

    public void execute(Graph g) {
        isCanceled = false;

        g.readLock();
        
        IndicatorResults indicators = computeConnectedCloseness(g);

        // TODO: do something with the indicators

        try {
            // graph.setAttribute(AVERAGE_DEGREE, avgDegree);
        } finally {
            g.readUnlockAll();
        }
    }

    public IndicatorResults computeConnectedCloseness(Graph g) {
        final List<Double> pairs_of_nodes_sampled = sample_pairs_of_nodes_distances(g);
        List<Double> connected_pairs = new ArrayList<>();
        for (Edge e : g.getEdges()) {
            Node n1 = e.getSource();
            Node n2 = e.getTarget();
            // Compute distance
            double d = Math.sqrt(Math.pow(n1.x() - n2.x(), 2) + Math.pow(n1.y() - n2.y(), 2));
            connected_pairs.add(d);
        }

        // Grid search for C_max

        double[] range = {0, Math.max(Collections.max(pairs_of_nodes_sampled), Collections.max(connected_pairs))};

        double C_max = 0;
        Map<Double, IndicatorResults> distances_index = new HashMap<>();
        double Delta;
        double old_C_max;
        double C;
        int target_index = -1;
        List<IndicatorResults> indicators_over_Delta;

        do {
            for (int i=0; i<=grid_size; i++){
                Delta = range[0] + (range[1]-range[0]) * i / grid_size;
                if (!distances_index.containsKey(Delta)) {
                    distances_index.put(Delta, computeIndicators(Delta, g, pairs_of_nodes_sampled, connected_pairs));
                }
            }
            old_C_max = C_max;
            C_max = 0;
            indicators_over_Delta = (List<IndicatorResults>) distances_index.values();
            int i = 0;
            for (IndicatorResults indicators:indicators_over_Delta){
                C = indicators.C;
                if (C > C_max) {
                    C_max = C;
                    target_index = i;
                }
                i++;
            }

            range = new double[] {indicators_over_Delta.get((int) Math.max(0, target_index - 1)).Delta,
                indicators_over_Delta.get((int) Math.min(indicators_over_Delta.size() - 1, target_index + 1)).Delta};
        } while ( (C_max-old_C_max)/C_max >= epsilon/10 );

        double Delta_max = find_Delta_max(indicators_over_Delta, epsilon);

        IndicatorResults indicators_of_Delta_max = computeIndicators(Delta_max, g, pairs_of_nodes_sampled, connected_pairs);

        // Resistance to misinterpretation
        if (indicators_of_Delta_max.C < 0.1) {
            return new IndicatorResults(Double.NaN, Double.NaN, Double.NaN, Double.NaN, indicators_of_Delta_max.C);
        } else {
            return indicators_of_Delta_max;
        }
    }

    public double find_Delta_max(List<IndicatorResults> indicators_over_Delta, double epsilon) {
	  double C_max = Collections.max(
          (List<Double>) indicators_over_Delta.stream().map(indicators -> {
              return indicators.C;
          })
      );
      double Delta_max = Collections.min(
          (List<Double>) indicators_over_Delta.stream().filter(indicators -> {
              return indicators.C >= (1-epsilon) * C_max;
          }).map(indicators -> {
              return indicators.Delta;
          })
      );
      return Delta_max;
    }

    // Compute indicators given a distance Delta
    public IndicatorResults computeIndicators(double Delta, Graph g, List<Double> pairs_of_nodes_sampled, List<Double> connected_pairs) {
        List<Double> connected_pairs_below_Delta = connected_pairs.stream().filter(d -> d <= Delta).collect(Collectors.toList());
        List<Double> pairs_below_Delta = pairs_of_nodes_sampled.stream().filter(d -> d <= Delta).collect(Collectors.toList());

        // Count of edges shorter than Delta
        // note: actual count
	    double E = connected_pairs_below_Delta.size();

        // Proportion of edges shorter than Delta
        // note: actual count
	    double E_percent = E / connected_pairs.size();

        // Count of node pairs closer than Delta
        // note: sampling-dependent
	    double p = pairs_below_Delta.size();

        // Proportion of node pairs closer than Delta
        // note: sampling-dependent, but it cancels out
	    double p_percent = p / pairs_of_nodes_sampled.size();

        // Connected closeness
	    double C = E_percent - p_percent;

        // Probability that, considering two nodes closer than Delta, they are connected
        // note: p is sampling-dependent, so we have to normalize it here.
        double possible_edges_per_pair = g.isUndirected() ? 1 : 2;
	    double P_edge = E / (possible_edges_per_pair * p * (g.getNodeCount() * (g.getNodeCount()-1)) / pairs_of_nodes_sampled.size());

        return new IndicatorResults(Delta, E_percent, p_percent, P_edge, C);
    }

    static class IndicatorResults {
        double Delta;
        double E_percent;
        double p_percent;
        double P_edge;
        double C;

        public IndicatorResults(double Delta, double E_percent, double p_percent, double P_edge, double C) {
            this.Delta = Delta;
            this.E_percent = E_percent;
            this.p_percent = p_percent;
            this.P_edge = P_edge;
            this.C = C;
        }
    }

    public ArrayList<Double> sample_pairs_of_nodes_distances(Graph g) {
        ArrayList<Double> samples = new ArrayList<>();

        if (g.getNodeCount()<2) {
            return samples;
        }
        final int samples_count = g.getEdgeCount(); // We want as many samples as edges
        if (samples_count<1) {
            return samples;
        }
        Node[] nodes = g.getNodes().toArray();
        Random random = new Random();
        for (int i=0; i<samples_count; i++) {
            Node n1 = nodes[random.nextInt(nodes.length)]; // node1 at random
            Node n2;
            do {
                n2 = nodes[random.nextInt(nodes.length)]; // node2 at random but different from node1
            } while (n1 == n2);
            double d = Math.sqrt(Math.pow(n1.x()-n2.x(), 2)+Math.pow(n1.y()-n2.y(), 2));
            samples.add(d);
        }
        return samples;
    }

    /**
     * @return
     */
    @Override
    public String getReport() {
        String report = "";

        // TODO: generate report

        report = "<HTML> <BODY> <h1>Connected-closeness Report </h1> "
            + "<hr>"
            + "<br> <h2> WORK IN PROGRESS </h2>"
            + "</BODY></HTML>";
        return report;
    }

    /**
     * @return
     */
    @Override
    public boolean cancel() {
        this.isCanceled = true;
        return true;
    }

    /**
     * @param progressTicket
     */
    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }
}
