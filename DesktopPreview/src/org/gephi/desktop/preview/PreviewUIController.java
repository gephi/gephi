package org.gephi.desktop.preview;

import java.awt.Color;
import javax.swing.SwingUtilities;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphEvent;
import org.gephi.graph.api.GraphListener;
import org.gephi.graph.api.GraphModel;
import org.gephi.preview.api.GraphSheet;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
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
        PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
        final GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        PreviewModel previewModel = previewController.getModel();
        if (previewModel != null) {
            graphModel = gc.getModel();
            graphModel.addGraphListener(this);
        }

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
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
                if (graphModel != null) {
                    graphModel.removeGraphListener(PreviewUIController.this);
                    graphModel = null;
                    graphSheet = null;
                }
                disableRefresh();

                //When project is closed, clear graph preview instead of keeping it:
                PreviewTopComponent previewTopComponent=PreviewTopComponent.findInstance();
                previewTopComponent.setGraph(null);
                previewTopComponent.refreshPreview();
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        PreviewSettingsTopComponent.findInstance().refreshModel();
                    }
                });
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
        final PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
        final PreviewTopComponent previewTopComponent = PreviewTopComponent.findInstance();
        final float visibilityRatio = PreviewSettingsTopComponent.findInstance().getVisibilityRatio();

        Thread refreshThread = new Thread(new Runnable() {

            public void run() {
                previewTopComponent.setRefresh(true);
                disableRefresh();
                hideRefreshNotification();
                GraphSheet controllerGraphSheet = previewController.getPartialGraphSheet(visibilityRatio);
                if (controllerGraphSheet != null) {
                    if (null == graphSheet || controllerGraphSheet != graphSheet) {
                        graphSheet = controllerGraphSheet;
                        previewTopComponent.setGraph(graphSheet);
                    }
                    previewTopComponent.refreshPreview();
                }

                previewTopComponent.setRefresh(false);
                enableRefresh();
            }
        }, "Refresh Preview");
        refreshThread.start();
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
                PreviewSettingsTopComponent.findInstance().refreshModel();
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
