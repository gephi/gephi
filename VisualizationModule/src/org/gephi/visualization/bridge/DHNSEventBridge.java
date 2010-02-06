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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GroupData;
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
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
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
    private GraphController graphController;

    @Override
    public void initArchitecture() {
        this.engine = VizController.getInstance().getEngine();
        this.graphIO = VizController.getInstance().getGraphIO();
        this.graphController = Lookup.getDefault().lookup(GraphController.class);
        initEvents();
    }

    @Override
    public void initEvents() {
    }

    public boolean canExpand() {
        this.graph = graphController.getModel().getHierarchicalGraphVisible();
        ModelImpl[] selectedNodeModels = engine.getSelectedObjects(AbstractEngine.CLASS_NODE);
        for (ModelImpl metaModelImpl : selectedNodeModels) {
            NodeData nodeData = (NodeData) metaModelImpl.getObj();
            Node node = nodeData.getNode(graph.getView().getViewId());
            if (graph.getDescendantCount(node) > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean canContract() {
        this.graph = graphController.getModel().getHierarchicalGraphVisible();
        ModelImpl[] selectedNodeModels = engine.getSelectedObjects(AbstractEngine.CLASS_NODE);
        for (ModelImpl metaModelImpl : selectedNodeModels) {
            NodeData nodeData = (NodeData) metaModelImpl.getObj();
            Node node = nodeData.getNode(graph.getView().getViewId());
            if (graph.getParent(node) != null) {
                return true;
            }
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
        this.graph = graphController.getModel().getHierarchicalGraphVisible();
        ModelImpl[] selectedNodeModels = engine.getSelectedObjects(AbstractEngine.CLASS_NODE);
        final ModelImpl[] models = Arrays.copyOf(selectedNodeModels, selectedNodeModels.length);
        new Thread(new Runnable() {

            public void run() {
                try {
                    for (ModelImpl metaModelImpl : models) {
                        Node node = ((NodeData) metaModelImpl.getObj()).getNode(graph.getView().getViewId());
                        if (graph.getDescendantCount(node) > 0) {
                            expandPositioning(node);
                            graph.expand(node);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    graph.readUnlockAll();
                }
            }
        }, "Expand nodes").start();
    }

    public void contract() {
        this.graph = graphController.getModel().getHierarchicalGraphVisible();
        ModelImpl[] selectedNodeModels = engine.getSelectedObjects(AbstractEngine.CLASS_NODE);
        final ModelImpl[] models = Arrays.copyOf(selectedNodeModels, selectedNodeModels.length);
        new Thread(new Runnable() {

            public void run() {
                try {
                    Set<Node> parents = new HashSet<Node>();
                    for (ModelImpl metaModelImpl : models) {
                        NodeData nodeData = (NodeData) metaModelImpl.getObj();
                        Node node = nodeData.getNode(graph.getView().getViewId());
                        Node nodeParent = graph.getParent(node);
                        if (nodeParent != null) {
                            parents.add(nodeParent);
                        }
                    }

                    for (Node parent : parents) {
                        GroupData gd = (GroupData) parent.getNodeData();
                        if (gd.getHullModel() != null) {
                            ConvexHull hull = ((ConvexHullModel) gd.getHullModel()).getObj();
                            contractPositioning(hull);
                        }
                        graph.retract(parent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    graph.readUnlockAll();
                }
            }
        }, "Contract nodes").start();
    }

    public void group() {
        this.graph = graphController.getModel().getHierarchicalGraphVisible();
        ModelImpl[] selectedNodeModels = engine.getSelectedObjects(AbstractEngine.CLASS_NODE);
        final Node[] newGroup = new Node[selectedNodeModels.length];
        for (int i = 0; i < selectedNodeModels.length; i++) {
            newGroup[i] = ((NodeData) selectedNodeModels[i].getObj()).getNode(graph.getView().getViewId());
        }
        new Thread(new Runnable() {

            public void run() {
                try {
                    float centroidX = 0;
                    float centroidY = 0;
                    int len = 0;
                    float sizes = 0;
                    float r = 0;
                    float g = 0;
                    float b = 0;
                    Node group = graph.groupNodes(newGroup);
                    group.getNodeData().setLabel("Group (" + newGroup.length + " nodes)");
                    group.getNodeData().setSize(10f);
                    for (Node child : newGroup) {
                        centroidX += child.getNodeData().x();
                        centroidY += child.getNodeData().y();
                        len++;
                        sizes += child.getNodeData().getSize() / 10f;
                        r += child.getNodeData().r();
                        g += child.getNodeData().g();
                        b += child.getNodeData().b();
                    }
                    centroidX /= len;
                    centroidY /= len;
                    group.getNodeData().setSize(sizes);
                    group.getNodeData().setColor(r / len, g / len, b / len);
                    group.getNodeData().setX(centroidX);
                    group.getNodeData().setY(centroidY);
                } catch (Exception e) {
                    graph.readUnlockAll();
                    NotifyDescriptor.Message nd = new NotifyDescriptor.Message(e.getMessage());
                    DialogDisplayer.getDefault().notifyLater(nd);
                }
            }
        }, "Group nodes").start();
    }

    public void ungroup() {
        this.graph = graphController.getModel().getHierarchicalGraph();
        ModelImpl[] selectedNodeModels = engine.getSelectedObjects(AbstractEngine.CLASS_NODE);
        final ModelImpl[] models = Arrays.copyOf(selectedNodeModels, selectedNodeModels.length);
        new Thread(new Runnable() {

            public void run() {
                try {
                    Set<Node> parents = new HashSet<Node>();
                    for (ModelImpl metaModelImpl : models) {
                        NodeData nodeData = (NodeData) metaModelImpl.getObj();
                        Node node = nodeData.getNode(graph.getView().getViewId());
                        if (graph.getDescendantCount(node) > 0) {
                            parents.add(node);
                        } else if (graph.getParent(node) != null) {
                            parents.add(graph.getParent(node));
                        }
                    }
                    for (Node parent : parents) {
                        graph.ungroupNodes(parent);
                    }
                } catch (Exception e) {
                    graph.readUnlockAll();
                    NotifyDescriptor.Message nd = new NotifyDescriptor.Message(e.getMessage());
                    DialogDisplayer.getDefault().notifyLater(nd);
                }
            }
        }, "Ungroup nodes").start();
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

    private void expandPositioning(Node node) {
        NodeData nodeData = node.getNodeData();
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
