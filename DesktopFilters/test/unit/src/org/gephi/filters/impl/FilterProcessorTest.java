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
package org.gephi.filters.impl;

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
        GraphFactory factory = gc.getModel().factory();
        Graph graph = gc.getModel().getUndirectedGraph();
        rootGraph = graph;

        //Add 8 nodes
        for (int i = 0; i < 8; i++) {
            Node node = factory.newNode();
            graph.addNode(node);
        }

        //Add edges
        graph.addEdge(factory.newEdge(graph.getNode(0), graph.getNode(1), 1f, false));
        graph.addEdge(factory.newEdge(graph.getNode(1), graph.getNode(3), 1f, false));
        graph.addEdge(factory.newEdge(graph.getNode(3), graph.getNode(2), 1f, false));
        graph.addEdge(factory.newEdge(graph.getNode(2), graph.getNode(0), 1f, false));
        graph.addEdge(factory.newEdge(graph.getNode(4), graph.getNode(5), 1f, false));
        graph.addEdge(factory.newEdge(graph.getNode(5), graph.getNode(7), 1f, false));
        graph.addEdge(factory.newEdge(graph.getNode(7), graph.getNode(6), 1f, false));
        graph.addEdge(factory.newEdge(graph.getNode(6), graph.getNode(4), 1f, false));
        graph.addEdge(factory.newEdge(graph.getNode(3), graph.getNode(4), 5f, false));

        //Query
        NodeDegreeFilter nodeDegreeFilter = new NodeDegreeFilter();
        simpleQuery = new FilterQueryImpl(nodeDegreeFilter);

        EdgeWeightFilter edgeWeightFilter = new EdgeWeightFilter();
        chainQuery = new FilterQueryImpl(edgeWeightFilter);
        ((FilterQueryImpl) chainQuery).addSubQuery(new FilterQueryImpl(new NodeDegreeFilter()));

        complexQueryUnion = new OperatorQueryImpl(new UnionOperator());
        ((OperatorQueryImpl)complexQueryUnion).addSubQuery(new FilterQueryImpl(new NodeIdFilter(1)));
        ((OperatorQueryImpl)complexQueryUnion).addSubQuery(new FilterQueryImpl(new NodeIdFilter(5)));

        veryComplexQueryInter = new FilterQueryImpl(new EdgeWeightFilter());
        OperatorQueryImpl q1 = new OperatorQueryImpl(new UnionOperator());
        ((FilterQueryImpl)veryComplexQueryInter).addSubQuery(q1);
        q1.addSubQuery(new FilterQueryImpl(new NodeIdFilter(0)));
        OperatorQueryImpl q2 = new OperatorQueryImpl(new UnionOperator());
        q2.addSubQuery(new FilterQueryImpl(new NodeIdFilter(1)));
        q2.addSubQuery(new FilterQueryImpl(new NodeIdFilter(2)));
        q1.addSubQuery(q2);
        FilterQueryImpl q3 = new FilterQueryImpl(new NodeDegreeFilter());
        q3.addSubQuery(new FilterQueryImpl(new EdgeWeightFilter()));
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
        Graph result = filterProcessor.process((AbstractQueryImpl)simpleQuery, rootGraph);
        printGraph(result);
//        printGraph(rootGraph);
//        rootGraph.removeNode(rootGraph.getNode(0));
//        printGraph(rootGraph);
    }

    private void printGraph(Graph graph) {
        Node[] nodes = graph.getNodes().toArray();
        Edge[] edges = graph.getEdges().toArray();
        System.out.println("--- Graph");
        System.out.println("--- Nodes: "+nodes.length);
        System.out.println("--- Edges: "+edges.length);
        for(Node n : nodes) {
            System.out.println(""+n.getId());
        }
        System.out.println("---------------");
        for(Edge e : edges) {
            System.out.println(e.getSource().getId()+"-"+e.getTarget().getId());
        }
        System.out.println("---------------");
        System.out.flush();
    }

    private static class EdgeWeightFilter implements EdgeFilter {

        private final float threshold = 3;

        public boolean evaluate(Graph graph, Edge edge) {
            return edge.getWeight() > threshold;
        }

        public String getName() {
            return "EdgeWeightFilter";
        }

        public FilterProperty[] getProperties() {
            return null;
        }
    }

    private static class NodeDegreeFilter implements NodeFilter {

        private final float threshold = 3;

        public boolean evaluate(Graph graph, Node node) {
            int degree = graph.getDegree(node);
            return degree < threshold;
        }

        public String getName() {
            return "NodeDegreeFilter";
        }

        public FilterProperty[] getProperties() {
            return null;
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
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Graph filter(Graph graph, Filter[] filters) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
