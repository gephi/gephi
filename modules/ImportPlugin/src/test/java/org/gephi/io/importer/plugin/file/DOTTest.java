package org.gephi.io.importer.plugin.file;

import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.junit.Assert;
import org.junit.Test;

public class DOTTest {

    @Test
    public void testBasic() {
        Container container = Utils.importFile(new ImporterDOT(), "dot/basic.dot");
        Assert.assertEquals(container.getUnloader().getEdgeDefault(), EdgeDirectionDefault.DIRECTED);
        NodeDraft[] nodes = Utils.toNodesArray(container);
        Assert.assertEquals(3, nodes.length);
        EdgeDraft[] edges = Utils.toEdgesArray(container);
        Utils.assertSameEdges(edges, "A -> B", "B -> C");
    }

    @Test
    public void testUndirectedBasic() {
        Container container = Utils.importFile(new ImporterDOT(), "dot/undirected.dot");
        Assert.assertEquals(container.getUnloader().getEdgeDefault(), EdgeDirectionDefault.UNDIRECTED);
        NodeDraft[] nodes = Utils.toNodesArray(container);
        Assert.assertEquals(2, nodes.length);
        EdgeDraft[] edges = Utils.toEdgesArray(container);
        Utils.assertSameEdges(edges, "A -> B");
        Assert.assertEquals("bar", edges[0].getValue("foo"));
    }

    @Test
    public void testMultipleEdges() {
        Container container = Utils.importFile(new ImporterDOT(), "dot/multipleedgesperline.dot");
        NodeDraft[] nodes = Utils.toNodesArray(container);
        Assert.assertEquals(4, nodes.length);
        EdgeDraft[] edges = Utils.toEdgesArray(container);
        Utils.assertSameEdges(edges, "a -> b", "b -> c", "b -> d");
    }

    @Test
    public void testUndirectedMultipleEdges() {
        Container container = Utils.importFile(new ImporterDOT(), "dot/multipleedgesperline2.dot");
        Assert.assertEquals(container.getUnloader().getEdgeDefault(), EdgeDirectionDefault.UNDIRECTED);
        NodeDraft[] nodes = Utils.toNodesArray(container);
        Assert.assertEquals(4, nodes.length);
        EdgeDraft[] edges = Utils.toEdgesArray(container);
        Utils.assertSameEdges(edges, "a -> b", "b -> c", "b -> d");
    }

    @Test
    public void testEmptyFieldsGraph() {
        Container container = Utils.importFile(new ImporterDOT(), "dot/emptyfields.dot");
        Assert.assertTrue(container.verify());
        NodeDraft[] nodes = Utils.toNodesArray(container);
        Utils.assertSameIds(nodes, "a", "b", "c");
    }

    @Test
    public void testNamedGraph() {
        Container container = Utils.importFile(new ImporterDOT(), "dot/namedgraph.dot");
        NodeDraft[] nodes = Utils.toNodesArray(container);
        Utils.assertSameIds(nodes, "n");
    }

    @Test
    public void testSubgraph() {
        Container container = Utils.importFile(new ImporterDOT(), "dot/subgraph.dot");

        NodeDraft[] nodes = Utils.toNodesArray(container);
        Assert.assertEquals(2, nodes.length);
        Assert.assertEquals("a", nodes[0].getValue("foo"));
        Assert.assertEquals("b", nodes[1].getValue("foo"));
        Assert.assertEquals(1, Utils.toEdgesArray(container).length);
    }

    @Test
    public void testIntLabels() {
        Container container = Utils.importFile(new ImporterDOT(), "dot/intlabels.dot");
        NodeDraft[] nodes = Utils.toNodesArray(container);
        Assert.assertEquals("-1", nodes[0].getLabel());
    }

    @Test
    public void testAdjacencyList() {
        Container container = Utils.importFile(new ImporterDOT(), "dot/adjacencylist.dot");
        NodeDraft[] nodes = Utils.toNodesArray(container);
        Utils.assertSameIds(nodes, "A", "B", "C", "D");
        Assert.assertEquals(5, Utils.toEdgesArray(container).length);
    }

    @Test
    public void testLabels() {
        Container container = Utils.importFile(new ImporterDOT(), "dot/labels.dot");
        NodeDraft[] nodes = Utils.toNodesArray(container);
        Utils.assertSameIds(nodes, "A", "B", "C");
        Assert.assertEquals("Node A", nodes[0].getLabel());
        EdgeDraft[] edges = Utils.toEdgesArray(container);
        Assert.assertEquals("Edge A to B", edges[0].getLabel());
        Assert.assertEquals("Edge B to C", edges[1].getLabel());
    }

    @Test
    public void testHashLineComment() {
        Container container = Utils.importFile(new ImporterDOT(), "dot/hashcomment.dot");
        NodeDraft[] nodes = Utils.toNodesArray(container);
        Utils.assertSameIds(nodes, "n1", "n2");
        EdgeDraft[] edges = Utils.toEdgesArray(container);
        Utils.assertSameEdges(edges, "n1 -> n2");
    }

    @Test
    public void testNoSpacesDirected() {
        Container container = Utils.importFile(new ImporterDOT(), "dot/nospaces.dot");
        Assert.assertEquals(EdgeDirectionDefault.DIRECTED, container.getUnloader().getEdgeDefault());
        NodeDraft[] nodes = Utils.toNodesArray(container);
        Utils.assertSameIds(nodes, "n1", "n2", "n3", "n4");
        EdgeDraft[] edges = Utils.toEdgesArray(container);
        Utils.assertSameEdges(edges, "n1 -> n2", "n2 -> n3", "n3 -> n4");
    }

    @Test
    public void testNoSpacesUndirected() {
        Container container = Utils.importFile(new ImporterDOT(), "dot/nospaces_undirected.dot");
        Assert.assertEquals(EdgeDirectionDefault.UNDIRECTED, container.getUnloader().getEdgeDefault());
        NodeDraft[] nodes = Utils.toNodesArray(container);
        Utils.assertSameIds(nodes, "n1", "n2", "n3", "n4");
        EdgeDraft[] edges = Utils.toEdgesArray(container);
        Assert.assertEquals(3, edges.length);
    }

    @Test
    public void testNoSpacesMixed() {
        Container container = Utils.importFile(new ImporterDOT(), "dot/nospaces_mixed.dot");
        NodeDraft[] nodes = Utils.toNodesArray(container);
        Utils.assertSameIds(nodes, "n1", "n2", "n3", "n4", "n5", "n6");
        EdgeDraft[] edges = Utils.toEdgesArray(container);
        Utils.assertSameEdges(edges, "n1 -> n2", "n3 -> n4", "n5 -> n6");
    }

    @Test
    public void testNodeAttrStatement() {
        // node [shape=box] is an attr_stmt and should be skipped; the graph must still load
        Container container = Utils.importFile(new ImporterDOT(), "dot/nodeattrstatement.dot");
        NodeDraft[] nodes = Utils.toNodesArray(container);
        Utils.assertSameIds(nodes, "n1", "n2");
        EdgeDraft[] edges = Utils.toEdgesArray(container);
        Utils.assertSameEdges(edges, "n1 -> n2");
    }

    @Test
    public void testAttrStatements() {
        // graph/node/edge attr_stmts must all be skipped; edges and nodes must still load
        Container container = Utils.importFile(new ImporterDOT(), "dot/attrstatements.dot");
        NodeDraft[] nodes = Utils.toNodesArray(container);
        Utils.assertSameIds(nodes, "A", "B", "C");
        EdgeDraft[] edges = Utils.toEdgesArray(container);
        Utils.assertSameEdges(edges, "A -> B", "B -> C");
    }

    @Test
    public void testChainedAttrLists() {
        // node_id [attr1=val1] [attr2=val2] — chained attr_lists must all be parsed
        Container container = Utils.importFile(new ImporterDOT(), "dot/chainedattrs.dot");
        NodeDraft[] nodes = Utils.toNodesArray(container);
        Assert.assertEquals(2, nodes.length);
        for (NodeDraft node : nodes) {
            Assert.assertNotNull("label must be set for " + node.getId(), node.getLabel());
        }
        EdgeDraft[] edges = Utils.toEdgesArray(container);
        Assert.assertEquals(1, edges.length);
        Assert.assertEquals("edge_label", edges[0].getLabel());
    }

    @Test
    public void testChainedAttrListsInEdgeChain() {
        // a -> b [attr1] [attr2] -> c [attr3] [attr4] — chained attrs mid-edge-chain
        Container container = Utils.importFile(new ImporterDOT(), "dot/chainedattrs_edgechain.dot");
        NodeDraft[] nodes = Utils.toNodesArray(container);
        Utils.assertSameIds(nodes, "a", "b", "c");
        EdgeDraft[] edges = Utils.toEdgesArray(container);
        Assert.assertEquals(2, edges.length);
        Utils.assertSameEdges(edges, "a -> b", "b -> c");
        EdgeDraft ab = edges[0].getSource().getId().equals("a") ? edges[0] : edges[1];
        EdgeDraft bc = edges[0].getSource().getId().equals("b") ? edges[0] : edges[1];
        Assert.assertEquals("ab", ab.getLabel());
        Assert.assertEquals("bc", bc.getLabel());
    }
}
