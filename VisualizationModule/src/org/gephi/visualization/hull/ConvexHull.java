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
package org.gephi.visualization.hull;

import org.gephi.datastructure.avl.param.AVLItemAccessor;
import org.gephi.datastructure.avl.param.ParamAVLTree;
import org.gephi.graph.api.Model;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.graph.api.Renderable;
import org.gephi.graph.api.TextData;
import org.gephi.visualization.api.ModelImpl;

/**
 *
 * @author Mathieu Bastian
 */
public class ConvexHull implements Renderable {

    private Node metaNode;
    private ParamAVLTree<Node> groupNodesTree;
    private ModelImpl[] hullNodes;
    private float alpha = 1f;
    private Model model;
    private float baryX;
    private float baryY;
    private float baryZ;

    public ConvexHull() {
        groupNodesTree = new ParamAVLTree<Node>(new AVLItemAccessor<Node>() {

            public int getNumber(Node item) {
                return item.getId();
            }
        });
    }

    public void addNode(Node node) {
        groupNodesTree.add(node);
    }

    public void setMetaNode(Node metaNode) {
        this.metaNode = metaNode;
    }

    public ModelImpl[] getNodes() {
        return hullNodes;
    }

    public Node[] getGroupNodes() {
        return groupNodesTree.toArray(new Node[0]);
    }

    private ModelImpl[] computeHull(NodeData[] nodes) {
        NodeData[] n = AlgoHull.calculate(nodes);
        ModelImpl[] models = new ModelImpl[n.length];
        for (int i = 0; i < n.length; i++) {
            models[i] = (ModelImpl) n[i].getModel();
        }
        return models;
    }

    public void recompute() {
        //this.hullNodes = computeHull(groupNodes);
    }

    public void setX(float x) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setY(float y) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setZ(float z) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getRadius() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getSize() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSize(float size) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float r() {
        return 0.4f;
    //return metaNode.getNodeData().r();
    }

    public float g() {
        return 0f;
    //return metaNode.getNodeData().g();
    }

    public float b() {
        return 0.3f;
    //return metaNode.getNodeData().b();
    }

    public void setR(float r) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setG(float g) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setB(float b) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float alpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model obj) {
        this.model = obj;
    }

    public TextData getTextData() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setTextData(TextData textData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float x() {
        return baryX;
    }

    public float y() {
        return baryY;
    }

    public float z() {
        return baryZ;
    }
}
