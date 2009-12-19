package org.gephi.ui.preview;

import org.gephi.preview.api.GraphSheet;
import org.gephi.preview.api.PreviewController;
import org.gephi.project.api.ProjectController;
import org.gephi.workspace.api.Workspace;
import org.gephi.workspace.api.WorkspaceListener;
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
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
            }

            public void select(Workspace workspace) {
                showRefreshNotification();
            }

            public void unselect(Workspace workspace) {
            }

            public void close(Workspace workspace) {
            }

            public void disable() {
                disableRefresh();
            }
        });
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

        hideRefreshNotification();
        

        if (null == graphSheet || controllerGraphSheet != graphSheet) {
            graphSheet = controllerGraphSheet;
            newGraphFlag = true;
        }

        if (newGraphFlag) {
            previewTopComponent.setGraph(graphSheet);
        }

        previewTopComponent.refreshPreview();
    }

    /**
     * Enables the preview refresh action.
     */
    private void enableRefresh() {
        PreviewSettingsTopComponent previewSettingsTopComponent = PreviewSettingsTopComponent.findInstance();

        previewSettingsTopComponent.enableRefreshButton();
    }

    /**
     * Disables the preview refresh action.
     *
     * The preview refresh notification is also hidden.
     */
    private void disableRefresh() {
        PreviewSettingsTopComponent previewSettingsTopComponent = PreviewSettingsTopComponent.findInstance();

        hideRefreshNotification();
        previewSettingsTopComponent.disableRefreshButton();
    }

    /**
     * Shows a notification to invite the user to refresh the preview.
     *
     * The refresh action is enabled.
     */
    private void showRefreshNotification() {
        PreviewTopComponent previewTopComponent = PreviewTopComponent.findInstance();

        enableRefresh();
        previewTopComponent.showBannerPanel();
    }

    /**
     * Hides the preview refresh notification.
     */
    private void hideRefreshNotification() {
        PreviewTopComponent previewTopComponent = PreviewTopComponent.findInstance();
        
        previewTopComponent.hideBannerPanel();
    }
}
