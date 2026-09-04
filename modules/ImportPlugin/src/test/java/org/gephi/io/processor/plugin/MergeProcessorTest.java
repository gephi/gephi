package org.gephi.io.processor.plugin;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.impl.ImportContainerImpl;
import org.gephi.io.importer.impl.NodeDraftImpl;
import org.gephi.project.api.Workspace;
import org.gephi.project.impl.WorkspaceImpl;
import org.junit.Assert;
import org.junit.Test;

public class MergeProcessorTest {

    @Test
    public void testMergeIntoSuppliedWorkspace() {
        Workspace workspace = new WorkspaceImpl(null, 1);

        ImportContainerImpl firstContainer = new ImportContainerImpl();
        firstContainer.addNode(new NodeDraftImpl(firstContainer, "1", 1));
        firstContainer.addNode(new NodeDraftImpl(firstContainer, "99", 99));

        ImportContainerImpl secondContainer = new ImportContainerImpl();
        secondContainer.addNode(new NodeDraftImpl(secondContainer, "1", 1));
        secondContainer.addNode(new NodeDraftImpl(secondContainer, "2", 2));

        MergeProcessor processor = new MergeProcessor();
        processor.setContainers(new ImportContainerImpl[] {firstContainer, secondContainer});
        processor.setWorkspace(workspace);
        processor.process();

        // Node "99" only exists in the first container: still present after the merge proves elements
        // are merged by id across all containers rather than only the last one being processed.
        GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
        Graph graph = graphModel.getGraph();
        Assert.assertEquals(3, graph.getNodeCount());
        Assert.assertNotNull(graph.getNode("1"));
        Assert.assertNotNull(graph.getNode("2"));
        Assert.assertNotNull(graph.getNode("99"));
    }

    @Test
    public void testSourceIsSetOnSuppliedWorkspace() {
        Workspace workspace = new WorkspaceImpl(null, 1);

        ImportContainerImpl firstContainer = new ImportContainerImpl();
        NodeDraft nodeDraft = new NodeDraftImpl(firstContainer, "1", 1);
        firstContainer.addNode(nodeDraft);
        firstContainer.setSource("some-file.gexf");

        ImportContainerImpl secondContainer = new ImportContainerImpl();
        secondContainer.addNode(new NodeDraftImpl(secondContainer, "2", 2));

        MergeProcessor processor = new MergeProcessor();
        processor.setContainers(new ImportContainerImpl[] {firstContainer, secondContainer});
        processor.setWorkspace(workspace);
        processor.process();

        Assert.assertEquals("some-file.gexf", workspace.getSource());
    }
}
