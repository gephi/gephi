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

package org.gephi.layout.plugin.forceAtlas2;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.junit.Assert;
import org.junit.Test;

public class ForceAtlas2Test {

    @Test
    public void testAdjustSizesDoesNotProduceNaNForNodeWithZeroNetDisplacement() {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        UndirectedGraph graph = graphModel.getUndirectedGraph();

        Node nodeA = graphModel.factory().newNode("A");
        nodeA.setX(10f);
        nodeA.setY(0f);
        nodeA.setSize(1f);
        graph.addNode(nodeA);

        Node nodeB = graphModel.factory().newNode("B");
        nodeB.setX(-10f);
        nodeB.setY(0f);
        nodeB.setSize(1f);
        graph.addNode(nodeB);

        // Isolated node sitting exactly at the origin: repulsion from A and B
        // cancels out by symmetry and gravity is zero at the origin, so its
        // net displacement (dx, dy) is exactly (0, 0).
        Node nodeC = graphModel.factory().newNode("C");
        nodeC.setX(0f);
        nodeC.setY(0f);
        nodeC.setSize(1f);
        graph.addNode(nodeC);

        Edge edge = graphModel.factory().newEdge(nodeA, nodeB, false);
        graph.addEdge(edge);

        ForceAtlas2 layout = new ForceAtlas2(new ForceAtlas2Builder());
        layout.setGraphModel(graphModel);
        layout.resetPropertiesValues();
        layout.setAdjustSizes(true);
        layout.setBarnesHutOptimize(false);
        layout.setThreadsCount(1);
        layout.initAlgo();
        try {
            layout.goAlgo();
        } finally {
            layout.endAlgo();
        }

        Assert.assertFalse("Node C x should not be NaN", Float.isNaN(nodeC.x()));
        Assert.assertFalse("Node C y should not be NaN", Float.isNaN(nodeC.y()));
    }
}
