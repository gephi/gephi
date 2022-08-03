package org.gephi.io.importer.plugin.file;

import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.impl.ImportContainerImpl;
import org.junit.Assert;
import org.junit.Test;

public class VNATest {

    @Test
    public void testEmptyAttribute() {
        ImporterVNA importer = new ImporterVNA();
        importer.setReader(Utils.getReader("emptyattribute.vna"));

        Container container = new ImportContainerImpl();
        importer.execute(container.getLoader());

        Assert.assertTrue(container.verify());
        Assert.assertTrue(container.getReport().getIssuesList(100).isEmpty());

        NodeDraft[] nodes = Utils.toNodesArray(container);
        Utils.assertSameIds(nodes, "1", "2", "3");
        Assert.assertNull(nodes[0].getValue("spots"));
    }
}
