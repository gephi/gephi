/*
Copyright 2008-2011 Gephi
Authors : Patick J. McSweeney <pjmcswee@syr.edu>, Sebastien Heymann <seb@gephi.org>
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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.statistics.spi.Statistics;
import org.openide.util.Lookup;

/**
 *
 * @author pjmcswee
 */
public class GraphDensity implements Statistics {

    /** The density of the graph.*/
    private double density;
    /** */
    private boolean isDirected;

    public GraphDensity() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if (graphController != null && graphController.getGraphModel()!= null) {
            isDirected = graphController.getGraphModel().isDirected();
        }
    }

    public void setDirected(boolean isDirected) {
        this.isDirected = isDirected;
    }

    public boolean getDirected() {
        return isDirected;
    }

    public double getDensity() {
        return density;
    }

    @Override
    public void execute(GraphModel graphModel) {
        Graph graph;
        if (isDirected) {
            graph = graphModel.getDirectedGraphVisible();
        } else {
            graph = graphModel.getUndirectedGraphVisible();
        }
        
        density = calculateDensity(graph, isDirected);
    }
    
    public double calculateDensity(Graph graph, boolean isGraphDirected) {
        double result;

        double edgesCount = graph.getEdgeCount();
        double nodesCount = graph.getNodeCount();
        double multiplier = 1;

        if (!isGraphDirected) {
            multiplier = 2;
        }
        result = (multiplier * edgesCount) / (nodesCount * nodesCount - nodesCount);
        return result;
    }

    /**
     *
     * @return
     */
    @Override
    public String getReport() {
        NumberFormat f = new DecimalFormat("#0.000");

        return "<HTML> <BODY> <h1>Graph Density  Report </h1> "
                + "<hr>"
                + "<br>"
                + "<h2> Parameters: </h2>"
                + "Network Interpretation:  " + (isDirected ? "directed" : "undirected") + "<br>"
                + "<br> <h2> Results: </h2>"
                + "Density: " + f.format(density)
                + "</BODY></HTML>";
    }
}
