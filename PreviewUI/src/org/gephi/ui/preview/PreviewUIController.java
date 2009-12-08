package org.gephi.ui.preview;

import org.gephi.preview.api.GraphSheet;
import org.gephi.preview.api.PreviewController;
import org.openide.util.Lookup;

/**
 * Controller implementation of the preview UI.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class PreviewUIController {

    private static PreviewUIController instance;
    private GraphSheet graphSheet = null;

    /**
     * Private constructor.
     */
    private PreviewUIController() {
    }

    /**
     * Returns the PreviewUIController singleton instance.
     *
     * @return the PreviewUIController singleton instance
     */
    public static synchronized PreviewUIController findInstance() {
        if (null == instance) {
            instance = new PreviewUIController();
        }
        return instance;
    }

    /**
     * Refreshes the preview applet.
     */
    public void refreshPreview() {
        PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
        PreviewTopComponent previewTopComponent = PreviewTopComponent.findInstance();
        PreviewSettingsTopComponent previewSettingsTopComponent = PreviewSettingsTopComponent.findInstance();
        boolean newGraphFlag = false;
        float visibilityRatio = previewSettingsTopComponent.getVisibilityRatio();
        GraphSheet controllerGraphSheet = previewController.getPartialGraphSheet(visibilityRatio);

        // UI update
        previewTopComponent.hideBannerPanel();

        if (null == graphSheet || controllerGraphSheet != graphSheet) {
            graphSheet = controllerGraphSheet;
            newGraphFlag = true;
        }

        if (newGraphFlag) {
            previewTopComponent.setGraph(graphSheet);
        }

        previewTopComponent.refreshPreview();
    }
}
