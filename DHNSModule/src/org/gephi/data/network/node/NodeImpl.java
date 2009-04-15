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
import org.gephi.graph.api.Sight;
import org.gephi.data.network.edge.DhnsEdge;
import org.gephi.data.network.utils.avl.DhnsEdgeTree;
import org.gephi.datastructure.avl.param.MultiParamAVLIterator;
import org.gephi.graph.api.Node;
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

    public NodeImpl() {
        x = ((float) Math.random()) * 2000 - 1000.0f;
        y = ((float) Math.random()) * 2000 - 1000.0f;
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
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setZ(float z) {
        this.z = z;
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

    public PreNode getPreNode() {
        return preNode;
    }

    public void setPreNode(PreNode preNode) {
        this.preNode = preNode;
    }

    public int getLevel() {
        return preNode.level;
    }

    public int getInDegree(Sight sight) {
        return preNode.getVirtualEdgesIN(sight).getCount();
    }

    public int getOutDegree(Sight sight) {
        return preNode.getVirtualEdgesOUT(sight).getCount();
    }

    public Iterable<? extends EdgeWrap> getEdgesOut(Sight sight) {
        return preNode.getVirtualEdgesOUT(sight);
    }

    public Iterable<? extends EdgeWrap> getEdgesIn(Sight sight) {
        return preNode.getVirtualEdgesIN(sight);
    }

    public Iterable<? extends EdgeWrap> getEdges(Sight sight) {
        return new InOutEdgesIterable(preNode.getVirtualEdgesIN(sight), preNode.getVirtualEdgesOUT(sight));
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

        public Iterator iterator() {
            return new MultiParamAVLIterator(tree1, tree2);
        }
    }
}
