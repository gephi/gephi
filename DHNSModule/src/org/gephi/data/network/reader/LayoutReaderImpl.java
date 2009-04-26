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
package org.gephi.data.network.reader;

import java.util.Iterator;
import java.util.List;
import org.gephi.data.network.Dhns;
import org.gephi.data.network.api.LayoutReader;
import org.gephi.data.network.edge.DhnsEdge;
import org.gephi.data.network.edge.EdgeImpl;
import org.gephi.data.network.node.NodeImpl;
import org.gephi.data.network.node.PreNode;
import org.gephi.data.network.sight.SightCache;
import org.gephi.data.network.sight.SightImpl;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeLayoutInterface;
import org.gephi.graph.api.NodeLayoutInterface;
import org.gephi.graph.api.LayoutDataFactory;

/**
 *
 * @author Mathieu Bastian
 */
public class LayoutReaderImpl<T extends NodeLayoutInterface, U extends EdgeLayoutInterface> implements LayoutReader {

    private Dhns dhns;
    private boolean locked = false;
    private NodeLayoutIterable nodeIterable1;
    private NodeLayoutIterable nodeIterable2;
    private EdgeIterable edgeIterable;

    public LayoutReaderImpl(Dhns dhns, SightImpl sight,LayoutDataFactory factory) {
        nodeIterable1 = new NodeLayoutIterable(sight,factory);
        nodeIterable2 = new NodeLayoutIterable(sight,factory);
        edgeIterable = new EdgeIterable(sight,factory);
    }

    public void lock() {
        if (!locked) {
            dhns.getReadLock().lock();
            locked = true;
        }
    }

    public void unlock() {
        if (locked) {
            dhns.getReadLock().unlock();
            locked = false;
        }
    }

    public Iterable<T> getNodes() {
        if (nodeIterable1.inUse) {
            nodeIterable2.reset();
            return nodeIterable2;
        } else {
            nodeIterable1.reset();
            return nodeIterable1;
        }
    }

    public Iterable<Edge> getEdges() {
        edgeIterable.reset();
        return edgeIterable;
    }

    private static class NodeLayoutIterable<T extends NodeLayoutInterface> implements Iterable<T> {

        private Iterator<T> iterator;
        private SightCache cache;
        private List<PreNode> nodes;
        private int index = -1;
        private boolean inUse = false;

        public NodeLayoutIterable(SightImpl sight,final LayoutDataFactory factory) {
            cache = sight.getSightCache();
            iterator = new Iterator<T>() {

                public boolean hasNext() {
                    boolean res = ++index < nodes.size();
                    if (!res) {
                        inUse = false;
                    }
                    return res;
                }

                public T next() {
                    NodeImpl ni =  nodes.get(index).getNode();
                    if(ni.getNodeLayout()==null)
                    {
                        ni.setNodeLayout(factory.getNodeLayout(ni));
                    }
                    return (T)ni.getNodeLayout();
                }

                public void remove() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
        }

        public Iterator<T> iterator() {
            return iterator;
        }

        public void reset() {
            nodes = cache.getCacheContent().getNodeCache();
            inUse = true;
        }
    }

    private static class EdgeIterable<U extends EdgeLayoutInterface> implements Iterable<U> {

        private Iterator<U> iterator;
        private SightCache cache;
        private List<DhnsEdge> edges;
        private int index = -1;

        public EdgeIterable(SightImpl sight,final LayoutDataFactory factory) {
            cache = sight.getSightCache();
            iterator = new Iterator<U>() {

                public boolean hasNext() {
                    return ++index < edges.size();
                }

                public U next() {
                    EdgeImpl edge = edges.get(index).getEdge();
                    if(edge.getEdgeLayout()==null)
                    {
                        edge.setEdgeLayout(factory.getEdgeLayout(edge));
                    }
                    return (U)edge.getEdgeLayout();
                }

                public void remove() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
        }

        public Iterator<U> iterator() {
            return iterator;
        }

        public void reset() {
            edges = cache.getCacheContent().getEdgeCache();
        }
    }
}
