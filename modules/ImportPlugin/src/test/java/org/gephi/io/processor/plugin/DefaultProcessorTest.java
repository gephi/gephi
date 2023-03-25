package org.gephi.io.processor.plugin;

import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.importer.api.MetadataDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.impl.ImportContainerImpl;
import org.gephi.io.importer.impl.NodeDraftImpl;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceMetaData;
import org.gephi.project.impl.WorkspaceImpl;
import org.junit.Assert;
import org.junit.Test;

public class DefaultProcessorTest {

    @Test
    public void testProcess() {
        ImportContainerImpl importContainer = new ImportContainerImpl();
        NodeDraft nodeDraft = new NodeDraftImpl(importContainer, "1", 1);
        importContainer.addNode(nodeDraft);

        Workspace workspace = new WorkspaceImpl(null, 1);
        DefaultProcessor defaultProcessor = new DefaultProcessor();
        defaultProcessor.setContainers(new ImportContainerImpl[] {importContainer});
        defaultProcessor.setWorkspace(workspace);
        defaultProcessor.process();

        GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
        Node node = graphModel.getGraph().getNode("1");
        Assert.assertNotNull(node);
    }

    @Test
    public void testMeta() {
        ImportContainerImpl importContainer = new ImportContainerImpl();
        importContainer.setMetadata(MetadataDraft.builder().description("foo").title("bar").build());

        Workspace workspace = new WorkspaceImpl(null, 1);
        DefaultProcessor defaultProcessor = new DefaultProcessor();
        defaultProcessor.setContainers(new ImportContainerImpl[] {importContainer});
        defaultProcessor.setWorkspace(workspace);
        defaultProcessor.process();

        WorkspaceMetaData workspaceMetaData = workspace.getWorkspaceMetadata();
        Assert.assertEquals("foo", workspaceMetaData.getDescription());
        Assert.assertEquals("bar", workspaceMetaData.getTitle());
    }
}
