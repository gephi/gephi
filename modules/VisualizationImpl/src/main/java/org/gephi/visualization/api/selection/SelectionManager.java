/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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

package org.gephi.visualization.api.selection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Rect2D;
import org.gephi.visualization.CurrentWorkspaceVizEngine;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.apiimpl.VizConfig;
import org.gephi.viz.engine.status.GraphSelection;
import org.gephi.viz.engine.structure.GraphIndex;
import org.joml.Vector2f;
import org.openide.util.Lookup;

/**
 * @author Mathieu Bastian
 */
public class SelectionManager implements VizArchitecture {

    private final List<ChangeListener> listeners;
    private VizConfig vizConfig;
    private CurrentWorkspaceVizEngine currentVizEngine;
    //Settings
    // TODO: use these options in the engine selection:
    private int mouseSelectionDiameter;
    private boolean mouseSelectionZoomProportional;
    //States
    private boolean blocked = false;
    private boolean wasAutoSelectNeighbors = false;
    private boolean wasRectangleSelection = false;
    private boolean wasDirectSelection = false;

    public SelectionManager() {
        listeners = new ArrayList<>();
    }

    @Override
    public void initArchitecture() {
        this.vizConfig = VizController.getInstance().getVizConfig();
        this.currentVizEngine = Lookup.getDefault().lookup(CurrentWorkspaceVizEngine.class);
        mouseSelectionDiameter = vizConfig.getMouseSelectionDiameter();
    }

    private Optional<GraphSelection> currentEngineSelectionModel() {
        return currentVizEngine.getEngine().map(engine -> {
            return engine.getLookup().lookup(GraphSelection.class);
        });
    }

    public void blockSelection(boolean block) {
        if (vizConfig.isRectangleSelection()) {
            this.blocked = block;
            vizConfig.setSelectionEnable(!block);
            fireChangeEvent();
        } else {
            setDirectMouseSelection();
        }
    }

    public void disableSelection() {
        vizConfig.setSelectionEnable(false);
        this.blocked = false;
        fireChangeEvent();
    }

    public void setRectangleSelection() {
        vizConfig.setCustomSelection(false);
        vizConfig.setSelectionEnable(true);
        setEngineSelectionMode(GraphSelection.GraphSelectionMode.RECTANGLE_SELECTION);
        this.blocked = false;
        fireChangeEvent();
    }

    public void setDirectMouseSelection() {
        vizConfig.setSelectionEnable(true);
        vizConfig.setCustomSelection(false);
        setEngineSelectionMode(GraphSelection.GraphSelectionMode.SIMPLE_MOUSE_SELECTION);
        this.blocked = false;
        fireChangeEvent();
    }

    public void setDraggingMouseSelection() {
        vizConfig.setSelectionEnable(true);
        vizConfig.setCustomSelection(false);
        setEngineSelectionMode(GraphSelection.GraphSelectionMode.SIMPLE_MOUSE_SELECTION);
        this.blocked = false;
        fireChangeEvent();
    }

    public void setCustomSelection() {
        vizConfig.setSelectionEnable(false);
        vizConfig.setCustomSelection(true);
        setEngineSelectionMode(GraphSelection.GraphSelectionMode.SIMPLE_MOUSE_SELECTION);
        this.blocked = true;
        fireChangeEvent();
    }

    public void resetSelection(VizModel vizModel) {
        if (isCustomSelection()) {
            currentEngineSelectionModel()
                    .ifPresent(GraphSelection::clearSelection);

            vizModel.setAutoSelectNeighbor(wasAutoSelectNeighbors);
            if (wasRectangleSelection) {
                setRectangleSelection();
            } else if (wasDirectSelection) {
                setDirectMouseSelection();
            }
        }
    }

    public List<Node> getSelectedNodes() {
        return currentEngineSelectionModel()
            .map(selection -> (List<Node>) new ArrayList<>(selection.getSelectedNodes()))
            .orElse(Collections.emptyList());
    }

    public List<Edge> getSelectedEdges() {
        return currentEngineSelectionModel()
            .map(selection -> (List<Edge>) new ArrayList<>(selection.getSelectedEdges()))
            .orElse(Collections.emptyList());
    }

    public void selectNodes(Node[] nodes, VizModel vizModel) {
        if (!isCustomSelection()) {
            wasAutoSelectNeighbors = vizModel.isAutoSelectNeighbor();
            wasDirectSelection = isDirectMouseSelection();
            wasRectangleSelection = isRectangleSelection();
            vizModel.setAutoSelectNeighbor(false);
            setCustomSelection();
        }

        currentEngineSelectionModel()
            .ifPresent(selection -> {
                if (nodes == null) {
                    selection.clearSelectedNodes();
                } else {
                    selection.setSelectedNodes(Arrays.asList(nodes));
                }
            });
    }

    public void selectEdges(Edge[] edges, VizModel vizModel) {
        if (!isCustomSelection()) {
            wasAutoSelectNeighbors = vizModel.isAutoSelectNeighbor();
            wasDirectSelection = isDirectMouseSelection();
            wasRectangleSelection = isRectangleSelection();
            vizModel.setAutoSelectNeighbor(false);
            setCustomSelection();
        }

        currentEngineSelectionModel()
            .ifPresent(selection -> {
                if (edges == null) {
                    selection.clearSelectedEdges();

                } else {
                    selection.setSelectedEdges(Arrays.asList(edges));
                }
            });
    }

    private void setEngineSelectionMode(GraphSelection.GraphSelectionMode mode) {
        currentEngineSelectionModel().ifPresent(graphSelection -> {
            graphSelection.setMode(mode);
        });
    }

    public void centerOnGraph() {
        currentVizEngine.getEngine().ifPresent(engine -> {
            final GraphIndex index = engine.getLookup().lookup(GraphIndex.class);
            final Rect2D visibleGraphBoundaries = index.getGraphBoundaries();

            final float[] center = visibleGraphBoundaries.center();
            engine.centerOn(
                new Vector2f(center[0], center[1]),
                visibleGraphBoundaries.width(),
                visibleGraphBoundaries.height()
            );
        });
    }

    public void centerOnNode(Node node) {
        if (node != null) {
            currentVizEngine.getEngine().ifPresent(engine -> {
                final Vector2f position = new Vector2f(node.x(), node.y());
                final float size = node.size();
                engine.centerOn(position, size, size);
            });
        }
    }

    public void centerOnEdge(Edge edge) {
        if (edge != null) {
            currentVizEngine.getEngine().ifPresent(engine -> {
                //TODO center on edge
            });
        }
    }

    public int getMouseSelectionDiameter() {
        return mouseSelectionDiameter;
    }

    public void setMouseSelectionDiameter(int mouseSelectionDiameter) {
        this.mouseSelectionDiameter = mouseSelectionDiameter;
    }

    public boolean isMouseSelectionZoomProportional() {
        return mouseSelectionZoomProportional;
    }

    public void setMouseSelectionZoomProportional(boolean mouseSelectionZoomProportional) {
        this.mouseSelectionZoomProportional = mouseSelectionZoomProportional;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public boolean isRectangleSelection() {
        return vizConfig.isSelectionEnable() && vizConfig.isRectangleSelection();
    }

    public boolean isDirectMouseSelection() {
        return vizConfig.isSelectionEnable() && !vizConfig.isRectangleSelection();
    }

    public boolean isCustomSelection() {
        return vizConfig.isCustomSelection();
    }

    public boolean isSelectionEnabled() {
        return vizConfig.isSelectionEnable();
    }

    //Event
    public void addChangeListener(ChangeListener changeListener) {
        listeners.add(changeListener);
    }

    public void removeChangeListener(ChangeListener changeListener) {
        listeners.remove(changeListener);
    }

    private void fireChangeEvent() {
        ChangeEvent evt = new ChangeEvent(this);
        for (ChangeListener l : listeners) {
            l.stateChanged(evt);
        }
    }
}
