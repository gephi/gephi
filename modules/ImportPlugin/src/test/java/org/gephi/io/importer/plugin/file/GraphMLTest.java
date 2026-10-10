package org.gephi.io.importer.plugin.file;

import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.impl.ImportContainerImpl;
import org.junit.Assert;
import org.junit.Test;

public class GraphMLTest {

    @Test
    public void testWithDescTag() {
        ImporterGraphML importer = new ImporterGraphML();
        importer.setReader(Utils.getReader("withdesc.graphml"));

        Container container = new ImportContainerImpl();
        importer.execute(container.getLoader());
        Assert.assertTrue(container.verify());

        Utils.assertSameLabels(Utils.toNodesArray(container), "Node Zero", "Node One");
        Utils.assertSameLabels(Utils.toEdgesArray(container), "Edge Zero");
    }

    @Test
    public void testCData() {
        ImporterGraphML importer = new ImporterGraphML();
        importer.setReader(Utils.getReader("cdata.graphml"));

        Container container = new ImportContainerImpl();
        importer.execute(container.getLoader());
        Assert.assertTrue(container.verify());

        Utils.assertSameLabels(Utils.toNodesArray(container), "foo", "bar");
        Utils.assertSameLabels(Utils.toEdgesArray(container), "foobar");
    }

    @Test
    public void testIllegalControlCharacter() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<graphml>\n<graph edgedefault=\"directed\">\n" +
            "<node id=\"n0\"/>\n<node id=\"n1\"/>\n<edge source=\"n0\" target=\"n1\"><data>a\u001bb</data></edge>\n" +
            "</graph>\n</graphml>";
        ImporterGraphML importer = new ImporterGraphML();
        importer.setReader(new java.io.StringReader(xml));

        Container container = new ImportContainerImpl();
        try {
            importer.execute(container.getLoader());
            Assert.fail("Expected import failure");
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getMessage(), e.getMessage().contains("line 6"));
        }
    }
}
