/*
 Copyright 2008-2013 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2013 Gephi Consortium. All rights reserved.

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

 Portions Copyrighted 2013 Gephi Consortium.
 */

package org.gephi.appearance;

import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.junit.Assert;
import org.junit.Test;

public class AttributePartitionTest {

    private static void clearNodeAttributes(Graph graph) {
        for (Node n : graph.getNodes()) {
            n.clearAttributes();
        }
    }

    @Test
    public void testEmpty() {
        Graph graph = GraphGenerator.build().addIntNodeColumn().getGraph();
        Column column = graph.getModel().getNodeTable().getColumn(GraphGenerator.INT_COLUMN);
        AttributePartitionImpl attributePartition = new AttributePartitionImpl(column);

        Assert.assertEquals(0, attributePartition.getElementCount(graph));
        Assert.assertEquals(0, attributePartition.getValues(graph).size());
        Assert.assertEquals(0, attributePartition.getSortedValues(graph).size());
        Assert.assertEquals(0, attributePartition.size(graph));
    }

    @Test
    public void testIntColumn() {
        Graph graph = GraphGenerator.build().generateTinyGraph().addIntNodeColumn().getGraph();
        Column column = graph.getModel().getNodeTable().getColumn(GraphGenerator.INT_COLUMN);

        AttributePartitionImpl p = new AttributePartitionImpl(column);
        Assert.assertEquals(graph.getNodeCount(), p.getElementCount(graph));
        Assert.assertEquals(graph.getNodeCount(), p.getValues(graph).size());
        Assert.assertNotNull(p.getValue(graph.getNodes().toArray()[0], graph));
    }

    @Test
    public void testIsValidStringColumn() {
        Graph graph = GraphGenerator.build().generateTinyGraph().addStringNodeColumn().getGraph();
        Column column = graph.getModel().getNodeTable().getColumn(GraphGenerator.STRING_COLUMN);

        AttributePartitionImpl p = new AttributePartitionImpl(column);
        Assert.assertTrue(p.isValid(graph));

        clearNodeAttributes(graph);
        Assert.assertTrue(p.isValid(graph));
    }

    @Test
    public void testIsValidIntColumn() {
        Graph graph = GraphGenerator.build().generateTinyGraph().addIntNodeColumn().getGraph();
        Column column = graph.getModel().getNodeTable().getColumn(GraphGenerator.INT_COLUMN);

        AttributePartitionImpl p = new AttributePartitionImpl(column);
        Assert.assertTrue(p.isValid(graph));

        clearNodeAttributes(graph);
        Assert.assertFalse(p.isValid(graph));
    }

    @Test
    public void testVersion() {
        Graph graph = GraphGenerator.build().generateTinyGraph().addIntNodeColumn().getGraph();
        Column column = graph.getModel().getNodeTable().getColumn(GraphGenerator.INT_COLUMN);
        Node n1 = graph.getNode(GraphGenerator.FIRST_NODE);

        AttributePartitionImpl p = new AttributePartitionImpl(column);
        int version = p.getVersion(graph);
        n1.setAttribute(column, 99);
        Assert.assertNotEquals(version, version = p.getVersion(graph));
        Assert.assertEquals(version, p.getVersion(graph));

        graph.removeNode(n1);
        Assert.assertNotEquals(version, p.getVersion(graph));
    }
}
