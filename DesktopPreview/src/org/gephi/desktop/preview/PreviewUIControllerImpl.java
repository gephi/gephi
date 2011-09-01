/*
Copyright 2008-2010 Gephi
Authors : Jérémy Subtil <jeremy.subtil@gephi.org>, Mathieu Bastian
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

import java.beans.PropertyEditorManager;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.desktop.preview.api.PreviewUIController;
import org.gephi.desktop.preview.api.PreviewUIModel;
import org.gephi.desktop.preview.propertyeditors.DependantColorPropertyEditor;
import org.gephi.desktop.preview.propertyeditors.DependantOriginalColorPropertyEditor;
import org.gephi.desktop.preview.propertyeditors.EdgeColorPropertyEditor;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphEvent;
import org.gephi.graph.api.GraphListener;
import org.gephi.graph.api.GraphModel;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewPreset;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.presets.DefaultPreset;
import org.gephi.preview.types.DependantColor;
import org.gephi.preview.types.DependantOriginalColor;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Controller implementation of the preview UI.
 *
 * @author Jérémy Subtil, Mathieu Bastian
 */
@ServiceProvider(service = PreviewUIController.class)
public class PreviewUIControllerImpl implements PreviewUIController, GraphListener {

    private final PreviewController previewController;
    private final GraphController graphController;
    private final PresetUtils presetUtils = new PresetUtils();
    private PreviewUIModelImpl model = null;
    private GraphModel graphModel = null;

    public PreviewUIControllerImpl() {
        previewController = Lookup.getDefault().lookup(PreviewController.class);
        graphController = Lookup.getDefault().lookup(GraphController.class);
        PreviewModel previewModel = previewController.getModel();
        if (previewModel != null) {
            graphModel = graphController.getModel();
            graphModel.addGraphListener(this);
        }

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
                workspace.add(new PreviewUIModelImpl());
            }

            public void select(Workspace workspace) {
                //Make sure AttributeModel is created before graph model:
                Lookup.getDefault().lookup(AttributeController.class).getModel();

                graphModel = graphController.getModel();
                graphModel.addGraphListener(PreviewUIControllerImpl.this);
                showRefreshNotification();

                model = workspace.getLookup().lookup(PreviewUIModelImpl.class);
                if (model == null) {
                    model = new PreviewUIModelImpl();
                    workspace.add(model);
                }
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        PreviewSettingsTopComponent.findInstance().refreshModel();
                        PreviewTopComponent.findInstance().refreshModel();
                    }
                });
            }

            public void unselect(Workspace workspace) {
                if (graphModel != null) {
                    graphModel.removeGraphListener(PreviewUIControllerImpl.this);
                    graphModel = null;
                }
            }

            public void close(Workspace workspace) {
            }

            public void disable() {
                if (graphModel != null) {
                    graphModel.removeGraphListener(PreviewUIControllerImpl.this);
                    graphModel = null;
                }
                disableRefresh();

                //When project is closed, clear graph preview instead of keeping it:
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        PreviewSettingsTopComponent.findInstance().refreshModel();
                        PreviewTopComponent.findInstance().refreshModel();
                    }
                });
            }
        });
        if (pc.getCurrentWorkspace() != null) {
            model = pc.getCurrentWorkspace().getLookup().lookup(PreviewUIModelImpl.class);
            if (model == null) {
                model = new PreviewUIModelImpl();
                pc.getCurrentWorkspace().add(model);
            }
        }

        //Register editors
        PropertyEditorManager.registerEditor(EdgeColor.class, EdgeColorPropertyEditor.class);
        PropertyEditorManager.registerEditor(DependantOriginalColor.class, DependantOriginalColorPropertyEditor.class);
        PropertyEditorManager.registerEditor(DependantColor.class, DependantColorPropertyEditor.class);
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
        final PreviewTopComponent previewTopComponent = PreviewTopComponent.findInstance();
        final float visibilityRatio = PreviewSettingsTopComponent.findInstance().getVisibilityRatio();
        setVisibilityRatio(visibilityRatio);

        Thread refreshThread = new Thread(new Runnable() {

            public void run() {
                previewTopComponent.setRefresh(true);
                disableRefresh();
                hideRefreshNotification();
                previewController.refreshPreview();

                previewTopComponent.refreshPreview();

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

    public void setVisibilityRatio(float visibilityRatio) {
        if (model != null) {
            model.setVisibilityRatio(visibilityRatio);
        }
    }

    public PreviewUIModel getModel() {
        return model;
    }

    public PreviewPreset[] getDefaultPresets() {
        return new PreviewPreset[]{new DefaultPreset()};
    }

    public PreviewPreset[] getUserPresets() {
        PreviewPreset[] presetsArray = presetUtils.getPresets();
        Arrays.sort(presetsArray);
        return presetsArray;
    }

    public void setCurrentPreset(PreviewPreset preset) {
        if (model != null) {
            model.setCurrentPreset(preset);
            PreviewModel previewModel = previewController.getModel();
            previewModel.getProperties().applyPreset(preset);
        }
    }

    public void addPreset(PreviewPreset preset) {
        presetUtils.savePreset(preset);
    }

    public void savePreset(String name) {
        if (model != null) {
            PreviewModel previewModel = previewController.getModel();
            Map<String, Object> map = new HashMap<String, Object>();
            for (PreviewProperty p : previewModel.getProperties().getProperties()) {
                map.put(p.getName(), p.getValue());
            }
            PreviewPreset preset = new PreviewPreset(name, map);
            presetUtils.savePreset(preset);
            model.setCurrentPreset(preset);
        }
    }
}
