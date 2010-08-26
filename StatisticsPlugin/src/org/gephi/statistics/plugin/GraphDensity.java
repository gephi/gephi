/*
Copyright 2008-2010 Gephi
Authors : Patick J. McSweeney <pjmcswee@syr.edu>
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

import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.statistics.spi.Statistics;

/**
 *
 * @author pjmcswee
 */
public class GraphDensity implements Statistics {

    /** The density of the graph.*/
    private double mDensity;
    /** */
    private boolean mDirected;

    public void setDirected(boolean pDirected) {
        mDirected = pDirected;
    }

    public boolean getDirected() {
        return mDirected;
    }

    public double getDensity() {
        return mDensity;
    }

    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        Graph graph;

        if (mDirected) {
            graph = graphModel.getDirectedGraphVisible();
        } else {
            graph = graphModel.getUndirectedGraphVisible();
        }
        execute(graph, attributeModel);
    }

    public void execute(Graph graph, AttributeModel attributeModel) {
        double edgesCount = graph.getEdgeCount();
        double nodesCount = graph.getNodeCount();
        double multiplier = 1;

        if (!mDirected) {
            multiplier = 2;
        }
        mDensity = (multiplier * edgesCount) / (nodesCount * nodesCount - nodesCount);
    }

    /**
     *
     * @return
     */
    public String getReport() {
        String report = "<HTML> <BODY> <h1>Graph Density  Report </h1> "
                + "<hr>"
                + "<br>"
                + "<h2> Parameters: </h2>"
                + "Network Interpretation:  " + (this.mDirected ? "directed" : "undirected") + "<br>"
                + "<br> <h2> Results: </h2>"
                + "Density: " + mDensity
                + "</BODY></HTML>";
        return report;
    }
}
