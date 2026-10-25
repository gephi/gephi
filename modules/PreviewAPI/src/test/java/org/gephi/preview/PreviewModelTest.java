package org.gephi.preview;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.spi.Renderer;
import org.gephi.preview.spi.ItemBuilder;
import org.gephi.preview.utils.MockBuilderA;
import org.gephi.preview.utils.MockBuilderB;
import org.gephi.preview.utils.MockItem;
import org.gephi.preview.utils.MockRendererA;
import org.gephi.preview.utils.MockRendererB;
import org.gephi.preview.utils.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.netbeans.junit.MockServices;
import org.openide.util.Lookup;

public class PreviewModelTest {

    @Test
    public void testEmpty() {
        PreviewModelImpl previewModel = Utils.newPreviewModel();
        Assert.assertEquals(0, previewModel.getItems(MockBuilderA.TYPE).length);
        Assert.assertEquals(0, previewModel.getItems(MockBuilderA.SOURCE_1).length);
        Assert.assertNull(previewModel.getItem(MockBuilderA.TYPE, MockBuilderA.SOURCE_1));
        Assert.assertEquals(0, previewModel.getItemTypes().length);
    }

    @Test
    public void testBuildAndLoadDropsItemsWithoutTheirNode() {
        MockServices.setServices(NodeItemBuilder.class, EdgeItemBuilder.class, NodeLabelItemBuilder.class,
            EdgeLabelItemBuilder.class);
        GraphGenerator generator = GraphGenerator.build().generateTinyGraph();
        Graph graph = generator.getGraph();
        Renderer renderer = new MockRendererA() {
            @Override
            public boolean needsItemBuilder(ItemBuilder itemBuilder, PreviewProperties properties) {
                return true;
            }
        };
        PreviewModelImpl previewModel = Utils.getPreviewModel(generator.getWorkspace());

        try {
            previewModel.buildAndLoadItems(new Renderer[] {renderer}, graph);
            Assert.assertEquals(2, previewModel.getItems(Item.NODE).length);
            Assert.assertEquals(1, previewModel.getItems(Item.EDGE).length);
            Assert.assertEquals(2, previewModel.getItems(Item.NODE_LABEL).length);
            Assert.assertEquals(1, previewModel.getItems(Item.EDGE_LABEL).length);

            // A node added after the nodes were read, but before the edges and labels were
            NodeItemBuilder.missing = graph.getNode(GraphGenerator.SECOND_NODE);
            previewModel.clear();
            previewModel.buildAndLoadItems(new Renderer[] {renderer}, graph);
            Assert.assertEquals(1, previewModel.getItems(Item.NODE).length);
            Assert.assertEquals(0, previewModel.getItems(Item.EDGE).length);
            Assert.assertEquals(1, previewModel.getItems(Item.NODE_LABEL).length);
            Assert.assertEquals(0, previewModel.getItems(Item.EDGE_LABEL).length);

            // The same when there were no nodes at all, so no node items
            NodeItemBuilder.noNodes = true;
            previewModel.clear();
            previewModel.buildAndLoadItems(new Renderer[] {renderer}, graph);
            Assert.assertEquals(0, previewModel.getItems(Item.NODE).length);
            Assert.assertEquals(0, previewModel.getItems(Item.EDGE).length);
            Assert.assertEquals(0, previewModel.getItems(Item.NODE_LABEL).length);
            Assert.assertEquals(0, previewModel.getItems(Item.EDGE_LABEL).length);

            // An edge added after the edges were read, but before the edge labels were
            NodeItemBuilder.noNodes = false;
            NodeItemBuilder.missing = null;
            EdgeItemBuilder.noEdges = true;
            previewModel.clear();
            previewModel.buildAndLoadItems(new Renderer[] {renderer}, graph);
            Assert.assertEquals(0, previewModel.getItems(Item.EDGE).length);
            Assert.assertEquals(0, previewModel.getItems(Item.EDGE_LABEL).length);
            Assert.assertEquals(2, previewModel.getItems(Item.NODE_LABEL).length);
        } finally {
            NodeItemBuilder.missing = null;
            NodeItemBuilder.noNodes = false;
            EdgeItemBuilder.noEdges = false;
        }
    }

    public static class NodeItemBuilder implements ItemBuilder {

        static volatile Node missing;
        static volatile boolean noNodes;

        @Override
        public Item[] getItems(Graph graph) {
            return Arrays.stream(graph.getNodes().toArray()).filter(n -> n != missing && !noNodes)
                .map(n -> new MockItem(n, Item.NODE, new HashMap<>())).toArray(Item[]::new);
        }

        @Override
        public String getType() {
            return Item.NODE;
        }
    }

    public static class EdgeItemBuilder implements ItemBuilder {

        static volatile boolean noEdges;

        @Override
        public Item[] getItems(Graph graph) {
            return Arrays.stream(graph.getEdges().toArray()).filter(e -> !noEdges)
                .map(e -> new MockItem(e, Item.EDGE, new HashMap<>())).toArray(Item[]::new);
        }

        @Override
        public String getType() {
            return Item.EDGE;
        }
    }

    public static class NodeLabelItemBuilder implements ItemBuilder {

        @Override
        public Item[] getItems(Graph graph) {
            return Arrays.stream(graph.getNodes().toArray())
                .map(n -> new MockItem(n, Item.NODE_LABEL, new HashMap<>())).toArray(Item[]::new);
        }

        @Override
        public String getType() {
            return Item.NODE_LABEL;
        }
    }

    public static class EdgeLabelItemBuilder implements ItemBuilder {

        @Override
        public Item[] getItems(Graph graph) {
            return Arrays.stream(graph.getEdges().toArray())
                .map(e -> new MockItem(e, Item.EDGE_LABEL, new HashMap<>())).toArray(Item[]::new);
        }

        @Override
        public String getType() {
            return Item.EDGE_LABEL;
        }
    }

    @Test
    public void testBuildAndLoadSingle() {
        MockServices.setServices(MockRendererA.class, MockBuilderA.class);

        GraphGenerator generator = GraphGenerator.build().generateTinyGraph();

        PreviewModelImpl previewModel = Utils.getPreviewModel(generator.getWorkspace());
        previewModel.buildAndLoadItems(previewModel.getManagedEnabledRenderers(), generator.getGraph());
        Assert.assertArrayEquals(new String[] {MockBuilderA.TYPE}, previewModel.getItemTypes());
        assertArrayEqualsUnordered(new Object[] {MockBuilderA.MOCK_ITEM_1, MockBuilderA.MOCK_ITEM_2}, previewModel.getItems(MockBuilderA.TYPE));
        Assert.assertArrayEquals(new Object[] {MockBuilderA.MOCK_ITEM_1}, previewModel.getItems(MockBuilderA.SOURCE_1));
        Assert.assertEquals(MockBuilderA.MOCK_ITEM_1, previewModel.getItem(MockBuilderA.TYPE, MockBuilderA.SOURCE_1));
    }

    @Test
    public void testBuildAndLoadMerge() {
        MockServices.setServices(MockRendererA.class, MockRendererB.class, MockBuilderA.class, MockBuilderB.class);

        GraphGenerator generator = GraphGenerator.build().generateTinyGraph();

        PreviewModelImpl previewModel = Utils.getPreviewModel(generator.getWorkspace());
        previewModel.buildAndLoadItems(previewModel.getManagedEnabledRenderers(), generator.getGraph());
        // Mock item from builder B with same source as item from builder A has been merged, therefore only 3 items
        assertArrayEqualsUnordered(new Object[] {MockBuilderA.MOCK_ITEM_1, MockBuilderA.MOCK_ITEM_2, MockBuilderB.MOCK_ITEM_2},
            previewModel.getItems(MockBuilderA.TYPE));
        assertArrayEqualsUnordered(new Object[] {MockBuilderA.MOCK_ITEM_1}, previewModel.getItems(MockBuilderA.SOURCE_1));
        Assert.assertEquals(MockBuilderA.MOCK_ITEM_1, previewModel.getItem(MockBuilderA.TYPE, MockBuilderA.SOURCE_1));

        Assert.assertEquals("bar", MockBuilderA.MOCK_ITEM_1.getData("foo"));
        Assert.assertEquals("42", MockBuilderA.MOCK_ITEM_1.getData("number").toString());
        Assert.assertEquals("foo", MockBuilderA.MOCK_ITEM_1.getData("bar"));
    }

    private void assertArrayEqualsUnordered(Object[] expected, Object[] actual) {
        Assert.assertEquals(new HashSet<>(Arrays.asList(expected)), new HashSet<>(Arrays.asList(actual)));
    }
}
