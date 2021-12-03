package org.gephi.io.importer.plugin.file;

import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerUnloader;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.impl.ImportContainerImpl;
import org.junit.Assert;
import org.junit.Test;

public class GEXFTest {

    @Test
    public void testBasicGraph() {
        ImporterGEXF importer = new ImporterGEXF();
        importer.setReader(Utils.getReader("basic.gexf"));

        Container container = new ImportContainerImpl();
        importer.execute(container.getLoader());

        Assert.assertTrue(container.verify());

        NodeDraft[] nodes = Utils.toNodesArray(container);
        EdgeDraft[] edges = Utils.toEdgesArray(container);

        Utils.assertSameIds(nodes, "0", "1");
        Utils.assertSameIds(edges, "0");
    }

    @Test
    public void testDoubleInfinity() {
        ImporterGEXF importer = new ImporterGEXF();
        importer.setReader(Utils.getReader("infinity.gexf"));

        Container container = new ImportContainerImpl();
        importer.execute(container.getLoader());

        NodeDraft node = Utils.getNode(container, "0");
        Assert.assertEquals(Double.POSITIVE_INFINITY, node.getValue("0"));
        Assert.assertEquals(Double.NEGATIVE_INFINITY, node.getValue("1"));
    }

    @Test
    public void testZeroWeight() {
        ImporterGEXF importer = new ImporterGEXF();
        importer.setReader(Utils.getReader("zeroweight.gexf"));

        Container container = new ImportContainerImpl();
        importer.execute(container.getLoader());

        Assert.assertTrue(container.verify());
    }
}
