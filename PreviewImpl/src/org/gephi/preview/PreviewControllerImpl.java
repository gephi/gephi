/*
Copyright 2008-2010 Gephi
Authors : Jeremy Subtil <jeremy.subtil@gephi.org>
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
package org.gephi.preview;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.preview.api.Graph;
import org.gephi.preview.api.GraphSheet;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewPreset;
import org.gephi.preview.presets.DefaultCurved;
import org.gephi.preview.presets.DefaultPreset;
import org.gephi.preview.presets.DefaultStraight;
import org.gephi.preview.presets.EdgesCustomColor;
import org.gephi.preview.presets.HighlightMutualEdges;
import org.gephi.preview.presets.SmallLabels;
import org.gephi.preview.presets.TagCloud;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of the preview controller.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
@ServiceProvider(service = PreviewController.class)
public class PreviewControllerImpl implements PreviewController {

    //Utils
    private final PreviewGraphFactory factory = new PreviewGraphFactory();
    private final PresetUtils presetUtils = new PresetUtils();
    //Current graphs
    private GraphImpl previewGraph = null;
    private PartialGraphImpl partialPreviewGraph = null;
    private GraphSheetImpl graphSheet = null;
    private GraphSheetImpl partialGraphSheet = null;
    //Model
    private PreviewModelImpl model;

    /**
     * Constructor.
     */
    public PreviewControllerImpl() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
                workspace.add(new PreviewModelImpl());
            }

            public void select(Workspace workspace) {
                model = (PreviewModelImpl) workspace.getLookup().lookup(PreviewModel.class);
                if (model == null) {
                    model = new PreviewModelImpl();
                    workspace.add(model);
                }
            }

            public void unselect(Workspace workspace) {
            }

            public void close(Workspace workspace) {
            }

            public void disable() {
                graphSheet = null;
                model = null;
                partialGraphSheet=null;
                previewGraph=null;
            }
        });

        // checks the current workspace state
        if (pc.getCurrentWorkspace() != null) {
            Workspace workspace = pc.getCurrentWorkspace();
            model = (PreviewModelImpl) workspace.getLookup().lookup(PreviewModel.class);
            if (model == null) {
                model = new PreviewModelImpl();
                workspace.add(model);
            }
        }
    }

    /**
     * Returns the current preview graph.
     *
     * @return the current preview graph
     */
    public Graph getGraph() {
        if (model != null) {
            if (model.isUpdateFlag()) {
                buildGraph();
            }
            return previewGraph;
        }
        return null;
    }

    /**
     * Returns a subgraph of the current preview graph.
     *
     * @param visibilityRatio  the ratio of the preview graph to display
     * @return                 a subgraph of the current preview graph
     */
    public Graph getPartialGraph(float visibilityRatio) {
        if (model != null) {
            if (model.isUpdateFlag() || null == partialPreviewGraph || partialPreviewGraph.getVisibilityRatio() != visibilityRatio) {
                Graph graph = getGraph();
                if (graph == null) {
                    return null;
                }
                partialPreviewGraph = new PartialGraphImpl(graph, visibilityRatio);
                model.setVisibilityRatio(visibilityRatio);
            }
            return partialPreviewGraph;
        }
        return null;
    }

    public GraphSheet getGraphSheet() {
        if (model != null) {
            if (model.isUpdateFlag() || null == graphSheet || graphSheet.getGraph() != previewGraph) {
                Graph graph = getGraph();
                if (graph == null) {
                    return null;
                }
                graphSheet = new GraphSheetImpl(graph);
            }
            return graphSheet;
        }
        return null;
    }

    public GraphSheet getPartialGraphSheet(float visibilityRatio) {
        if (model != null) {
            if (model.isUpdateFlag() || null == partialGraphSheet
                    || ((PartialGraphImpl) partialGraphSheet.getGraph()).getVisibilityRatio() != visibilityRatio) {
                Graph graph = getPartialGraph(visibilityRatio);
                if (graph != null) {
                    partialGraphSheet = new GraphSheetImpl(graph);
                }
            }
            return partialGraphSheet;
        }
        return null;
    }

    /**
     * Retreives the workspace graph and builds a preview graph from it.
     *
     * For each build, the supervisors' lists of supervised elements are
     * cleared because the previous preview graph is forgotten.
     *
     * @see PreviewController#buildGraph()
     */
    public void buildGraph() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        model.clearSupervisors();

        graphModel.getGraph().readLock();

        if (graphModel.isUndirected()) {
            previewGraph = factory.createPreviewGraph(model, graphModel.getHierarchicalUndirectedGraphVisible());
        } else if (graphModel.isDirected()) {
            previewGraph = factory.createPreviewGraph(model, graphModel.getHierarchicalDirectedGraphVisible());
        } else if (graphModel.isMixed()) {
            previewGraph = factory.createPreviewGraph(model, graphModel.getHierarchicalMixedGraphVisible());
        }

        graphModel.getGraph().readUnlockAll();

        model.setUpdateFlag(true);
    }

    public PreviewModel getModel() {
        return model;
    }

    public PreviewPreset[] getDefaultPresets() {
        List<PreviewPreset> presets = new ArrayList<PreviewPreset>();
        presets.add(new DefaultPreset());
        presets.add(new DefaultCurved());
        presets.add(new DefaultStraight());
        presets.add(new SmallLabels());
        presets.add(new HighlightMutualEdges());
        presets.add(new TagCloud());
        presets.add(new EdgesCustomColor());
        return presets.toArray(new PreviewPreset[0]);
    }

    public PreviewPreset[] getUserPresets() {
        PreviewPreset[] presetsArray = presetUtils.getPresets();
        Arrays.sort(presetsArray);
        return presetsArray;
    }

    public void savePreset(String name) {
        if (model != null) {
            PreviewPreset preset = model.wrapPreset(name);
            presetUtils.savePreset(preset);
            model.setCurrentPreset(preset);
        }
    }

    public void setCurrentPreset(PreviewPreset preset) {
        if (model != null) {
            model.setCurrentPreset(preset);
            model.applyPreset(preset);
        }
    }

    public void setBackgroundColor(Color color) {
        if(model!=null) {
            model.setBackgroundColor(color);
        }
    }
}
