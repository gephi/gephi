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
package org.gephi.graph.dhns.graph;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.View;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.core.GraphStructure;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.MetaEdgeImpl;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.views.ViewImpl;

/**
 * Utilities methods for managing graphs implementation like locking of non-null checking
 *
 * @author Mathieu Bastian
 */
public abstract class AbstractGraphImpl {

    protected Dhns dhns;
    protected GraphStructure structure;
    protected ViewImpl view;

    public View getView() {
        return view;
    }

    public GraphModel getGraphModel() {
        return dhns;
    }

    public void readLock() {
        //System.out.println(Thread.currentThread()+ "read lock");
        dhns.getReadLock().lock();
    }

    public void readUnlock() {
        //System.out.println(Thread.currentThread()+ "read unlock");
        dhns.getReadLock().unlock();
    }

    public void writeLock() {
        //System.out.println(Thread.currentThread()+ "write lock");
        dhns.getWriteLock().lock();
    }

    public void writeUnlock() {
        //System.out.println(Thread.currentThread()+ "write lock");
        dhns.getWriteLock().unlock();
    }

    public int getNodeVersion() {
        return dhns.getGraphVersion().getNodeVersion();
    }

    public int getEdgeVersion() {
        return dhns.getGraphVersion().getEdgeVersion();
    }

    protected AbstractNode checkNode(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("node can't be null");
        }
        AbstractNode preNode = (AbstractNode) node;
        if (!preNode.isValid() || preNode.avlNode.getList() != structure.getStructure().getTree()) {
            //Try to find it in this dictionnary
            preNode = structure.getNodeDictionnary().get(preNode.getId());
            if (preNode != null) {
                preNode = preNode.getOriginalNode();
            } else {
                throw new IllegalArgumentException("Node must be in the graph");
            }
        }
        return preNode;
    }

    protected AbstractEdge checkEdge(Edge edge) {
        if (edge == null) {
            throw new IllegalArgumentException("edge can't be null");
        }
        AbstractEdge abstractEdge = (AbstractEdge) edge;
        if (!abstractEdge.isValid() || abstractEdge.getSource().avlNode.getList() != structure.getStructure().getTree()) {
            //Try to find it in the dictionnary
            abstractEdge = structure.getEdgeDictionnary().get(abstractEdge.getId());
            if (abstractEdge == null) {
                throw new IllegalArgumentException("Nodes must be in the graph");
            }
        }
        if (abstractEdge.isMetaEdge()) {
            throw new IllegalArgumentException("Edge can't be a meta edge");
        }
        return abstractEdge;
    }

    protected MetaEdgeImpl checkMetaEdge(Edge edge) {
        if (edge == null) {
            throw new IllegalArgumentException("edge can't be null");
        }
        AbstractEdge absEdge = (AbstractEdge) edge;
        if (!absEdge.isMetaEdge()) {
            throw new IllegalArgumentException("edge must be a meta edge");
        }
        if (!absEdge.isValid()) {
            throw new IllegalArgumentException("Nodes must be in the graph");
        }
        return (MetaEdgeImpl) absEdge;
    }

    protected boolean checkEdgeExist(AbstractNode source, AbstractNode target) {
        return source.getEdgesOutTree().hasNeighbour(target);
    }

    protected AbstractEdge getSymmetricEdge(AbstractEdge edge) {
        return edge.getTarget().getEdgesOutTree().getItem(edge.getSource().getNumber());
    }
}
