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
package org.gephi.data.network.edge;

import java.util.concurrent.atomic.AtomicInteger;
import org.gephi.data.network.api.EdgeWrap;
import org.gephi.data.network.node.PreNode;
import org.gephi.datastructure.avl.simple.AVLItem;
import org.gephi.graph.api.Edge;

/**
 *
 * @author Mathieu Bastian
 */
public class PreEdge implements AVLItem, EdgeWrap {

    private static AtomicInteger AUTO_ID = new AtomicInteger();
    public enum EdgeType {

        IN(1),
        OUT(2);
        public final int id;

        EdgeType(int id) {
            this.id = id;
        }
    }
    public PreNode minNode;
    public PreNode maxNode;
    public EdgeType edgeType;
    public int cardinal = 1;
    public int ID = PreEdge.AUTO_ID.incrementAndGet();

    private Edge edge;

    public PreEdge(EdgeType edgeType, PreNode minNode, PreNode maxNode) {
        this.minNode = minNode;
        this.maxNode = maxNode;
        this.edgeType = edgeType;
    }

    public PreEdge(PreNode source, PreNode target) {
        if (source.getPre() > target.getPre()) {
            this.minNode = target;
            this.maxNode = source;
            this.edgeType = EdgeType.IN;
        }
        else
        {
            this.minNode = source;
            this.maxNode = target;
            this.edgeType = EdgeType.OUT;
        }
    }

    @Override
    public int getNumber() {
        return ID;
    }

    public Edge getEdge() {
        return edge;
    }

    public void setEdge(Edge edge)
    {
        this.edge = edge;
    }
}
