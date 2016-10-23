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
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.project.impl.ProjectControllerImpl;
import org.openide.util.Lookup;
import static org.testng.Assert.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Anna
 */
public class DegreeNGTest {

    private ProjectController pc;

    @BeforeClass
    public void setUp() {
        pc = Lookup.getDefault().lookup(ProjectControllerImpl.class);
    }

    @BeforeMethod
    public void initialize() {
        pc.newProject();
    }

    @AfterMethod
    public void clean() {
        pc.closeCurrentProject();
    }

    @Test
    public void testOneNodeDegree() {
        GraphModel graphModel = GraphGenerator.generateNullUndirectedGraph(1);
        Graph graph = graphModel.getGraph();
        Node n = graph.getNode("0");

        Degree d = new Degree();
        int degree = d.calculateDegree(graph, n);
        assertEquals(degree, 0);
    }

    @Test
    public void testNullGraphDegree() {
        GraphModel graphModel = GraphGenerator.generateNullUndirectedGraph(5);
        Graph graph = graphModel.getGraph();
        Node n = graph.getNode("1");
        Degree d = new Degree();
        int degree = d.calculateDegree(graph, n);
        double avDegree = d.calculateAverageDegree(graph, false, false);
        assertEquals(degree, 0);
        assertEquals(avDegree, 0.0);
    }

    @Test
    public void testCompleteGraphDegree() {
        GraphModel graphModel = GraphGenerator.generateCompleteUndirectedGraph(5);
        Graph graph = graphModel.getGraph();
        Node n = graph.getNode("2");
        Degree d = new Degree();
        int degree = d.calculateDegree(graph, n);
        assertEquals(degree, 4);
    }

    @Test
    public void testSelfLoopGraphDegree() {
        GraphModel graphModel = GraphGenerator.generateSelfLoopUndirectedGraph(1);
        Graph graph = graphModel.getGraph();
        Node n = graph.getNode("0");
        Degree d = new Degree();
        int degree = d.calculateDegree(graph, n);
        assertEquals(degree, 2);
    }

    @Test
    public void testSelfLoopDirectedGraphDegree() {
        GraphModel graphModel = GraphGenerator.generateSelfLoopDirectedGraph(1);
        DirectedGraph graph = graphModel.getDirectedGraph();
        Node n = graph.getNode("0");
        Degree d = new Degree();
        assertEquals(d.calculateDegree(graph, n), 2);
        assertEquals(d.calculateInDegree(graph, n), 1);
        assertEquals(d.calculateOutDegree(graph, n), 1);
    }

    @Test
    public void testStarGraphDegree() {
        GraphModel graphModel = GraphGenerator.generateStarUndirectedGraph(5);
        Graph graph = graphModel.getGraph();
        Node n1 = graph.getNode("0");
        Node n2 = graph.getNode("1");
        Degree d = new Degree();
        int degree1 = d.calculateDegree(graph, n1);
        int degree2 = d.calculateDegree(graph, n2);
        double avDegree = d.calculateAverageDegree(graph, false, false);
        double expectedAvDegree = 1.6667;
        double diff = Math.abs(avDegree - expectedAvDegree);
        assertEquals(degree1, 5);
        assertEquals(degree2, 1);
        assertTrue(diff < 0.001);
    }

    @Test
    public void testCyclicGraphDegree() {
        GraphModel graphModel = GraphGenerator.generateCyclicUndirectedGraph(5);
        Graph graph = graphModel.getGraph();
        Node n = graph.getNode("3");
        Degree d = new Degree();
        int degree = d.calculateDegree(graph, n);
        double avDegree = d.calculateAverageDegree(graph, false, false);
        assertEquals(degree, 2);
        assertEquals(avDegree, 2.0);
    }

    @Test
    public void testDirectedPathGraphDegree() {
        GraphModel graphModel = GraphGenerator.generatePathDirectedGraph(2);
        DirectedGraph graph = graphModel.getDirectedGraph();
        Node n1 = graph.getNode("0");
        Node n2 = graph.getNode("1");
        Degree d = new Degree();
        int inDegree1 = d.calculateInDegree(graph, n1);
        int inDegree2 = d.calculateInDegree(graph, n2);
        int outDegree1 = d.calculateOutDegree(graph, n1);
        double avDegree = d.calculateAverageDegree(graph, true, false);
        assertEquals(inDegree1, 0);
        assertEquals(inDegree2, 1);
        assertEquals(outDegree1, 1);
        assertEquals(avDegree, 0.5);
    }

    @Test
    public void testDirectedCyclicGraphDegree() {
        GraphModel graphModel = GraphGenerator.generateCyclicDirectedGraph(5);
        DirectedGraph graph = graphModel.getDirectedGraph();
        Node n1 = graph.getNode("0");
        Node n3 = graph.getNode("2");
        Node n5 = graph.getNode("4");
        Degree d = new Degree();
        int inDegree3 = d.calculateInDegree(graph, n3);
        int degree1 = d.calculateDegree(graph, n1);
        int outDegree5 = d.calculateOutDegree(graph, n5);
        double avDegree = d.calculateAverageDegree(graph, true, false);
        assertEquals(inDegree3, 1);
        assertEquals(degree1, 2);
        assertEquals(outDegree5, 1);
        assertEquals(avDegree, 1.0);
    }

    @Test
    public void testDirectedStarOutGraphDegree() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();

        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        Node firstNode = graphModel.factory().newNode("0");
        directedGraph.addNode(firstNode);
        for (int i = 1; i <= 5; i++) {
            Node currentNode = graphModel.factory().newNode(((Integer) i).toString());
            directedGraph.addNode(currentNode);
            Edge currentEdge = graphModel.factory().newEdge(firstNode, currentNode);
            directedGraph.addEdge(currentEdge);
        }

        DirectedGraph graph = graphModel.getDirectedGraph();
        Node n1 = graph.getNode("0");
        Node n3 = graph.getNode("2");
        Degree d = new Degree();
        int inDegree1 = d.calculateInDegree(graph, n1);
        int outDegree1 = d.calculateOutDegree(graph, n1);
        int degree3 = d.calculateDegree(graph, n3);

        assertEquals(inDegree1, 0);
        assertEquals(outDegree1, 5);
        assertEquals(degree3, 1);
    }
}
