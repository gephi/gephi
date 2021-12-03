package org.gephi.statistics.plugin;

import org.gephi.graph.api.GraphModel;
import org.gephi.io.importer.GraphImporter;
import org.junit.Assert;
import org.junit.Test;

public class DummyTest {

    @Test
    public void testDummy() {
        GraphModel graphModel = GraphImporter.importGraph(DummyTest.class, "basic.gexf");

        Assert.assertEquals(2, graphModel.getGraph().getNodeCount());
        Assert.assertEquals(1, graphModel.getGraph().getEdgeCount());
    }
}
