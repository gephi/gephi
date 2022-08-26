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
}
