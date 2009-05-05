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
package org.gephi.data.network.node;

import java.util.Iterator;
import org.gephi.graph.api.EdgeWrap;
import org.gephi.graph.api.NodeLayoutInterface;
import org.gephi.data.network.edge.DhnsEdge;
import org.gephi.data.network.edge.EdgeImpl;
import org.gephi.data.network.utils.avl.DhnsEdgeTree;
import org.gephi.data.network.utils.avl.NeighbourIterator;
import org.gephi.datastructure.avl.param.MultiParamAVLIterator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeWrap;
import org.gephi.graph.api.Object3d;

/**
 *
 * @author Mathieu Bastian
 */
public class NodeImpl implements Node {

    protected float x;
    protected float y;
    protected float z;
    protected String label = "";
    protected float r = 0f;
    protected float g = 0f;
    protected float b = 0f;
    protected float alpha = 1f;
    protected float size = 1f;
    protected Object3d obj;

    //Impl
    protected PreNode preNode;
    protected NodeLayoutInterface nodeLayout;

    public NodeImpl() {
        x = ((float) Math.random()) * 2000 - 1000.0f;
        y = ((float) Math.random()) * 2000 - 1000.0f;
        r = (float) Math.random();
        g = (float) Math.random();
        b = (float) Math.random();
        //size= 10f;
        size = ((float) Math.random()) * 20 + 10;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float z() {
        return z;
    }

    public void setX(float x) {
        this.x = x;
        updatePositionFlag();
    }

    public void setY(float y) {
        this.y = y;
        updatePositionFlag();
    }

    public void setZ(float z) {
        this.z = z;
        updatePositionFlag();
    }

    private void updatePositionFlag() {
        if (obj != null) {
            obj.updatePositionFlag();
            preNode.touchPotatoes();
        }
    }

    public float getRadius() {
        return size;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public float r() {
        return r;
    }

    public float g() {
        return g;
    }

    public float b() {
        return b;
    }

    public void setR(float r) {
        this.r = r;
    }

    public void setG(float g) {
        this.g = g;
    }

    public void setB(float b) {
        this.b = b;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public float alpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public Object3d getObject3d() {
        return obj;
    }

    public void setObject3d(Object3d obj) {
        this.obj = obj;
    }

    public int getIndex() {
        return preNode.getPre();
    }

    public String getLabel() {
        return label;
    }

    public PreNode getPreNode() {
        return preNode;
    }

    public void setPreNode(PreNode preNode) {
        this.preNode = preNode;
    }

    public int getLevel() {
        return preNode.level;
    }

    public NodeLayoutInterface getNodeLayout() {
        return nodeLayout;
    }

    public void setNodeLayout(NodeLayoutInterface nodeLayout) {
        this.nodeLayout = nodeLayout;
    }

    public int getInDegree() {
        return preNode.countDhnsEdgeIN();
    }

    public int getOutDegree() {
        return preNode.countDhnsEdgeOUT();
    }

    public Iterable<? extends EdgeWrap> getEdgesOut() {
        return preNode.getDhnsEdgesOUT();
    }

    public Iterable<? extends EdgeWrap> getEdgesIn() {
        return preNode.getDhnsEdgesIN();
    }

    public Iterable<? extends EdgeWrap> getEdges() {
        return new InOutEdgesIterable(preNode.getDhnsEdgesIN(), preNode.getDhnsEdgesOUT());
    }

    public boolean containsEdge(Edge edge) {
        DhnsEdge dhnsEdge = ((EdgeImpl) edge).getDhnsEdge();
        boolean in = preNode.getDhnsEdgesIN().contains(dhnsEdge);
        boolean out = preNode.getDhnsEdgesOUT().contains(dhnsEdge);
        return in | out;
    }

    public boolean hasNeighbour(Node node) {
        NodeImpl potentialNeighbour = (NodeImpl) node;
        boolean in = preNode.getDhnsEdgesIN().hasNeighbour(potentialNeighbour.getPreNode());
        boolean out = preNode.getDhnsEdgesOUT().hasNeighbour(potentialNeighbour.getPreNode());
        return in | out;
    }

    public Iterable<? extends NodeWrap> getNeighbours() {
        return new InOutNeighboursIterable(preNode.getDhnsEdgesIN(), preNode.getDhnsEdgesOUT(), preNode);
    }

    public int countNeighbours() {
        return getInDegree() + getOutDegree();
    }

    /**
     * Utility Iterable for returning an Iterable instead of an Iterator in getEdges
     */
    private static class InOutEdgesIterable implements Iterable<DhnsEdge> {

        private DhnsEdgeTree tree1;
        private DhnsEdgeTree tree2;

        public InOutEdgesIterable(DhnsEdgeTree tree1, DhnsEdgeTree tree2) {
            this.tree1 = tree1;
            this.tree2 = tree2;
        }

        public Iterator<DhnsEdge> iterator() {
            return new MultiParamAVLIterator<DhnsEdge>(tree1, tree2);
        }
    }

    /**
     * Utility Iterable for returning an Iterable instead of an Iterator in getNeighbours
     */
    private static class InOutNeighboursIterable implements Iterable<PreNode> {

        private DhnsEdgeTree tree1;
        private DhnsEdgeTree tree2;
        private PreNode preNode;

        public InOutNeighboursIterable(DhnsEdgeTree tree1, DhnsEdgeTree tree2, PreNode preNode) {
            this.tree1 = tree1;
            this.tree2 = tree2;
            this.preNode = preNode;
        }

        public Iterator<PreNode> iterator() {
            return new NeighbourIterator(new MultiParamAVLIterator<DhnsEdge>(tree1, tree2), preNode);
        }
    }
}
