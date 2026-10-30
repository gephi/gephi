/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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

package org.gephi.statistics.plugin.dynamic;

import junit.framework.TestCase;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Subgraph;
import org.gephi.graph.api.types.TimestampIntegerMap;
import org.gephi.statistics.spi.DynamicStatistics;
import org.junit.Assert;
import org.junit.Test;

public class DynamicStatisticsTest extends TestCase {

    @Test
    public void testNbNodesSeriesOnMainGraphWhenFiltered() {
        GraphModel graphModel = filteredGraphModel();
        run(graphModel, new DynamicNbNodes());

        assertSeries(graphModel, DynamicNbNodes.NB_NODES, new int[] {2, 2, 1, 1});
    }

    @Test
    public void testNbEdgesSeriesOnMainGraphWhenFiltered() {
        GraphModel graphModel = filteredGraphModel();
        run(graphModel, new DynamicNbEdges());

        assertSeries(graphModel, DynamicNbEdges.NB_EDGES, new int[] {1, 1, 0, 0});
    }

    private static GraphModel filteredGraphModel() {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        DirectedGraph graph = graphModel.getDirectedGraph();
        Node n1 = graphModel.factory().newNode("1");
        Node n2 = graphModel.factory().newNode("2");
        Node n3 = graphModel.factory().newNode("3");
        graph.addNode(n1);
        graph.addNode(n2);
        graph.addNode(n3);
        Edge e12 = graphModel.factory().newEdge(n1, n2);
        graph.addEdge(e12);
        graph.addEdge(graphModel.factory().newEdge(n2, n3));

        //A filter's result without node 3 is the visible view
        GraphView filteredView = graphModel.createView();
        Subgraph filteredGraph = graphModel.getGraph(filteredView);
        filteredGraph.addNode(n1);
        filteredGraph.addNode(n2);
        filteredGraph.addEdge(e12);
        graphModel.setVisibleView(filteredView);
        return graphModel;
    }

    private static void run(GraphModel graphModel, DynamicStatistics statistics) {
        GraphView filteredView = graphModel.getVisibleView();
        Subgraph filteredGraph = graphModel.getGraph(filteredView);
        statistics.execute(graphModel);

        //The whole filtered graph in [0, 1], then one of its nodes in [2, 3]
        GraphView window = graphModel.createView();
        Subgraph windowGraph = graphModel.getGraph(window);
        for (Node node : filteredGraph.getNodes().toArray()) {
            windowGraph.addNode(node);
        }
        for (Edge edge : filteredGraph.getEdges().toArray()) {
            windowGraph.addEdge(edge);
        }
        statistics.loop(window, new Interval(0, 1));
        windowGraph.clear();
        windowGraph.addNode(graphModel.getGraph().getNode("1"));
        statistics.loop(window, new Interval(2, 3));
        statistics.end();

        //The filter is changed or removed
        graphModel.destroyView(window);
        graphModel.destroyView(filteredView);
    }

    private static void assertSeries(GraphModel graphModel, String key, int[] values) {
        Object series = graphModel.getGraph().getAttribute(key);
        Assert.assertTrue(series instanceof TimestampIntegerMap);
        Assert.assertArrayEquals(new double[] {0, 1, 2, 3}, ((TimestampIntegerMap) series).getTimestamps(), 0);
        Assert.assertArrayEquals(values, ((TimestampIntegerMap) series).toIntegerArray());
    }
}
