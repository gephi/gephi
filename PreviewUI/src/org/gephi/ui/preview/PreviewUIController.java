package org.gephi.ui.preview;

import org.gephi.preview.api.Graph;
import org.gephi.preview.api.PreviewController;
import org.openide.util.Lookup;

/**
 * Controller implementation of the preview UI.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class PreviewUIController {

    private static PreviewUIController instance;
    private Graph graph = null;

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
        Graph controllerGraph = previewController.getPartialGraph(visibilityRatio);

        // UI update
        previewTopComponent.hideBannerPanel();

        if (null == graph || controllerGraph != graph) {
            graph = controllerGraph;
            newGraphFlag = true;
        }

        if (newGraphFlag) {
            previewTopComponent.setGraph(graph);
        }

        previewTopComponent.refreshPreview();
    }
}
