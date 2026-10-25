package org.gephi.io.importer.plugin.file;

import java.io.StringReader;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.impl.ImportContainerImpl;
import org.junit.Assert;
import org.junit.Test;

public class GMLTest {

    @Test
    public void testLabels() {
        ImporterGML importer = new ImporterGML();
        importer.setReader(Utils.getReader("label.gml"));

        Container container = new ImportContainerImpl();
        importer.execute(container.getLoader());

        Assert.assertTrue(container.verify());

        NodeDraft[] nodes = Utils.toNodesArray(container);

        Utils.assertSameIds(nodes, "A", "B", "C");
    }

    @Test
    public void testEmojis() {
        ImporterGML importer = new ImporterGML();
        importer.setReader(Utils.getReader("emojis.gml"));

        Container container = new ImportContainerImpl();
        importer.execute(container.getLoader());

        Assert.assertTrue(container.verify());

        NodeDraft[] nodes = Utils.toNodesArray(container);
        NodeDraft node1 = nodes[0];

        Assert.assertEquals("✅", node1.getLabel());
    }

    @Test
    public void testInvalidNumericPropertyKey() {
        ImporterGML importer = new ImporterGML();
        importer.setReader(new StringReader("graph [ node [ 1.0 value ] ]"));

        Container container = new ImportContainerImpl();
        importer.execute(container.getLoader());

        Assert.assertTrue(importer.getReport().getIssuesList(100).stream()
            .anyMatch(issue -> issue.getLevel() == Issue.Level.SEVERE));
    }
}
