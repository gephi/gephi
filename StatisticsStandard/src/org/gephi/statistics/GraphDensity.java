/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
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

import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.statistics.api.Statistics;
import org.gephi.statistics.ui.api.StatisticsUI;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class GraphDensity implements Statistics {

    /** The density of the graph.*/
    private float density;

    /**
     * 
     * @param graphController
     */
    public void execute(GraphController graphController) {
        DirectedGraph graph = graphController.getModel().getDirectedGraphVisible();
        int edgesCount = graph.getEdgeCount();
        int nodesCount = graph.getNodeCount();
        density = (float) edgesCount / (nodesCount * nodesCount - nodesCount);
    }

    /**
     * 
     * @return
     */
    public String toString() {
        return new String("Graph Density");
    }

    /**
     *
     * @return
     */
    public String getName() {
        return NbBundle.getMessage(GraphDensity.class, "GraphDensity_name");
    }

    /**
     *
     * @return
     */
    public boolean isParamerizable() {
        return false;
    }

    /**
     *
     * @return
     */
    public String getReport() {
        return new String("Density: " + density);
    }

    public StatisticsUI getUI() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
