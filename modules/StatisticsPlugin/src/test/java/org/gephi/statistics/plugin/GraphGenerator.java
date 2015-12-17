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
