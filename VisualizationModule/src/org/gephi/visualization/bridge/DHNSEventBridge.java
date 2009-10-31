/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.visualization.bridge;

import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Model;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.GraphIO;
import org.gephi.visualization.api.ModelImpl;
import org.gephi.visualization.api.objects.ModelClass;
import org.gephi.visualization.hull.ConvexHull;
import org.gephi.visualization.opengl.AbstractEngine;
import org.gephi.visualization.opengl.compatibility.objects.ConvexHullModel;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class DHNSEventBridge implements EventBridge, VizArchitecture {

    //Architecture
    private AbstractEngine engine;
    private HierarchicalGraph graph;
    private GraphIO graphIO;

    @Override
    public void initArchitecture() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        this.engine = VizController.getInstance().getEngine();
        this.graphIO = VizController.getInstance().getGraphIO();
        initEvents();
    }

    @Override
    public void initEvents() {
    }

    public boolean canExpand() {
        ModelImpl[] selectedNodeModels = engine.getSelectedObjects(AbstractEngine.CLASS_NODE);
        if (selectedNodeModels.length == 1) {
            ModelImpl metaModel = selectedNodeModels[0];
            //TODO check it is a metaNode
            return true;
        }
        return false;
    }

    public boolean canContract() {
        ModelImpl[] selectedNodeModels = engine.getSelectedObjects(AbstractEngine.CLASS_POTATO);
        if (selectedNodeModels.length == 1) {
            ModelImpl metaModel = selectedNodeModels[0];
            //TODO check it is a metaNode
            return true;
        }
        return false;
    }

    public boolean canGroup() {
        return true;
    }

    public boolean canUngroup() {
        return true;
    }

    public void expand() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        this.graph = graphController.getModel().getHierarchicalGraph();
        ModelImpl[] selectedNodeModels = engine.getSelectedObjects(AbstractEngine.CLASS_NODE);
        if (selectedNodeModels.length == 1) {
            ModelImpl metaModel = selectedNodeModels[0];
            //TODO check it is a metaNode
            NodeData node = (NodeData) metaModel.getObj();
            expandPositioning(node);
            graph.expand(node.getNode());
        }
    }

    public void contract() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        this.graph = graphController.getModel().getHierarchicalGraph();
        ModelImpl[] selectedNodeModels = engine.getSelectedObjects(AbstractEngine.CLASS_POTATO);
        if (selectedNodeModels.length == 1) {
            ModelImpl metaModel = selectedNodeModels[0];
            //TODO check it is a metaNode
            ConvexHull hull = (ConvexHull) metaModel.getObj();
            contractPositioning(hull);
            graph.retract(hull.getMetaNode());
        }
    }

    public void group() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        this.graph = graphController.getModel().getHierarchicalGraph();
        ModelImpl[] selectedNodeModels = engine.getSelectedObjects(AbstractEngine.CLASS_NODE);
        Node[] newGroup = new Node[selectedNodeModels.length];
        for (int i = 0; i < selectedNodeModels.length; i++) {
            newGroup[i] = ((NodeData) selectedNodeModels[i].getObj()).getNode();
        }
        float centroidX = 0;
        float centroidY = 0;
        int len = 0;
        Node group = graph.groupNodes(newGroup);
        group.getNodeData().setLabel("Group");
        group.getNodeData().setSize(10f);
        for (Node child : newGroup) {
            centroidX += child.getNodeData().x();
            centroidY += child.getNodeData().y();
            len++;
        }
        centroidX /= len;
        centroidY /= len;
        group.getNodeData().setX(centroidX);
        group.getNodeData().setY(centroidY);
    }

    public void ungroup() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        this.graph = graphController.getModel().getHierarchicalGraph();
        ModelImpl[] selectedNodeModels = engine.getSelectedObjects(AbstractEngine.CLASS_NODE);
        if (selectedNodeModels.length == 1) {
            ModelImpl metaModel = selectedNodeModels[0];
            //TODO check it is a metaNode
            NodeData node = (NodeData) metaModel.getObj();
            graph.ungroupNodes(node.getNode());
        }
    }

    public void mouseClick(ModelClass objClass, Model[] clickedObjects) {

        /* switch (objClass.getClassId()) {
        case AbstractEngine.CLASS_NODE:
        for (int i = 0; i < clickedObjects.length; i++) {
        Model obj = clickedObjects[i];
        Node node = (Node) obj.getObj();
        DhnsController dhnsController = Lookup.getDefault().lookup(DhnsController.class);
        freeModifier.expand(node);
        }
        break;
        case AbstractEngine.CLASS_POTATO:
        for (int i = 0; i < clickedObjects.length; i++) {
        Model obj = clickedObjects[i];
        Potato potato = (Potato) obj.getObj();
        DhnsController dhnsController = Lookup.getDefault().lookup(DhnsController.class);
        freeModifier.retract(potato.getNode());
        }
        break;
        }*/
    }

    private void expandPositioning(NodeData nodeData) {
        Node node = nodeData.getNode();
        float centroidX = 0;
        float centroidY = 0;
        int len = 0;
        Node[] children = graph.getChildren(node).toArray();
        for (Node child : children) {
            centroidX += child.getNodeData().x();
            centroidY += child.getNodeData().y();
            len++;
        }
        centroidX /= len;
        centroidY /= len;

        float diffX = nodeData.x() - centroidX;
        float diffY = nodeData.y() - centroidY;
        for (Node child : children) {
            NodeData nd = child.getNodeData();
            nd.setX(nd.x() + diffX);
            nd.setY(nd.y() + diffY);
        }
    }

    private void contractPositioning(ConvexHull hull) {
        NodeData metaNode = hull.getMetaNode().getNodeData();
        metaNode.setX(hull.x());
        metaNode.setY(hull.y());

        ConvexHullModel model = (ConvexHullModel) hull.getModel();
        model.setScale(0.9f);
        model.setScaleQuantum(-0.1f);
    }
}
