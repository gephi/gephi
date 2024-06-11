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
import java.util.Random;
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

        final ArrayList<Double> pairs_of_nodes_sampled = sample_pairs_of_nodes_distances(g);

        // TODO: everything else

        try {
            // graph.setAttribute(AVERAGE_DEGREE, avgDegree);
        } finally {
            g.readUnlockAll();
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
