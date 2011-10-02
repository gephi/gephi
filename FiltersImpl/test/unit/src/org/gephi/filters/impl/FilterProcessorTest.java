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
package org.gephi.filters.impl;

import java.util.ArrayList;
import java.util.List;
import org.gephi.filters.AbstractQueryImpl;
import org.gephi.filters.FilterProcessor;
import org.gephi.filters.FilterQueryImpl;
import org.gephi.filters.OperatorQueryImpl;
import org.gephi.filters.api.Query;
import org.gephi.filters.spi.EdgeFilter;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.filters.spi.NodeFilter;
import org.gephi.filters.spi.Operator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class FilterProcessorTest {

    private GraphModel graphModel;
    private Graph rootGraph;
    private Query simpleQuery;
    private Query chainQuery;
    private Query complexQueryUnion;
    private Query veryComplexQueryInter;

    @Before
    public void setUp() {
        //Graph
        ProjectController pj = Lookup.getDefault().lookup(ProjectController.class);
        pj.newProject();
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        graphModel = gc.getModel();
        GraphFactory factory = graphModel.factory();
        Graph graph = gc.getModel().getUndirectedGraph();
        rootGraph = graph;

        //Add 8 nodes
        for (int i = 0; i < 8; i++) {
            Node node = factory.newNode();
            graph.addNode(node);
        }

        //Add edges
        graph.addEdge(factory.newEdge("0-1", graph.getNode(0), graph.getNode(1), 1f, false));
        graph.addEdge(factory.newEdge("1-3",graph.getNode(1), graph.getNode(3), 1f, false));
        graph.addEdge(factory.newEdge("3-2",graph.getNode(3), graph.getNode(2), 1f, false));
        graph.addEdge(factory.newEdge("2-0",graph.getNode(2), graph.getNode(0), 1f, false));
        graph.addEdge(factory.newEdge("4-5",graph.getNode(4), graph.getNode(5), 2f, false));
        graph.addEdge(factory.newEdge("5-7",graph.getNode(5), graph.getNode(7), 2f, false));
        graph.addEdge(factory.newEdge("7-6",graph.getNode(7), graph.getNode(6), 2f, false));
        graph.addEdge(factory.newEdge("6-4",graph.getNode(6), graph.getNode(4), 2f, false));
        graph.addEdge(factory.newEdge("3-4",graph.getNode(3), graph.getNode(4), 5f, false));

        //Query
        NodeDegreeFilter nodeDegreeFilter = new NodeDegreeFilter(3);
        simpleQuery = new FilterQueryImpl(nodeDegreeFilter);

        nodeDegreeFilter = new NodeDegreeFilter(1);
        chainQuery = new FilterQueryImpl(nodeDegreeFilter);
        ((FilterQueryImpl) chainQuery).addSubQuery(new FilterQueryImpl(new EdgeWeightFilter(1)));

        complexQueryUnion = new OperatorQueryImpl(new UnionOperator());
        ((OperatorQueryImpl) complexQueryUnion).addSubQuery(new FilterQueryImpl(new NodeIdFilter(1)));
        ((OperatorQueryImpl) complexQueryUnion).addSubQuery(new FilterQueryImpl(new NodeIdFilter(3)));

        veryComplexQueryInter = new FilterQueryImpl(new EdgeWeightFilter(0));
        OperatorQueryImpl q1 = new OperatorQueryImpl(new UnionOperator());
        ((FilterQueryImpl) veryComplexQueryInter).addSubQuery(q1);
        q1.addSubQuery(new FilterQueryImpl(new NodeIdFilter(0)));
        OperatorQueryImpl q2 = new OperatorQueryImpl(new UnionOperator());
        q2.addSubQuery(new FilterQueryImpl(new NodeIdFilter(1)));
        q2.addSubQuery(new FilterQueryImpl(new NodeIdFilter(2)));
        q1.addSubQuery(q2);
        FilterQueryImpl q3 = new FilterQueryImpl(new NodeDegreeFilter(2));
        q3.addSubQuery(new FilterQueryImpl(new EdgeWeightFilter(1)));
        q1.addSubQuery(q3);
    }

    @After
    public void tearDown() {
        rootGraph = null;
        simpleQuery = null;
        chainQuery = null;
        veryComplexQueryInter = null;
        complexQueryUnion = null;
    }

    @Test
    public void testProcess() {
        FilterProcessor filterProcessor = new FilterProcessor();
        printGraph(rootGraph);
        Graph result = filterProcessor.process((AbstractQueryImpl) veryComplexQueryInter, graphModel);
        printGraph(result);
//        printGraph(rootGraph);
//        rootGraph.removeNode(rootGraph.getNode(0));
//        printGraph(rootGraph);
    }

    private void printGraph(Graph graph) {
        Node[] nodes = graph.getNodes().toArray();
        Edge[] edges = graph.getEdges().toArray();
        System.out.println("--- Graph");
        System.out.println("--- Nodes: " + nodes.length);
        System.out.println("--- Edges: " + edges.length);
        for (Node n : nodes) {
            System.out.println("" + n.getId());
        }
        System.out.println("---------------");
        for (Edge e : edges) {
            System.out.println(e.getSource().getId() + "-" + e.getTarget().getId());
        }
        System.out.println("---------------");
        System.out.flush();
    }

    private static class EdgeWeightFilter implements EdgeFilter {

        private final float threshold;

        public EdgeWeightFilter(float threshold) {
            this.threshold = threshold;
        }

        public boolean evaluate(Graph graph, Edge edge) {
            return edge.getWeight() > threshold;
        }

        public String getName() {
            return "EdgeWeightFilter";
        }

        public FilterProperty[] getProperties() {
            return null;
        }

        public boolean init(Graph graph) {
            return true;
        }

        public void finish() {
        }
    }

    private static class NodeDegreeFilter implements NodeFilter {

        private final float threshold;

        public NodeDegreeFilter(float threshold) {
            this.threshold = threshold;
        }

        public boolean evaluate(Graph graph, Node node) {
            int degree = graph.getDegree(node);
            return degree > threshold;
        }

        public String getName() {
            return "NodeDegreeFilter";
        }

        public FilterProperty[] getProperties() {
            return null;
        }

        public boolean init(Graph graph) {
            return true;
        }

        public void finish() {
        }
    }

    private static class NodeIdFilter implements NodeFilter {

        private final int id;

        public NodeIdFilter(int id) {
            this.id = id;
        }

        public boolean evaluate(Graph graph, Node node) {
            return node.getId() == id;
        }

        public String getName() {
            return "NodeIdFilter " + id;
        }

        public FilterProperty[] getProperties() {
            return null;
        }

        public boolean init(Graph graph) {
            return true;
        }

        public void finish() {
        }
    }

    private static class UnionOperator implements Operator {

        public int getInputCount() {
            return 2;
        }

        public String getName() {
            return "UNION";
        }

        public FilterProperty[] getProperties() {
            return null;
        }

        public Graph filter(Graph[] graphs) {
            Graph maxGraph = graphs[0];
            int maxElements = 0;
            for (int i = 0; i < graphs.length; i++) {
                int count = graphs[i].getNodeCount();
                if (count > maxElements) {
                    maxGraph = graphs[i];
                    maxElements = count;
                }
            }
            for (int i = 0; i < graphs.length; i++) {
                if (graphs[i] != maxGraph) {
                    //Merge
                    for (Node n : graphs[i].getNodes().toArray()) {
                        maxGraph.addNode(n);
                    }
                    for (Edge e : graphs[i].getEdges().toArray()) {
                        maxGraph.addEdge(e);
                    }
                }
            }
            return maxGraph;
        }

        public Graph filter(Graph graph, Filter[] filters) {
            List<NodeFilter> nodeFilters = new ArrayList<NodeFilter>();
            List<EdgeFilter> edgeFilters = new ArrayList<EdgeFilter>();
            for (Filter f : filters) {
                if (f instanceof NodeFilter) {
                    nodeFilters.add((NodeFilter) f);
                } else if (f instanceof EdgeFilter) {
                    edgeFilters.add((EdgeFilter) f);
                }
            }
            if (nodeFilters.size() > 0) {
                List<Node> nodesToRemove = new ArrayList<Node>();
                for (Node n : graph.getNodes()) {
                    boolean remove = true;
                    for (NodeFilter nf : nodeFilters) {
                        if (nf.evaluate(graph, n)) {
                            remove = false;
                        }
                    }
                    if (remove) {
                        nodesToRemove.add(n);
                    }
                }

                for (Node n : nodesToRemove) {
                    graph.removeNode(n);
                }
            }
            if (edgeFilters.size() > 0) {
                List<Edge> edgesToRemove = new ArrayList<Edge>();
                for (Edge e : graph.getEdges()) {
                    boolean remove = true;
                    for (EdgeFilter nf : edgeFilters) {
                        if (nf.evaluate(graph, e)) {
                            remove = false;
                        }
                    }
                    if (remove) {
                        edgesToRemove.add(e);
                    }
                }

                for (Edge e : edgesToRemove) {
                    graph.removeEdge(e);
                }
            }
            return graph;
        }
    }
}
