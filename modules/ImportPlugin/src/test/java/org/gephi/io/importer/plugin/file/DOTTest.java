package org.gephi.io.importer.plugin.file;

import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerUnloader;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.impl.ImportContainerImpl;
import org.junit.Assert;
import org.junit.Test;

public class DOTTest {

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
}
