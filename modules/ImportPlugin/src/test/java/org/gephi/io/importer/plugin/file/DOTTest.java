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
        ImporterDOT importer = new ImporterDOT();
        importer.setReader(Utils.getReader("emptyfields.dot"));

        Container container = new ImportContainerImpl();
        importer.execute(container.getLoader());

        Assert.assertTrue(container.verify());

        NodeDraft[] nodes = Utils.toNodesArray(container);

        Utils.assertSameIds(nodes, "a", "b", "c");
    }
}
