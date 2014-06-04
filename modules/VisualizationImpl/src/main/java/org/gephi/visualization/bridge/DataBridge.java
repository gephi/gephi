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
    private VizConfig vizConfig;
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
    }

    public synchronized boolean updateWorld() {
        if (observer != null && observer.hasGraphChanged()) {
            NodeModeler nodeModeler = (NodeModeler) engine.getNodeClass().getCurrentModeler();
            EdgeModeler edgeModeler = (EdgeModeler) engine.getEdgeClass().getCurrentModeler();
            Octree octree = engine.getOctree();

            //Stats
            int removedNodes = 0;
            int addedNodes = 0;
            int removedEdges = 0;
            int addedEdges = 0;

            for (int i = 0; i < nodes.length; i++) {
                NodeModel node = nodes[i];
                if (node != null && node.getNode().getStoreId() == -1) {
                    //Removed
                    octree.removeNode(node);
                    nodes[i] = null;
                    removedNodes++;
                }
            }
            for (Node node : graph.getNodes()) {
                int id = node.getStoreId();
                if (id >= nodes.length || nodes[id] == null) {
                    growNodes(id);
                    NodeModel model = nodeModeler.initModel(node);
                    octree.addNode(model);
                    nodes[id] = model;
                    addedNodes++;
                }
            }
            for (int i = 0; i < edges.length; i++) {
                EdgeModel edge = edges[i];
                if (edge != null && edge.getEdge().getStoreId() == -1) {
                    //Removed
                    NodeModel sourceModel = nodes[edge.getEdge().getSource().getStoreId()];
                    NodeModel targetModel = nodes[edge.getEdge().getTarget().getStoreId()];
                    if (sourceModel != null) {
                        sourceModel.removeEdge(edge);
                    }
                    if (targetModel != null) {
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
                if (id >= edges.length || edges[id] == null) {
                    growEdges(id);
                    NodeModel sourceModel = nodes[edge.getSource().getStoreId()];
                    NodeModel targetModel = nodes[edge.getTarget().getStoreId()];
                    EdgeModel model = edgeModeler.initModel(edge, sourceModel, targetModel);
                    sourceModel.addEdge(model);
                    targetModel.addEdge(model);
                    edges[id] = model;
                    addedEdges++;
                }
                float w = (float) edge.getWeight();
                minWeight = Math.min(w, minWeight);
                maxWeight = Math.max(w, maxWeight);
            }
            limits.setMaxWeight(maxWeight);
            limits.setMinWeight(minWeight);

            System.out.println("DATABRIDGE:");
            System.out.println(" Removed Edges: " + removedEdges);
            System.out.println(" Added Edges: " + addedEdges);
            System.out.println(" Removed Nodes: " + removedNodes);
            System.out.println(" Added Nodes: " + addedNodes);

            return true;
        } else if (observer == null) {
            Octree octree = engine.getOctree();
            if (!octree.isEmpty()) {
                octree.clear();
            }
        }
        return false;
    }

    public synchronized void reset() {
        graphModel = controller.getGraphModel();
        if (graphModel != null) {
            graph = graphModel.getGraphVisible();
        }
        if (observer != null && (graphModel == null || observer.getGraph() != graph)) {
            observer.destroy();
            observer = null;
        }
        nodes = new NodeModel[10];
        edges = new EdgeModel[10];
        if (graphModel != null) {
            observer = graphModel.createGraphObserver(graph, false);
        }
    }

    public boolean isDirected() {
        return graphModel != null && !graphModel.isUndirected();
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
