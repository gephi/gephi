package org.gephi.preview;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.gephi.graph.GraphGenerator;
import org.gephi.preview.spi.ItemBuilder;
import org.gephi.preview.utils.MockBuilderA;
import org.gephi.preview.utils.MockBuilderB;
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
