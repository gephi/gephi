/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke, Sebastien Heymann
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

import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.statistics.api.Statistics;

/**
 *
 * @author Sebastien Heymann
 */
public class NodeCount implements Statistics {

    /** */
    private int nodeCount;
    /** */
    private String mGraphRevision;

    public int getNodeCount() {
        return nodeCount;
    }

    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        Graph graph = graphModel.getGraph();
        mGraphRevision = "(" + graph.getNodeVersion() + ", " + graph.getEdgeVersion() + ")";
        nodeCount = graph.getNodeCount();
    }

    public String getReport() {
        String report = new String("<HTML> <BODY> <h1>Graph Density  Report </h1> "
                + "<hr> <br> <h2>Network Revision Number:</h2>"
                + mGraphRevision
                + "<br> <h2> Results: </h2>"
                + "Number of nodes: " + nodeCount);
        return report;
    }

}
