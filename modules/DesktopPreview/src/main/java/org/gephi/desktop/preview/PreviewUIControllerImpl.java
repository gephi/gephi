/*
 Copyright 2008-2010 Gephi
 Authors : Jérémy Subtil <jeremy.subtil@gephi.org>, Mathieu Bastian
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.desktop.preview;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.SwingUtilities;
import org.gephi.desktop.preview.api.PreviewUIController;
import org.gephi.desktop.preview.api.PreviewUIModel;
import org.gephi.desktop.preview.propertyeditors.DependantColorPropertyEditor;
import org.gephi.desktop.preview.propertyeditors.DependantOriginalColorPropertyEditor;
import org.gephi.desktop.preview.propertyeditors.EdgeColorPropertyEditor;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewPreset;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.presets.BlackBackground;
import org.gephi.preview.presets.DefaultCurved;
import org.gephi.preview.presets.DefaultPreset;
import org.gephi.preview.presets.DefaultStraight;
import org.gephi.preview.presets.EdgesCustomColor;
import org.gephi.preview.presets.TagCloud;
import org.gephi.preview.presets.TextOutline;
import org.gephi.preview.types.DependantColor;
import org.gephi.preview.types.DependantOriginalColor;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 * Controller implementation of the preview UI.
 *
 * @author Jérémy Subtil, Mathieu Bastian
 */
@ServiceProvider(service = PreviewUIController.class)
public class PreviewUIControllerImpl implements PreviewUIController {

    private final List<PropertyChangeListener> listeners;
    private final PreviewController previewController;
    private final GraphController graphController;
    private final PresetUtils presetUtils = new PresetUtils();
    private PreviewUIModelImpl model = null;
    private GraphModel graphModel = null;

    public PreviewUIControllerImpl() {
        previewController = Lookup.getDefault().lookup(PreviewController.class);
        listeners = new ArrayList<PropertyChangeListener>();
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        graphController = Lookup.getDefault().lookup(GraphController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {
            @Override
            public void initialize(Workspace workspace) {
                if (workspace.getLookup().lookup(PreviewUIModelImpl.class) == null) {
                    workspace.add(new PreviewUIModelImpl());
                }
                enableRefresh();
            }

            @Override
            public void select(Workspace workspace) {
                graphModel = graphController.getGraphModel(workspace);

                model = workspace.getLookup().lookup(PreviewUIModelImpl.class);
                if (model == null) {
                    model = new PreviewUIModelImpl();
                    workspace.add(model);
                }
                Float visibilityRatio = previewController.getModel().getProperties().getFloatValue(PreviewProperty.VISIBILITY_RATIO);
                if (visibilityRatio != null) {
                    ((PreviewUIModelImpl) model).setVisibilityRatio(visibilityRatio);
                }
                fireEvent(SELECT, model);
            }

            @Override
            public void unselect(Workspace workspace) {
                if (graphModel != null) {
                    graphModel = null;
                }
                fireEvent(UNSELECT, model);
            }

            @Override
            public void close(Workspace workspace) {
            }

            @Override
            public void disable() {
                if (graphModel != null) {
                    graphModel = null;
                }
                fireEvent(SELECT, null);
                model = null;
            }
        });
        if (pc.getCurrentWorkspace() != null) {
            model = pc.getCurrentWorkspace().getLookup().lookup(PreviewUIModelImpl.class);
            if (model == null) {
                model = new PreviewUIModelImpl();
                pc.getCurrentWorkspace().add(model);
            }
            Float visibilityRatio = previewController.getModel().getProperties().getFloatValue(PreviewProperty.VISIBILITY_RATIO);
            if (visibilityRatio != null) {
                ((PreviewUIModelImpl) model).setVisibilityRatio(visibilityRatio);
            }
            graphModel = graphController.getGraphModel(pc.getCurrentWorkspace());
        }

        //Register editors
        //Overriding default Preview API basic editors that don't support CustomEditor
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
//    public void graphChanged(GraphEvent event) {
//        boolean previous = model.isWorkspaceBarVisible();
//        model.setWorkspaceBarVisible(true);
//        if (!previous) {
//            fireEvent(GRAPH_CHANGED, true);
//        }
//    }
    /**
     * Refreshes the preview applet.
     */
    @Override
    public void refreshPreview() {
        if (model != null) {
            Thread refreshThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    model.setRefreshing(true);
                    fireEvent(REFRESHING, true);

                    previewController.getModel().getProperties().putValue(PreviewProperty.VISIBILITY_RATIO, model.getVisibilityRatio());
                    previewController.refreshPreview();

                    fireEvent(REFRESHED, model);

                    model.setRefreshing(false);
                    fireEvent(REFRESHING, false);
                    fireEvent(GRAPH_CHANGED, false);
                }
            }, "Refresh Preview");
            refreshThread.start();
        }
    }

    /**
     * Enables the preview refresh action.
     */
    private void enableRefresh() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                PreviewSettingsTopComponent pstc = (PreviewSettingsTopComponent) WindowManager.getDefault().findTopComponent("PreviewSettingsTopComponent");
                pstc.enableRefreshButton();
            }
        });
    }

    @Override
    public void setVisibilityRatio(float visibilityRatio) {
        if (model != null) {
            model.setVisibilityRatio(visibilityRatio);
        }
    }

    @Override
    public PreviewUIModel getModel() {
        return model;
    }

    @Override
    public PreviewPreset[] getDefaultPresets() {
        return new PreviewPreset[]{new DefaultPreset(), new DefaultCurved(), new DefaultStraight(), new TextOutline(), new BlackBackground(), new EdgesCustomColor(), new TagCloud()};
    }

    @Override
    public PreviewPreset[] getUserPresets() {
        PreviewPreset[] presetsArray = presetUtils.getPresets();
        Arrays.sort(presetsArray);
        return presetsArray;
    }

    @Override
    public void setCurrentPreset(PreviewPreset preset) {
        if (model != null) {
            model.setCurrentPreset(preset);
            PreviewModel previewModel = previewController.getModel();
            previewModel.getProperties().applyPreset(preset);
        }
    }

    @Override
    public void addPreset(PreviewPreset preset) {
        presetUtils.savePreset(preset);
    }

    @Override
    public void savePreset(String name) {
        if (model != null) {
            PreviewModel previewModel = previewController.getModel();
            Map<String, Object> map = new HashMap<String, Object>();
            for (PreviewProperty p : previewModel.getProperties().getProperties()) {
                map.put(p.getName(), p.getValue());
            }
            for (Entry<String, Object> p : previewModel.getProperties().getSimpleValues()) {
                map.put(p.getKey(), p.getValue());
            }
            PreviewPreset preset = new PreviewPreset(name, map);
            presetUtils.savePreset(preset);
            model.setCurrentPreset(preset);
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.remove(listener);
    }

    private void fireEvent(String eventName, Object data) {
        PropertyChangeEvent event = new PropertyChangeEvent(this, eventName, null, data);
        for (PropertyChangeListener l : listeners) {
            l.propertyChange(event);
        }
    }
}
