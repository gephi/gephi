package org.gephi.io.importer.plugin.file;

import org.gephi.graph.api.AttributeUtils;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.MetadataDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.impl.ImportContainerImpl;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;

public class GEXFTest {

    @Test
    public void testBasicGraph() {
        ImporterGEXF importer = new ImporterGEXF();
        importer.setReader(Utils.getReader("gexf/basic.gexf"));

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
        importer.setReader(Utils.getReader("gexf/infinity.gexf"));

        Container container = new ImportContainerImpl();
        importer.execute(container.getLoader());

        NodeDraft node = Utils.getNode(container, "0");
        Assert.assertEquals(Double.POSITIVE_INFINITY, node.getValue("0"));
        Assert.assertEquals(Double.NEGATIVE_INFINITY, node.getValue("1"));
    }

    @Test
    public void testZeroWeight() {
        ImporterGEXF importer = new ImporterGEXF();
        importer.setReader(Utils.getReader("gexf/zeroweight.gexf"));

        Container container = new ImportContainerImpl();
        importer.execute(container.getLoader());

        Assert.assertTrue(container.verify());
    }

    @Test
    public void testTimezone() {
        ImporterGEXF importer = new ImporterGEXF();
        importer.setReader(Utils.getReader("gexf/timezone.gexf"));

        Container container = new ImportContainerImpl();
        importer.execute(container.getLoader());

        DateTimeZone timeZone = container.getUnloader().getTimeZone();
        Assert.assertEquals(DateTimeZone.forID("America/Los_Angeles"), timeZone);

        NodeDraft node0 = Utils.getNode(container, "0");
        NodeDraft node1 = Utils.getNode(container, "1");

        node0.getTimeSet().contains(AttributeUtils.parseDateTime("2012-01-12T15:00:00", timeZone));
        node1.getTimeSet()
            .contains(AttributeUtils.parseDateTime("2012-01-12T15:00:00", DateTimeZone.forID("Europe/Moscow")));
    }

    @Test
    public void testMeta() {
        ImporterGEXF importer = new ImporterGEXF();
        importer.setReader(Utils.getReader("gexf/meta.gexf"));

        Container container = new ImportContainerImpl();
        importer.execute(container.getLoader());

        MetadataDraft meta = container.getUnloader().getMetadata();
        Assert.assertEquals("TITLE", meta.getTitle());
        Assert.assertEquals("DESCRIPTION", meta.getDescription());
    }
}
