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
public interface UndirectedGraph extends Graph {

    /**
     * Add an undirected edge between<code>node1</code> and <code>node2</code>to the graph.
     * Graph does not accept parallel edges.
     * Fails if a such edge already exists in the graph.
     * @param node1 the first node
     * @param node2 the second node
     * @return true if add is successful, false otherwise
     * @throws IllegalArgumentException if <code>source</code> or <code>target</code>
     * is <code>null</code> or not legal nodes for this <code>edge</code>
     */
    public boolean addEdge(Node node1, Node node2);


    /**
     * Find and returns an edge that connects <code>node1</code> and <code>node2</code>. Returns
     * <code>null</code> if no such edge is found.
     * @param node1 the first incident node of the queried edge
     * @param node2 thge second incident node of the queried edge
     * @return an edge that connects <code>node1</code> and <code>node2</code>
     * or <code>null</code> if no such edge exists
    */
    public Edge getEdge(Node node1, Node node2);

}
