package org.gephi.ui.preview;

import javax.swing.SwingUtilities;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphEvent;
import org.gephi.graph.api.GraphListener;
import org.gephi.graph.api.GraphModel;
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
public class PreviewUIController implements GraphListener {

    private static PreviewUIController instance;
    private GraphSheet graphSheet = null;
    private GraphModel graphModel = null;

    /**
     * Private constructor.
     */
    private PreviewUIController() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        final GraphController gc = Lookup.getDefault().lookup(GraphController.class);

        // checks the current workspace state before listening to the related events
        if (pc.getCurrentWorkspace() != null) {
            graphModel = gc.getModel();
            graphModel.addGraphListener(this);
        }

        pc.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
            }

            public void select(Workspace workspace) {
                graphModel = gc.getModel();
                graphModel.addGraphListener(PreviewUIController.this);
                showRefreshNotification();
            }

            public void unselect(Workspace workspace) {
                graphModel.removeGraphListener(PreviewUIController.this);
                graphModel = null;
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
     * Shows the refresh notification when the structure of the workspace graph
     * has changed.
     *
     * @param event
     * @see GraphListener#graphChanged(org.gephi.graph.api.GraphEvent)
     */
    public void graphChanged(GraphEvent event) {
        showRefreshNotification();
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
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                PreviewSettingsTopComponent previewSettingsTopComponent = PreviewSettingsTopComponent.findInstance();
                previewSettingsTopComponent.enableRefreshButton();
            }
        });
    }

    /**
     * Disables the preview refresh action.
     *
     * The preview refresh notification is also hidden.
     */
    private void disableRefresh() {
        hideRefreshNotification();

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                PreviewSettingsTopComponent previewSettingsTopComponent = PreviewSettingsTopComponent.findInstance();
                previewSettingsTopComponent.disableRefreshButton();
            }
        });
    }

    /**
     * Shows a notification to invite the user to refresh the preview.
     *
     * The refresh action is enabled.
     */
    private void showRefreshNotification() {
        enableRefresh();

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                PreviewTopComponent previewTopComponent = PreviewTopComponent.findInstance();
                previewTopComponent.showBannerPanel();
            }
        });
    }

    /**
     * Hides the preview refresh notification.
     */
    private void hideRefreshNotification() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                PreviewTopComponent previewTopComponent = PreviewTopComponent.findInstance();
                previewTopComponent.hideBannerPanel();
            }
        });
    }
}
