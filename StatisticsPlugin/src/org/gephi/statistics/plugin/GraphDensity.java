/*
Copyright 2008-2010 Gephi
Authors : Patick J. McSweeney <pjmcswee@syr.edu>, Sebastien Heymann <seb@gephi.org>
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
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
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
        if (graphController != null && graphController.getModel() != null) {
            isDirected = graphController.getModel().isDirected();
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

    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        HierarchicalGraph hgraph;

        if (isDirected) {
            hgraph = graphModel.getHierarchicalDirectedGraphVisible();
        } else {
            hgraph = graphModel.getHierarchicalUndirectedGraphVisible();
        }

        double edgesCount = hgraph.getTotalEdgeCount();
        double nodesCount = hgraph.getNodeCount();
        double multiplier = 1;

        if (!isDirected) {
            multiplier = 2;
        }
        density = (multiplier * edgesCount) / (nodesCount * nodesCount - nodesCount);
    }

    /**
     *
     * @return
     */
    public String getReport() {
        return "<HTML> <BODY> <h1>Graph Density  Report </h1> "
                + "<hr>"
                + "<br>"
                + "<h2> Parameters: </h2>"
                + "Network Interpretation:  " + (isDirected ? "directed" : "undirected") + "<br>"
                + "<br> <h2> Results: </h2>"
                + "Density: " + density
                + "</BODY></HTML>";
    }
}
