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
package org.gephi.visualization.bridge;

import java.util.Arrays;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.ColumnObserver;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphObserver;
import org.gephi.graph.api.Node;
import org.gephi.visualization.GraphLimits;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.apiimpl.VizConfig;
import org.gephi.visualization.model.edge.EdgeModel;
import org.gephi.visualization.model.edge.EdgeModeler;
import org.gephi.visualization.model.node.NodeModel;
import org.gephi.visualization.model.node.NodeModeler;
import org.gephi.visualization.octree.Octree;
import org.gephi.visualization.opengl.AbstractEngine;
import org.gephi.visualization.text.TextManager;
import org.gephi.visualization.text.TextModelImpl;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class DataBridge implements VizArchitecture {

    //Const
    protected static final long ONEOVERPHI = 106039;
    //Architecture
    protected AbstractEngine engine;
    protected GraphController controller;
    protected TextManager textManager;
    protected ColumnObserver[] nodeColumnObservers;
    protected ColumnObserver[] edgeColumnObservers;
    protected int nodeColumnHashCode;
    protected int edgeColumnHashCode;
    private VizConfig vizConfig;
    private TextModelImpl textModel;
    protected GraphLimits limits;
    //Graph
    protected GraphModel graphModel;
    protected Graph graph;
    protected GraphObserver observer;
    //Data
    protected NodeModel[] nodes;
    protected EdgeModel[] edges;

    @Override
    public void initArchitecture() {
        this.engine = VizController.getInstance().getEngine();
        this.controller = Lookup.getDefault().lookup(GraphController.class);
        this.vizConfig = VizController.getInstance().getVizConfig();
        this.limits = VizController.getInstance().getLimits();
        this.textManager = VizController.getInstance().getTextManager();
    }

    public synchronized boolean updateWorld() {
        boolean force = false;
        if ((observer != null && observer.isDestroyed()) || (graphModel != null && graph.getView() != graphModel.getVisibleView())) {
            if (observer != null && !observer.isDestroyed()) {
                observer.destroy();
            }
            observer = null;
            if (graphModel != null) {
                graph.writeLock();
                graph = graphModel.getGraphVisible();
                observer = graphModel.createGraphObserver(graph, false);
                force = true;
                graph.writeUnlock();
            }
        }
        if (force || (observer != null && (observer.isNew() || observer.hasGraphChanged())) || hasColumnsChanged()) {
            if (observer.isNew()) {
                observer.hasGraphChanged();
            }
            NodeModeler nodeModeler = engine.getNodeModeler();
            EdgeModeler edgeModeler = engine.getEdgeModeler();
            Octree octree = engine.getOctree();

            //Stats
            int removedNodes = 0;
            int addedNodes = 0;
            int removedEdges = 0;
            int addedEdges = 0;

            graph.readLock();
            boolean isView = !graph.getView().isMainView();
            for (int i = 0; i < nodes.length; i++) {
                NodeModel node = nodes[i];
                if (node != null && (node.getNode().getStoreId() == -1 || (isView && !graph.contains(node.getNode())))) {
                    //Removed
                    octree.removeNode(node);
                    nodes[i] = null;
                    removedNodes++;
                }
            }
            for (Node node : graph.getNodes()) {
                int id = node.getStoreId();
                NodeModel model;
                if (id >= nodes.length || nodes[id] == null) {
                    growNodes(id);
                    model = nodeModeler.initModel(node);
                    octree.addNode(model);
                    nodes[id] = model;
                    addedNodes++;
                } else {
                    model = nodes[id];
                }
                textManager.refreshNode(graph, model, textModel);
            }
            for (int i = 0; i < edges.length; i++) {
                EdgeModel edge = edges[i];
                if (edge != null && (edge.getEdge().getStoreId() == -1 || (isView && !graph.contains(edge.getEdge())))) {
                    //Removed
                    int sourceId = edge.getEdge().getSource().getStoreId();
                    int targetId = edge.getEdge().getTarget().getStoreId();
                    NodeModel sourceModel = sourceId == -1 ? null : nodes[sourceId];
                    NodeModel targetModel = targetId == -1 ? null : nodes[targetId];
                    if (sourceModel != null) {
                        sourceModel.removeEdge(edge);
                    }
                    if (targetModel != null && sourceModel != targetModel) {
                        targetModel.removeEdge(edge);
                    }
                    edges[i] = null;
                    removedEdges++;
                }
            }
            float minWeight = Float.MAX_VALUE;
            float maxWeight = Float.MIN_VALUE;
            for (Edge edge : graph.getEdges()) {
                int id = edge.getStoreId();
                EdgeModel model;
                if (id >= edges.length || edges[id] == null) {
                    growEdges(id);
                    NodeModel sourceModel = nodes[edge.getSource().getStoreId()];
                    NodeModel targetModel = nodes[edge.getTarget().getStoreId()];
                    model = edgeModeler.initModel(edge, sourceModel, targetModel);
                    sourceModel.addEdge(model);
                    if (targetModel != sourceModel) {
                        targetModel.addEdge(model);
                    }
                    edges[id] = model;
                    addedEdges++;
                } else {
                    model = edges[id];
                }
                float w = (float) edge.getWeight(graph.getView());
                model.setWeight(w);
                minWeight = Math.min(w, minWeight);
                maxWeight = Math.max(w, maxWeight);

                textManager.refreshEdge(graph, model, textModel);
            }
            if (!isView) {
                limits.setMaxWeight(maxWeight);
                limits.setMinWeight(minWeight);
            }

            graph.readUnlock();

            return true;
        } else if (observer == null) {
            Octree octree = engine.getOctree();
            if (!octree.isEmpty()) {
                octree.clear();
            }
        }
        return false;
    }

    private boolean hasColumnsChanged() {
        if (nodeColumnObservers != null && edgeColumnObservers != null) {
            Column[] nodeColumns = textModel.getNodeTextColumns();
            int nodeCode = Arrays.hashCode(nodeColumns);
            if (nodeCode != nodeColumnHashCode) {
                refreshNodeColumns(textModel);
                return true;
            }
            Column[] edgeColumns = textModel.getEdgeTextColumns();
            int edgeCode = Arrays.hashCode(edgeColumns);
            if (edgeCode != edgeColumnHashCode) {
                refreshEdgeColumns(textModel);
                return true;
            }

            boolean nodeC = false, edgeC = false;
            for (ColumnObserver c : nodeColumnObservers) {
                nodeC = nodeC | c.hasColumnChanged();
            }
            for (ColumnObserver c : edgeColumnObservers) {
                edgeC = edgeC | c.hasColumnChanged();
            }
            return nodeC || edgeC;
        }
        return false;
    }

    public synchronized void reset() {
        graphModel = controller.getGraphModel();
        if (graphModel != null) {
            graph = graphModel.getGraphVisible();
        }
        if (observer != null && (graphModel == null || observer.getGraph() != graph)) {
            if (!observer.isDestroyed()) {
                observer.destroy();
            }
            observer = null;
        }
        if (nodeColumnObservers != null) {
            for (ColumnObserver c : nodeColumnObservers) {
                if (!c.isDestroyed()) {
                    c.destroy();
                }
            }
            nodeColumnObservers = null;
        }
        if (edgeColumnObservers != null) {
            for (ColumnObserver c : edgeColumnObservers) {
                if (!c.isDestroyed()) {
                    c.destroy();
                }
            }
            edgeColumnObservers = null;
        }
        nodes = new NodeModel[10];
        edges = new EdgeModel[10];
        Octree octree = engine.getOctree();
        if (!octree.isEmpty()) {
            octree.clear();
        }
        if (graphModel != null) {
            observer = graphModel.createGraphObserver(graph, false);
            textModel = VizController.getInstance().getVizModel().getTextModel();
            refreshNodeColumns(textModel);
            refreshEdgeColumns(textModel);
        }
    }

    private void refreshNodeColumns(TextModelImpl textModelImpl) {
        if (nodeColumnObservers != null) {
            for (ColumnObserver c : nodeColumnObservers) {
                if (!c.isDestroyed()) {
                    c.destroy();
                }
            }
            nodeColumnObservers = null;
        }
        Column[] nodeColumns = textModelImpl.getNodeTextColumns();
        nodeColumnHashCode = Arrays.hashCode(nodeColumns);
        nodeColumnObservers = new ColumnObserver[nodeColumns.length];
        for (int i = 0; i < nodeColumns.length; i++) {
            nodeColumnObservers[i] = nodeColumns[i].createColumnObserver(false);
        }
    }

    private void refreshEdgeColumns(TextModelImpl textModelImpl) {
        if (edgeColumnObservers != null) {
            for (ColumnObserver c : edgeColumnObservers) {
                if (!c.isDestroyed()) {
                    c.destroy();
                }
            }
            edgeColumnObservers = null;
        }
        Column[] edgeColumns = textModelImpl.getEdgeTextColumns();
        edgeColumnHashCode = Arrays.hashCode(edgeColumns);
        edgeColumnObservers = new ColumnObserver[edgeColumns.length];
        for (int i = 0; i < edgeColumns.length; i++) {
            edgeColumnObservers[i] = edgeColumns[i].createColumnObserver(false);
        }
    }

    public boolean isDirected() {
        return graphModel != null && !graphModel.isUndirected();
    }

    public Graph getGraph() {
        return graph;
    }

    private void growNodes(final int index) {
        if (nodes == null) {
            nodes = new NodeModel[10];
        } else if (index >= nodes.length) {
            final int newLength = (int) Math.min(Math.max((ONEOVERPHI * nodes.length) >>> 16, index + 1), Integer.MAX_VALUE);
            final NodeModel t[] = new NodeModel[newLength];
            System.arraycopy(nodes, 0, t, 0, nodes.length);
            nodes = t;
        }
    }

    private void growEdges(final int index) {
        if (edges == null) {
            edges = new EdgeModel[10];
        } else if (index >= edges.length) {
            final int newLength = (int) Math.min(Math.max((ONEOVERPHI * edges.length) >>> 16, index + 1), Integer.MAX_VALUE);
            final EdgeModel t[] = new EdgeModel[newLength];
            System.arraycopy(edges, 0, t, 0, edges.length);
            edges = t;
        }
    }
}
