/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.statistics.plugin;

import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.openide.util.Lookup;

/**
 *
 * @author mbastian
 */
public class GraphGenerator {

    public static GraphModel generateNullUndirectedGraph(int n) {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        for (int i = 0; i < n; i++) {
            Node currentNode = graphModel.factory().newNode(((Integer) i).toString());
            undirectedGraph.addNode(currentNode);
        }
        return graphModel;
    }

    public static GraphModel generateCompleteUndirectedGraph(int n) {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        Node[] nodes = new Node[n];
        for (int i = 0; i < n; i++) {
            Node currentNode = graphModel.factory().newNode(((Integer) i).toString());
            nodes[i] = currentNode;
            undirectedGraph.addNode(currentNode);
        }
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                Edge currentEdge = graphModel.factory().newEdge(nodes[i], nodes[j], false);
                undirectedGraph.addEdge(currentEdge);
            }
        }
        return graphModel;
    }

    public static GraphModel generatePathUndirectedGraph(int n) {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        if (n <= 0) {
            return graphModel;
        }
        Node firstNode = graphModel.factory().newNode("0");
        undirectedGraph.addNode(firstNode);
        Node prevNode = firstNode;
        for (int i = 1; i < n; i++) {
            Node currentNode = graphModel.factory().newNode(((Integer) i).toString());
            undirectedGraph.addNode(currentNode);
            Edge currentEdge = graphModel.factory().newEdge(prevNode, currentNode, false);
            undirectedGraph.addEdge(currentEdge);
            prevNode = currentNode;
        }
        return graphModel;
    }

    public static GraphModel generateCyclicUndirectedGraph(int n) {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        if (n <= 0) {
            return graphModel;
        }
        Node firstNode = graphModel.factory().newNode("0");
        undirectedGraph.addNode(firstNode);
        Node prevNode = firstNode;
        for (int i = 1; i < n; i++) {
            Node currentNode = graphModel.factory().newNode(((Integer) i).toString());
            undirectedGraph.addNode(currentNode);
            Edge currentEdge = graphModel.factory().newEdge(prevNode, currentNode, false);
            undirectedGraph.addEdge(currentEdge);
            prevNode = currentNode;
        }
        Edge currentEdge = graphModel.factory().newEdge(prevNode, firstNode, false);
        undirectedGraph.addEdge(currentEdge);
        return graphModel;
    }

    //generates graph from n+1 nodes
    public static GraphModel generateStarUndirectedGraph(int n) {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        Node firstNode = graphModel.factory().newNode("0");
        undirectedGraph.addNode(firstNode);
        for (int i = 1; i <= n; i++) {
            Node currentNode = graphModel.factory().newNode(((Integer) i).toString());
            undirectedGraph.addNode(currentNode);
            Edge currentEdge = graphModel.factory().newEdge(firstNode, currentNode, false);
            undirectedGraph.addEdge(currentEdge);
        }
        return graphModel;
    }

    public static GraphModel generateNullDirectedGraph(int n) {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        for (int i = 0; i < n; i++) {
            Node currentNode = graphModel.factory().newNode(((Integer) i).toString());
            directedGraph.addNode(currentNode);
        }
        return graphModel;
    }

    public static GraphModel generateCompleteDirectedGraph(int n) {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        Node[] nodes = new Node[n];
        for (int i = 0; i < n; i++) {
            Node currentNode = graphModel.factory().newNode(((Integer) i).toString());
            nodes[i] = currentNode;
            directedGraph.addNode(currentNode);
        }
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                Edge currentEdge = graphModel.factory().newEdge(nodes[i], nodes[j]);
                directedGraph.addEdge(currentEdge);
                currentEdge = graphModel.factory().newEdge(nodes[j], nodes[i]);
                directedGraph.addEdge(currentEdge);
            }
        }
        return graphModel;
    }

    public static GraphModel generatePathDirectedGraph(int n) {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        if (n <= 0) {
            return graphModel;
        }
        Node firstNode = graphModel.factory().newNode("0");
        directedGraph.addNode(firstNode);
        Node prevNode = firstNode;
        for (int i = 1; i < n; i++) {
            Node currentNode = graphModel.factory().newNode(((Integer) i).toString());
            directedGraph.addNode(currentNode);
            Edge currentEdge = graphModel.factory().newEdge(prevNode, currentNode);
            directedGraph.addEdge(currentEdge);
            prevNode = currentNode;
        }
        return graphModel;
    }

    public static GraphModel generateCyclicDirectedGraph(int n) {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        if (n <= 0) {
            return graphModel;
        }
        Node firstNode = graphModel.factory().newNode("0");
        directedGraph.addNode(firstNode);
        Node prevNode = firstNode;
        for (int i = 1; i < n; i++) {
            Node currentNode = graphModel.factory().newNode(((Integer) i).toString());
            directedGraph.addNode(currentNode);
            Edge currentEdge = graphModel.factory().newEdge(prevNode, currentNode);
            directedGraph.addEdge(currentEdge);
            prevNode = currentNode;
        }
        Edge currentEdge = graphModel.factory().newEdge(prevNode, firstNode);
        directedGraph.addEdge(currentEdge);
        return graphModel;
    }
}
