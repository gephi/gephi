/*
Copyright 2008-2010 Gephi
Authors : Jérémy Subtil <jeremy.subtil@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.gephi.desktop.preview;

import javax.swing.SwingUtilities;
import org.gephi.data.attributes.api.AttributeController;
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
                //Make sure AttributeModel is created before graph model:
                Lookup.getDefault().lookup(AttributeController.class).getModel();

                graphModel = gc.getModel();
                graphModel.addGraphListener(PreviewUIController.this);
                showRefreshNotification();
            }

            public void unselect(Workspace workspace) {
                if (graphModel != null) {
                    graphModel.removeGraphListener(PreviewUIController.this);
                    graphModel = null;
                }
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
                PreviewTopComponent previewTopComponent = PreviewTopComponent.findInstance();
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
