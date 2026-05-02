package org.gephi.desktop.preview;

import org.gephi.desktop.preview.api.PreviewUIController;
import org.gephi.desktop.preview.utils.Utils;
import org.gephi.preview.api.PreviewPreset;
import org.gephi.preview.presets.DefaultCurved;
import org.gephi.project.api.Workspace;
import org.gephi.project.io.utils.GephiFormat;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.openide.util.Lookup;

public class PersistenceProviderTest {

    private PreviewPreset savedPreset;

    @After
    public void tearDown() {
        if (savedPreset != null) {
            PreviewUIController controller = Lookup.getDefault().lookup(PreviewUIController.class);
            if (controller != null) {
                controller.removePreset(savedPreset);
            }
            savedPreset = null;
        }
    }

    @Test
    public void testDefaultPreset() throws Exception {
        PreviewUIModelImpl previewUIModel = Utils.newPreviewUIModel();
        GephiFormat.testXMLPersistenceProvider(new PreviewUIPersistenceProvider(), previewUIModel.getWorkspace());
    }

    @Test
    public void testOtherPreset() throws Exception {
        PreviewUIModelImpl previewUIModel = Utils.newPreviewUIModel();
        previewUIModel.setCurrentPreset(new DefaultCurved());
        GephiFormat.testXMLPersistenceProvider(new PreviewUIPersistenceProvider(), previewUIModel.getWorkspace());
    }

    @Test
    public void testUserPreset() throws Exception {
        savedPreset = new PreviewPreset("Foo");
        PreviewUIController controller = Lookup.getDefault().lookup(PreviewUIController.class);
        controller.addPreset(savedPreset);

        PreviewUIModelImpl previewUIModel = Utils.newPreviewUIModel();
        previewUIModel.setCurrentPreset(savedPreset);
        GephiFormat.testXMLPersistenceProvider(new PreviewUIPersistenceProvider(), previewUIModel.getWorkspace());
    }

    @Test
    public void testVisibilityRatio() throws Exception {
        PreviewUIModelImpl previewUIModel = Utils.newPreviewUIModel();
        previewUIModel.setVisibilityRatio(0.5f);

        Workspace workspace =
            GephiFormat.testXMLPersistenceProvider(new PreviewUIPersistenceProvider(), previewUIModel.getWorkspace());
        PreviewUIModelImpl newModel = workspace.getLookup().lookup(PreviewUIModelImpl.class);
        Assert.assertEquals(previewUIModel.getVisibilityRatio(), newModel.getVisibilityRatio(), 0.0001f);
    }
}
