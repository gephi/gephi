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

package org.gephi.layout.plugin;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.layout.plugin.forceAtlas.ForceAtlas;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder;
import org.gephi.layout.plugin.fruchterman.FruchtermanReingoldBuilder;
import org.gephi.layout.plugin.labelAdjust.LabelAdjustBuilder;
import org.gephi.layout.plugin.rotate.Rotate;
import org.gephi.layout.plugin.scale.Contract;
import org.gephi.layout.spi.Layout;
import org.junit.Assert;
import org.junit.Test;

public class NonFiniteCoordinatesTest {

    private static final int ITERATIONS = 10;

    private GraphModel graphModel;

    @Test
    public void testForceAtlasWithHugeCoordinate() {
        Node[] nodes = createGraph(new float[] {10f, 0f}, new float[] {-10f, 0f}, new float[] {0f, 1e20f});
        runLayout(new ForceAtlas().buildLayout());
        assertFinite(nodes);
    }

    @Test
    public void testForceAtlasWithInfiniteCoordinate() {
        Node[] nodes = createGraphWithInfiniteNode();
        runLayout(new ForceAtlas().buildLayout());
        assertMoved(nodes[0], 10f, 0f);
        assertMoved(nodes[1], -10f, 0f);
        assertFinite(nodes);
    }

    @Test
    public void testFruchtermanReingoldWithHugeCoordinate() {
        Node[] nodes = createGraph(new float[] {10f, 0f}, new float[] {-10f, 0f}, new float[] {0f, 1e19f});
        runLayout(new FruchtermanReingoldBuilder().buildLayout());
        assertFinite(nodes);
    }

    @Test
    public void testFruchtermanReingoldWithInfiniteCoordinate() {
        Node[] nodes = createGraphWithInfiniteNode();
        runLayout(new FruchtermanReingoldBuilder().buildLayout());
        assertMoved(nodes[0], 10f, 0f);
        assertMoved(nodes[1], -10f, 0f);
        assertFinite(nodes);
    }

    @Test
    public void testContractWithHugeCoordinates() {
        Node[] nodes = createGraph(new float[] {3e38f, 0f}, new float[] {3e38f, 10f}, new float[] {0f, 0f});
        runLayout(new Contract().buildLayout());
        assertFinite(nodes);
    }

    @Test
    public void testContractWithInfiniteCoordinate() {
        Node[] nodes = createGraphWithInfiniteNode();
        runLayout(new Contract().buildLayout());
        Assert.assertEquals("Distance between finite nodes should be scaled by 0.8", 16f,
            ForceVectorUtils.distance(nodes[0], nodes[1]), 1e-3f);
        assertFinite(nodes);
    }

    @Test
    public void testForceAtlas2WithHugeCoordinate() {
        Node[] nodes = createGraph(new float[] {10f, 0f}, new float[] {-10f, 0f}, new float[] {0f, 3e38f});
        runLayout(new ForceAtlas2Builder().buildLayout());
        assertFinite(nodes);
    }

    @Test
    public void testForceAtlas2WithInfiniteCoordinate() {
        Node[] nodes = createGraphWithInfiniteNode();
        runLayout(new ForceAtlas2Builder().buildLayout());
        assertMoved(nodes[0], 10f, 0f);
        assertMoved(nodes[1], -10f, 0f);
        assertFinite(nodes);
    }

    @Test
    public void testRotateWithInfiniteCoordinate() {
        Node[] nodes = createGraph(new float[] {10f, 0f}, new float[] {-10f, 0f},
            new float[] {Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY});
        runLayout(new Rotate().buildLayout());
        Assert.assertEquals(0f, nodes[0].x(), 1e-3f);
        Assert.assertEquals(-10f, nodes[0].y(), 1e-3f);
        Assert.assertEquals(0f, nodes[1].x(), 1e-3f);
        Assert.assertEquals(10f, nodes[1].y(), 1e-3f);
        assertFinite(nodes);
    }

    @Test
    public void testForceAtlas2WithNodesFartherApartThanFloatMax() {
        Node[] nodes = createGraph(new float[] {10f, 0f}, new float[] {-10f, 0f}, new float[] {0f, 2e38f},
            new float[] {0f, -2e38f});
        runLayout(new ForceAtlas2Builder().buildLayout());
        assertMoved(nodes[0], 10f, 0f);
        assertMoved(nodes[1], -10f, 0f);
        assertFinite(nodes);
    }

    @Test
    public void testLabelAdjustWithInfiniteCoordinate() {
        Node[] nodes = createGraphWithInfiniteNode();
        runLabelAdjust(nodes);
        assertPushedApart(nodes[0], nodes[1]);
        assertFinite(nodes);
    }

    @Test
    public void testLabelAdjustWithHugeCoordinate() {
        // Enough nodes for two quadtree columns, so that an uncapped cell count overflows int
        float[][] positions = new float[61][];
        positions[0] = new float[] {10f, 0f};
        positions[1] = new float[] {-10f, 0f};
        for (int i = 2; i < 60; i++) {
            positions[i] = new float[] {100f * i, 0f};
        }
        positions[60] = new float[] {0f, 1e30f};
        Node[] nodes = createGraph(positions);
        runLabelAdjust(nodes);
        assertPushedApart(nodes[0], nodes[1]);
        assertFinite(nodes);
    }

    @Test
    public void testLabelAdjustWithNodeFarBelow() {
        // The quadtree gets several rows and the overlapping nodes are in the top one
        Node[] nodes = createGraph(new float[] {10f, 0f}, new float[] {-10f, 0f}, new float[] {0f, -400f});
        runLabelAdjust(nodes);
        assertPushedApart(nodes[0], nodes[1]);
    }

    private Node[] createGraphWithInfiniteNode() {
        return createGraph(new float[] {10f, 0f}, new float[] {-10f, 0f}, new float[] {0f, Float.POSITIVE_INFINITY});
    }

    private Node[] createGraph(float[]... positions) {
        graphModel = GraphModel.Factory.newInstance();
        Graph graph = graphModel.getUndirectedGraph();
        Node[] nodes = new Node[positions.length];
        for (int i = 0; i < positions.length; i++) {
            nodes[i] = graphModel.factory().newNode(String.valueOf(i));
            nodes[i].setX(positions[i][0]);
            nodes[i].setY(positions[i][1]);
            graph.addNode(nodes[i]);
        }
        for (int i = 1; i < nodes.length; i++) {
            graph.addEdge(graphModel.factory().newEdge(nodes[0], nodes[i], false));
        }
        return nodes;
    }

    private void runLayout(Layout layout) {
        layout.setGraphModel(graphModel);
        layout.resetPropertiesValues();
        layout.initAlgo();
        try {
            for (int i = 0; i < ITERATIONS && layout.canAlgo(); i++) {
                layout.goAlgo();
            }
        } finally {
            layout.endAlgo();
        }
    }

    private void runLabelAdjust(Node[] nodes) {
        for (Node node : nodes) {
            node.setSize(10f);
            node.getTextProperties().setDimensions(30f, 10f);
        }
        Layout layout = new LabelAdjustBuilder().buildLayout();
        layout.setGraphModel(graphModel);
        layout.resetPropertiesValues();
        layout.initAlgo();
        try {
            // Checked before goAlgo, so that a non-finite position fails here rather than in the quadtree allocation
            assertFinite(nodes);
            for (int i = 0; i < ITERATIONS && layout.canAlgo(); i++) {
                layout.goAlgo();
            }
        } finally {
            layout.endAlgo();
        }
    }

    private static void assertFinite(Node... nodes) {
        for (Node node : nodes) {
            Assert.assertTrue("Node " + node.getId() + " x should be finite", Float.isFinite(node.x()));
            Assert.assertTrue("Node " + node.getId() + " y should be finite", Float.isFinite(node.y()));
        }
    }

    private static void assertMoved(Node node, float x, float y) {
        Assert.assertTrue("Node " + node.getId() + " should have moved", node.x() != x || node.y() != y);
    }

    private static void assertPushedApart(Node n1, Node n2) {
        Assert.assertTrue("Overlapping nodes should be pushed apart", ForceVectorUtils.distance(n1, n2) > 20f);
    }
}
