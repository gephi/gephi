package org.gephi.preview;

import java.awt.Color;
import org.gephi.preview.api.ManagedRenderer;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.utils.MockRendererA;
import org.gephi.preview.utils.MockRendererB;
import org.gephi.preview.utils.Utils;
import org.gephi.project.io.utils.GephiFormat;
import org.junit.Assert;
import org.junit.Test;
import org.netbeans.junit.MockServices;

public class PersistenceProviderTest {

    @Test
    public void testEmpty() throws Exception {
        PreviewModelImpl previewModel = Utils.newPreviewModel();
        GephiFormat.testXMLPersistenceProvider(new PreviewPersistenceProvider(), previewModel.getWorkspace());
    }

    @Test
    public void testDefaultRendererOrder() throws Exception {
        MockServices.setServices(MockRendererA.class, MockRendererB.class);

        PreviewModelImpl previewModel = Utils.newPreviewModel();
        PreviewModelImpl readModel = Utils.getPreviewModel(
            GephiFormat.testXMLPersistenceProvider(new PreviewPersistenceProvider(), previewModel.getWorkspace()));
        Assert.assertArrayEquals(previewModel.getManagedRenderers(), readModel.getManagedRenderers());
    }

    @Test
    public void testDisabledRenderer() throws Exception {
        MockServices.setServices(MockRendererA.class, MockRendererB.class);

        PreviewModelImpl previewModel = Utils.newPreviewModel();
        previewModel.setManagedRenderers(new ManagedRenderer[] {previewModel.getManagedRenderers()[0]});

        PreviewModelImpl readModel = Utils.getPreviewModel(
            GephiFormat.testXMLPersistenceProvider(new PreviewPersistenceProvider(), previewModel.getWorkspace()));
        Assert.assertArrayEquals(previewModel.getManagedRenderers(), readModel.getManagedRenderers());
    }

    @Test
    public void testChangeRendererOrder() throws Exception {
        MockServices.setServices(MockRendererA.class, MockRendererB.class);

        PreviewModelImpl previewModel = Utils.newPreviewModel();
        previewModel.setManagedRenderers(new ManagedRenderer[] {previewModel.getManagedRenderers()[1], previewModel.getManagedRenderers()[0]});

        PreviewModelImpl readModel = Utils.getPreviewModel(
            GephiFormat.testXMLPersistenceProvider(new PreviewPersistenceProvider(), previewModel.getWorkspace()));
        Assert.assertArrayEquals(previewModel.getManagedRenderers(), readModel.getManagedRenderers());
    }

    @Test
    public void testProperty() throws Exception {
        PreviewModelImpl previewModel = Utils.newPreviewModel();
        PreviewProperties properties = previewModel.getProperties();
        properties.putValue(PreviewProperty.BACKGROUND_COLOR, Color.CYAN);

        PreviewModelImpl readModel = Utils.getPreviewModel(
            GephiFormat.testXMLPersistenceProvider(new PreviewPersistenceProvider(), previewModel.getWorkspace()));
        Assert.assertEquals(Color.CYAN, readModel.getProperties().getValue(PreviewProperty.BACKGROUND_COLOR));
    }

    @Test
    public void testGlobalCanvasSize() throws Exception {
        PreviewModelImpl previewModel = Utils.newPreviewModel();
        Assert.assertFalse(previewModel.isGlobalCanvasSize());
        previewModel.setGlobalCanvasSize(true);

        PreviewModelImpl readModel = Utils.getPreviewModel(
            GephiFormat.testXMLPersistenceProvider(new PreviewPersistenceProvider(), previewModel.getWorkspace()));
        Assert.assertTrue(readModel.isGlobalCanvasSize());
    }
}
