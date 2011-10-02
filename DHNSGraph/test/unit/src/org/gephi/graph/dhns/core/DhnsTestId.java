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
package org.gephi.graph.dhns.core;

import java.util.logging.Logger;
import org.gephi.graph.api.GraphView;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.project.api.ProjectController;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import static org.junit.Assert.*;

/**
 *
 * @author Mathieu Bastian
 */
public class DhnsTestId {

    private GraphModel graphModel;
    private Graph rootGraph;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        ProjectController pj = Lookup.getDefault().lookup(ProjectController.class);
        pj.newProject();
        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        ac.getModel();
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        graphModel = gc.getModel();
        Graph graph = gc.getModel().getUndirectedGraph();
        rootGraph = graph;
    }

    @After
    public void tearDown() {
        rootGraph = null;
        graphModel = null;
    }

    @Test
    public void testNodeDefaultId() {
        GraphFactory factory = graphModel.factory();

        Node n1 = factory.newNode();
        rootGraph.addNode(n1);

        String n1Id = "" + n1.getId();
        assertEquals(n1Id, n1.getNodeData().getId());
        assertEquals(n1, rootGraph.getNode(n1Id));
    }

    @Test
    public void testEdgeDefaultId() {
        GraphFactory factory = graphModel.factory();

        Node n1 = factory.newNode();
        Node n2 = factory.newNode();
        rootGraph.addNode(n1);
        rootGraph.addNode(n2);

        Edge e1 = factory.newEdge(n1, n2);
        rootGraph.addEdge(e1);

        String e1Id = "" + e1.getId();
        assertEquals(e1Id, e1.getEdgeData().getId());
        assertEquals(e1, rootGraph.getEdge(e1Id));
    }

    @Test
    public void testSetId() {
        GraphFactory factory = graphModel.factory();

        Node n1 = factory.newNode();
        Node n2 = factory.newNode();
        rootGraph.addNode(n1);
        rootGraph.addNode(n2);

        Edge e1 = factory.newEdge(n1, n2);
        rootGraph.addEdge(e1);

        rootGraph.setId(n1, "test");
        assertEquals(n1, rootGraph.getNode("test"));

        rootGraph.setId(e1, "edge");
        assertEquals(e1, rootGraph.getEdge("edge"));
    }

    @Test
    public void testNodeUserId() {
        GraphFactory factory = graphModel.factory();

        Node n1 = factory.newNode("test");
        rootGraph.addNode(n1);

        assertEquals(n1, rootGraph.getNode("test"));

        rootGraph.setId(n1, "test2");

        assertEquals(n1, rootGraph.getNode("test2"));
    }

    @Test
    public void testEdgeUserId() {
        GraphFactory factory = graphModel.factory();

        Node n1 = factory.newNode();
        Node n2 = factory.newNode();
        rootGraph.addNode(n1);
        rootGraph.addNode(n2);

        Edge e1 = factory.newEdge("test", n1, n2, 1f, false);
        rootGraph.addEdge(e1);

        assertEquals(e1, rootGraph.getEdge("test"));

        rootGraph.setId(e1, "test2");

        assertEquals(e1, rootGraph.getEdge("test2"));
    }

    @Test
    public void testViewNodes() {
        GraphFactory factory = graphModel.factory();
        Node n1 = factory.newNode("n1");
        Node n2 = factory.newNode("n2");
        rootGraph.addNode(n1);
        rootGraph.addNode(n2);

        GraphView newView = graphModel.newView();
        Graph graphNewView = graphModel.getGraph(newView);
        Node n1c = graphNewView.getNode("n1");
        assertNotSame(n1, n1c);
        assertSame(n1.getNodeData(), n1c.getNodeData());
        assertEquals(((AbstractNode) n1c).getViewId(), newView.getViewId());
        assertSame(n1c, graphNewView.getNode(n1.getId()));

        graphNewView.removeNode(n1c);
        assertNull(graphNewView.getNode("n1"));
        assertNotNull(rootGraph.getNode("n1"));

        rootGraph.removeNode(n1);
        assertNull(rootGraph.getNode("n1"));
    }

    @Test
    public void testViewEdges() {
        GraphFactory factory = graphModel.factory();
        Node n1 = factory.newNode("n1");
        Node n2 = factory.newNode("n2");
        rootGraph.addNode(n1);
        rootGraph.addNode(n2);
        Edge e1 = factory.newEdge("e1", n1, n2, 1f, false);
        rootGraph.addEdge(e1);

        GraphView newView = graphModel.newView();
        Graph graphNewView = graphModel.getGraph(newView);
        assertNotNull(graphNewView.getEdge(e1.getId()));
        assertNotNull(graphNewView.getEdge(e1.getEdgeData().getId()));
        assertSame(e1, graphNewView.getEdge(e1.getId()));

        graphNewView.removeEdge(graphNewView.getEdge(e1.getId()));
        assertSame(e1, rootGraph.getEdge(e1.getId()));

        newView = graphModel.newView();
        assertNotNull(rootGraph.getEdge(e1.getId()));
        ((Dhns) graphModel).destroyView(newView);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        rootGraph.removeEdge(e1);
        assertNull(rootGraph.getEdge(e1.getId()));

        rootGraph.addEdge(e1);
        newView = graphModel.newView();
        graphNewView = graphModel.getGraph(newView);
        Logger.getLogger("").info("clearedges");
        graphNewView.clearEdges();
        graphNewView = null;
        ((Dhns) graphModel).destroyView(newView);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Test
    public void testMetaEdgeId() {
        //TODO
    }
}
