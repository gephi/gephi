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
public interface DirectedGraph extends Graph {

    public boolean addEdge(Node source, Node target);

    public Edge getEdge(Node source, Node target);

    public EdgeIterable getInEdges(Node node);

    public EdgeIterable getOutEdges(Node node);
    
    public NodeIterable getSuccessors(Node node);
    
    public NodeIterable getPredecessors(Node node);

    public boolean isSuccessor(Node node, Node successor);

    public boolean isPredecessor(Node node, Node predecessor);

    public int getInDegree(Node node);

    public int getOutDegree(Node node);
}
