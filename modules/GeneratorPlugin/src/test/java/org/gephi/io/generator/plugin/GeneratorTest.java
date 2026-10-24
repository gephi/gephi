package org.gephi.io.generator.plugin;

import org.gephi.graph.api.TimeRepresentation;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerUnloader;
import org.gephi.io.importer.impl.ImportContainerImpl;
import org.junit.Assert;
import org.junit.Test;

public class GeneratorTest {

    @Test
    public void testRandomGraph() {
        RandomGraph randomGraph = new RandomGraph();
        randomGraph.setNumberOfNodes(500);
        randomGraph.setWiringProbability(0.005);
        Container container = new ImportContainerImpl();
        randomGraph.generate(container.getLoader());

        Assert.assertEquals(500, container.getUnloader().getNodeCount());
    }

    @Test
    public void testDynamicGraph() {
        Container container = new ImportContainerImpl();
        new DynamicGraph().generate(container.getLoader());

        ContainerUnloader unloader = container.getUnloader();
        Assert.assertEquals(TimeRepresentation.TIMESTAMP, unloader.getTimeRepresentation());
        Assert.assertTrue(unloader.getNodeColumn("score").isDynamic());
        Assert.assertTrue(unloader.getEdgeColumn("weight").isDynamic());
    }
}
