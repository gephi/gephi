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
package org.gephi.graph.api;

/**
 *
 * @author Mathieu Bastian
 */
public interface Graph {

    public void addEdge(Edge edge);

    public void addNode(Node node);

    public void removeEdge(Edge edge);

    public void removeNode(Node node);

    public boolean contains(Node node);

    public boolean contains(Edge edge);

    public NodeIterable getNodes();

    public EdgeIterable getEdges();

    public NodeIterable getNeigbors(Node node);

    public EdgeIterable getEdges(Node node);

    public int getNodeCount();

    public int getEdgeCount();

    public int getNodeVersion();

    public int getEdgeVersion();

    public Node getOpposite(Node node, Edge edge);

    public int getDegree(Node node);

    public boolean isSelfLoop(Edge edge);

    public boolean isNeighbor(Node node, Node neighbor);

    public void clearEdges(Node node);

    public void clear();

    public void clearEdges();

    public void readLock();

    public void readUnlock();

    public void writeLock();

    public void writeUnlock();
}
