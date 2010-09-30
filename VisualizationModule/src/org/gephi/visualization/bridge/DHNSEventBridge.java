/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.visualization.bridge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.desktop.project.api.ProjectControllerUI;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GroupData;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Model;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.apiimpl.GraphIO;
import org.gephi.visualization.apiimpl.ModelImpl;
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

    //GROUPING
    public boolean canExpand() {
        GraphModel graphModel = graphController.getModel();
        if (graphModel == null) {
            return false;
        }
        this.graph = graphController.getModel().getHierarchicalGraphVisible();
        ModelImpl[] selectedNodeModels = engine.getSelectedObjects(AbstractEngine.CLASS_NODE);
        for (ModelImpl metaModelImpl : selectedNodeModels) {
            NodeData nodeData = (NodeData) metaModelImpl.getObj();
            Node node = nodeData.getNode(graph.getView().getViewId());
            if (node != null && graph.getDescendantCount(node) > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean canContract() {
        GraphModel graphModel = graphController.getModel();
        if (graphModel == null) {
            return false;
        }
        this.graph = graphController.getModel().getHierarchicalGraphVisible();
        ModelImpl[] selectedNodeModels = engine.getSelectedObjects(AbstractEngine.CLASS_NODE);
        for (ModelImpl metaModelImpl : selectedNodeModels) {
            NodeData nodeData = (NodeData) metaModelImpl.getObj();
            Node node = nodeData.getNode(graph.getView().getViewId());
            if (node != null && graph.getParent(node) != null) {
                return true;
            }
        }
        return false;
    }

    public boolean canGroup() {
        GraphModel graphModel = graphController.getModel();
        if (graphModel == null) {
            return false;
        }
        this.graph = graphController.getModel().getHierarchicalGraphVisible();
        ModelImpl[] selectedNodeModels = engine.getSelectedObjects(AbstractEngine.CLASS_NODE);
        int nodesReallyPresent = 0;
        for (ModelImpl metaModelImpl : selectedNodeModels) {
            NodeData nodeData = (NodeData) metaModelImpl.getObj();
            Node node = nodeData.getNode(graph.getView().getViewId());
            if (node != null) {
                nodesReallyPresent++;
            }
        }
        return nodesReallyPresent >= 1;
    }

    public boolean canUngroup() {
        GraphModel graphModel = graphController.getModel();
        if (graphModel == null) {
            return false;
        }
        ModelImpl[] selectedNodeModels = engine.getSelectedObjects(AbstractEngine.CLASS_NODE);
        return selectedNodeModels.length >= 1;
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
                        if (node != null && graph.getDescendantCount(node) > 0) {
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
                        if (node != null) {
                            Node nodeParent = graph.getParent(node);
                            if (nodeParent != null) {
                                parents.add(nodeParent);
                            }
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
        final List<Node> newGroup = new ArrayList<Node>();
        for (int i = 0; i < selectedNodeModels.length; i++) {
            Node node = ((NodeData) selectedNodeModels[i].getObj()).getNode(graph.getView().getViewId());
            if (node != null) {
                newGroup.add(node);
            }
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
                    Node group = graph.groupNodes(newGroup.toArray(new Node[0]));
                    group.getNodeData().setLabel("Group (" + newGroup.size() + " nodes)");
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
                        if (node != null && graph.getDescendantCount(node) > 0) {
                            parents.add(node);
                        } else if (node != null && graph.getParent(node) != null) {
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

    //SETTLE AND FREE
    public boolean canSettle() {
        GraphModel graphModel = graphController.getModel();
        if (graphModel == null) {
            return false;
        }
        this.graph = graphController.getModel().getHierarchicalGraphVisible();
        ModelImpl[] selectedNodeModels = engine.getSelectedObjects(AbstractEngine.CLASS_NODE);
        for (ModelImpl metaModelImpl : selectedNodeModels) {
            NodeData nodeData = (NodeData) metaModelImpl.getObj();
            if (!nodeData.isFixed()) {
                return true;
            }
        }
        return false;
    }

    public boolean canFree() {
        GraphModel graphModel = graphController.getModel();
        if (graphModel == null) {
            return false;
        }
        this.graph = graphController.getModel().getHierarchicalGraphVisible();
        ModelImpl[] selectedNodeModels = engine.getSelectedObjects(AbstractEngine.CLASS_NODE);
        for (ModelImpl metaModelImpl : selectedNodeModels) {
            NodeData nodeData = (NodeData) metaModelImpl.getObj();
            if (nodeData.isFixed()) {
                return true;
            }
        }
        return false;
    }

    public void settle() {
        this.graph = graphController.getModel().getHierarchicalGraphVisible();
        ModelImpl[] selectedNodeModels = engine.getSelectedObjects(AbstractEngine.CLASS_NODE);
        for (ModelImpl metaModelImpl : selectedNodeModels) {
            NodeData nodeData = (NodeData) metaModelImpl.getObj();
            nodeData.setFixed(true);
        }
    }

    public void free() {
        this.graph = graphController.getModel().getHierarchicalGraphVisible();
        ModelImpl[] selectedNodeModels = engine.getSelectedObjects(AbstractEngine.CLASS_NODE);
        for (ModelImpl metaModelImpl : selectedNodeModels) {
            NodeData nodeData = (NodeData) metaModelImpl.getObj();
            nodeData.setFixed(false);
        }
    }

    //DELETE
    public boolean canDelete() {
        GraphModel graphModel = graphController.getModel();
        if (graphModel == null) {
            return false;
        }
        this.graph = graphController.getModel().getHierarchicalGraphVisible();
        ModelImpl[] selectedNodeModels = engine.getSelectedObjects(AbstractEngine.CLASS_NODE);
        int nodesReallyPresent = 0;
        for (ModelImpl metaModelImpl : selectedNodeModels) {
            NodeData nodeData = (NodeData) metaModelImpl.getObj();
            Node node = nodeData.getNode(graph.getView().getViewId());
            if (node != null) {
                nodesReallyPresent++;
            }
        }
        return nodesReallyPresent >= 1;
    }

    public void delete() {
        this.graph = graphController.getModel().getHierarchicalGraph();
        ModelImpl[] selectedNodeModels = engine.getSelectedObjects(AbstractEngine.CLASS_NODE);
        for (ModelImpl model : selectedNodeModels) {
            NodeData nodeData = (NodeData) model.getObj();
            Node node = nodeData.getRootNode();
            if (node != null) {
                graph.removeNode(node);
            }
        }
    }

    //MOVE & COPY WORKSPACE
    public boolean canMoveOrCopyWorkspace() {
        GraphModel graphModel = graphController.getModel();
        if (graphModel == null) {
            return false;
        }
        ModelImpl[] selectedNodeModels = engine.getSelectedObjects(AbstractEngine.CLASS_NODE);
        int nodesReallyPresent = 0;
        for (ModelImpl metaModelImpl : selectedNodeModels) {
            NodeData nodeData = (NodeData) metaModelImpl.getObj();
            Node node = nodeData.getNode(graph.getView().getViewId());
            if (node != null) {
                nodesReallyPresent++;
            }
        }
        return nodesReallyPresent >= 1;
    }

    public void moveToWorkspace(Workspace workspace) {
        copyToWorkspace(workspace);
        delete();
    }

    public void moveToNewWorkspace() {
        Workspace workspace = Lookup.getDefault().lookup(ProjectControllerUI.class).newWorkspace();
        moveToWorkspace(workspace);
    }

    public void copyToWorkspace(Workspace workspace) {
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);

        Workspace currentWorkspace = projectController.getCurrentWorkspace();
        AttributeModel sourceAttributeModel = attributeController.getModel(currentWorkspace);
        AttributeModel destAttributeModel = attributeController.getModel(workspace);
        destAttributeModel.mergeModel(sourceAttributeModel);

        GraphModel sourceModel = graphController.getModel(currentWorkspace);
        GraphModel destModel = graphController.getModel(workspace);
        Graph destGraph = destModel.getHierarchicalGraphVisible();
        Graph sourceGraph = sourceModel.getHierarchicalGraphVisible();

        ModelImpl[] selectedNodeModels = engine.getSelectedObjects(AbstractEngine.CLASS_NODE);
        List<Node> nodes = new ArrayList<Node>();
        for (ModelImpl metaModelImpl : selectedNodeModels) {
            NodeData nodeData = (NodeData) metaModelImpl.getObj();
            Node node = nodeData.getNode(sourceGraph.getView().getViewId());
            if (node != null && destGraph.getNode(node.getNodeData().getId()) == null) {
                nodes.add(node);
            }
        }

        destModel.pushNodes(sourceGraph, nodes.toArray(new Node[0]));
    }

    public void copyToNewWorkspace() {
        Workspace workspace = Lookup.getDefault().lookup(ProjectControllerUI.class).newWorkspace();
        copyToWorkspace(workspace);
    }

    public void mouseClick(ModelClass objectClass, Model[] clickedObjects) {
    }
}
