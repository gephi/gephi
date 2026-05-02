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

package org.gephi.io.importer.impl;

import org.gephi.graph.api.TimeRepresentation;
import org.gephi.graph.api.types.TimestampStringMap;
import org.gephi.io.importer.api.ColumnDraft;
import org.gephi.io.importer.api.EdgeDirection;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.ElementIdType;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.NodeDraft;
import org.junit.Assert;
import org.junit.Test;

public class ImportContainerImplTest {

    @Test
    public void testAddColumn() {
        ImportContainerImpl importContainer = new ImportContainerImpl();
        ColumnDraft col = importContainer.addNodeColumn("foo", String.class);
        Assert.assertNotNull(col);
        Assert.assertEquals(String.class, col.getTypeClass());
        Assert.assertSame(col, importContainer.getNodeColumn("foo"));
    }

    @Test
    public void testAddDynamicColumn() {
        ImportContainerImpl importContainer = new ImportContainerImpl();
        importContainer.setTimeRepresentation(TimeRepresentation.TIMESTAMP);
        ColumnDraft col = importContainer.addNodeColumn("foo", String.class, true);
        Assert.assertNotNull(col);
        Assert.assertEquals(String.class, col.getTypeClass());
        Assert.assertEquals(TimestampStringMap.class, col.getResolvedTypeClass(importContainer));
    }

    @Test
    public void testEdgeExists() {
        ImportContainerImpl importContainer = new ImportContainerImpl();
        generateTinyGraph(importContainer);
        Assert.assertTrue(importContainer.edgeExists("1"));
        Assert.assertTrue(importContainer.edgeExists("1", "2"));
        Assert.assertTrue(importContainer.edgeExists("2", "1"));
    }

    @Test
    public void testEdgeExistsUndirectedWithDefault() {
        ImportContainerImpl importContainer = new ImportContainerImpl();
        importContainer.setEdgeDefault(EdgeDirectionDefault.UNDIRECTED);
        generateTinyUndirectedGraph(importContainer);
        Assert.assertTrue(importContainer.edgeExists("1"));
        Assert.assertTrue(importContainer.edgeExists("1", "2"));
        Assert.assertTrue(importContainer.edgeExists("2", "1"));
    }

    @Test
    public void testEdgeExistsUndirected() {
        ImportContainerImpl importContainer = new ImportContainerImpl();
        generateTinyUndirectedGraph(importContainer);
        Assert.assertTrue(importContainer.edgeExists("1"));
        Assert.assertTrue(importContainer.edgeExists("1", "2"));
        Assert.assertTrue(importContainer.edgeExists("2", "1"));
    }

    @Test
    public void testEdgeExistsSelfLoop() {
        ImportContainerImpl importContainer = new ImportContainerImpl();
        generateTinyGraphWithSelfLoop(importContainer, EdgeDirection.DIRECTED);
        Assert.assertTrue(importContainer.edgeExists("1", "1"));

        importContainer = new ImportContainerImpl();
        generateTinyGraphWithSelfLoop(importContainer, EdgeDirection.UNDIRECTED);
        Assert.assertTrue(importContainer.edgeExists("1", "1"));
    }

    @Test
    public void testRemoveEdge() {
        ImportContainerImpl importContainer = new ImportContainerImpl();
        generateTinyGraph(importContainer);
        importContainer.removeEdge(importContainer.getEdge("1"));

        Assert.assertTrue(importContainer.verify());
        Assert.assertEquals(1, importContainer.getUnloader().getEdgeCount());
    }

    // Bug fix: removeEdge was leaving a stale empty int[] in edgeTypeSets, blocking
    // any subsequent addEdge for the same source/target pair.
    @Test
    public void testRemoveEdgeAndReAdd() {
        ImportContainerImpl importContainer = new ImportContainerImpl();
        generateTinyGraph(importContainer);
        importContainer.removeEdge(importContainer.getEdge("1"));

        // Re-add a new edge between the same nodes — must not be rejected
        NodeDraft node1 = importContainer.getNode("1");
        NodeDraft node2 = importContainer.getNode("2");
        EdgeDraft newEdge = importContainer.factory().newEdgeDraft("3");
        newEdge.setDirection(EdgeDirection.DIRECTED);
        newEdge.setSource(node1);
        newEdge.setTarget(node2);
        importContainer.addEdge(newEdge);

        Assert.assertEquals(2, importContainer.getEdgeCount());
        Assert.assertNotNull(importContainer.getEdge("3"));
        Assert.assertFalse(
            importContainer.getReport().getIssues(1).hasNext()
        );
    }

    // Bug fix: verify() was iterating nodeList without null guards in the ID-type
    // validation section, causing NPE when removed nodes left null tombstones.
    @Test
    public void testVerifyWithRemovedNodeIntegerIdType() {
        ImportContainerImpl importContainer = new ImportContainerImpl();
        importContainer.setElementIdType(ElementIdType.INTEGER);
        importContainer.setAllowAutoNode(true);

        // Add a node explicitly and an edge referencing an unknown node (auto-created)
        NodeDraft node1 = importContainer.factory().newNodeDraft("1");
        importContainer.addNode(node1);
        EdgeDraft edge = importContainer.factory().newEdgeDraft("10");
        edge.setDirection(EdgeDirection.DIRECTED);
        edge.setSource(node1);
        edge.setTarget(importContainer.getNode("2")); // auto-creates node "2"
        importContainer.addEdge(edge);

        // Disabling auto-node and calling closeLoader() removes the auto-created node,
        // leaving a null tombstone in nodeList.
        importContainer.setAllowAutoNode(false);
        importContainer.closeLoader();

        // verify() must not throw NullPointerException when nodeList contains nulls
        Assert.assertTrue(importContainer.verify());
    }

    // Bug fix: edgeExists(String, String) was calling getNode() which auto-creates
    // nodes when allowAutoNode is true, corrupting the container as a side-effect.
    @Test
    public void testEdgeExistsNoAutoNodeSideEffect() {
        ImportContainerImpl importContainer = new ImportContainerImpl();
        importContainer.setAllowAutoNode(true);

        Assert.assertFalse(importContainer.edgeExists("ghost1", "ghost2"));
        Assert.assertEquals(0, importContainer.getNodeCount());
        Assert.assertFalse(importContainer.nodeExists("ghost1"));
        Assert.assertFalse(importContainer.nodeExists("ghost2"));
    }

    // Bug fix: NullFilterIterator.hasNext() was advancing the underlying iterator
    // on every call, so calling it twice without next() skipped an element.
    @Test
    public void testNullFilterIteratorHasNextIdempotent() {
        ImportContainerImpl importContainer = new ImportContainerImpl();
        NodeDraft node1 = importContainer.factory().newNodeDraft("1");
        NodeDraft node2 = importContainer.factory().newNodeDraft("2");
        importContainer.addNode(node1);
        importContainer.addNode(node2);

        java.util.Iterator<NodeDraft> it = importContainer.getNodes().iterator();
        // Call hasNext() twice without next() — both calls must report true and
        // the subsequent next() must return the first node, not skip it.
        Assert.assertTrue(it.hasNext());
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("1", it.next().getId());
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("2", it.next().getId());
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testCheckSpecialCharacterNode() {
        ImportContainerImpl container = new ImportContainerImpl();

        container.addNode(new NodeDraftImpl(container, "foo ", 1));
        container.verify();
        Utils.assertContainerIssues(container.getReport(), Issue.Level.WARNING, "foo ");
    }

    @Test
    public void testCheckSpecialCharacterEdge() {
        ImportContainerImpl container = new ImportContainerImpl();

        NodeDraft node = new NodeDraftImpl(container, "0", 1);
        container.addNode(node);
        EdgeDraft edge = new EdgeDraftImpl(container, "bar ");
        edge.setSource(node);
        edge.setTarget(node);
        container.addEdge(edge);
        container.verify();
        Utils.assertContainerIssues(container.getReport(), Issue.Level.WARNING, "bar ");
    }

    // Utility

    private void generateTinyUndirectedGraph(ImportContainerImpl container) {
        NodeDraft node1 = container.factory().newNodeDraft("1");
        NodeDraft node2 = container.factory().newNodeDraft("2");
        EdgeDraft edge1 = container.factory().newEdgeDraft("1");
        edge1.setDirection(EdgeDirection.UNDIRECTED);
        edge1.setSource(node1);
        edge1.setTarget(node2);

        container.addNode(node1);
        container.addNode(node2);
        container.addEdge(edge1);
    }

    private void generateTinyGraphWithSelfLoop(ImportContainerImpl container, EdgeDirection edgeDirection) {
        NodeDraft node1 = container.factory().newNodeDraft("1");
        EdgeDraft edge1 = container.factory().newEdgeDraft("1");
        edge1.setDirection(edgeDirection);
        edge1.setSource(node1);
        edge1.setTarget(node1);

        container.addNode(node1);
        container.addEdge(edge1);
    }

    private void generateTinyGraph(ImportContainerImpl container) {
        NodeDraft node1 = container.factory().newNodeDraft("1");
        NodeDraft node2 = container.factory().newNodeDraft("2");
        EdgeDraft edge1 = container.factory().newEdgeDraft("1");
        edge1.setDirection(EdgeDirection.DIRECTED);
        edge1.setSource(node1);
        edge1.setTarget(node2);
        EdgeDraft edge2 = container.factory().newEdgeDraft("2");
        edge2.setDirection(EdgeDirection.DIRECTED);
        edge2.setSource(node2);
        edge2.setTarget(node1);

        container.addNode(node1);
        container.addNode(node2);
        container.addEdge(edge1);
        container.addEdge(edge2);
    }
}
