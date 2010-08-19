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
 * Graph that contains both directed and undirected edges.
 * 
 * @author Mathieu Bastian
 * @see GraphModel
 */
public interface MixedGraph extends Graph {

    /**
     * Add to the graph a <b>directed</b> or <b>undirected</b> edge between <code>source</code> and
     * <code>target</code>. Graph does not accept parallel edges.
     * Fails if a such edge already exists in the graph.
     * @param source the source node
     * @param target the target node
     * @param directed the type of edge to be created
     * @return true if add is successful, false otherwise
     * @throws IllegalArgumentException if <code>source</code> or <code>target</code>
     * is <code>null</code> or not legal nodes for this <code>edge</code>
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public boolean addEdge(Node source, Node target, boolean directed);

    /**
     * Finds and returns a <b>directed</b> or <b>undirected</b> edge that connects <code>node1</code> and
     * <code>node2</code>. Returns <code>null</code> if no such edge is found.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node1 the first incident node of the queried edge
     * @param node2 thge second incident node of the queried edge
     * @return an edge that connects <code>node1</code> and <code>node2</code>
     * or <code>null</code> if no such edge exists
     * @throws IllegalArgumentException if <code>node1</code> or <code>node2</code>
     * is <code>null</code> or not legal nodes in the graph
    */
    public Edge getEdge(Node node1, Node node2);

    /**
     * Returns an edge iterator of <b>directed</b> edges in the graph.
     * @return Returns an edge iterator of <b>directed</b> edges in the graph.
     */
    public EdgeIterable getDirectedEdges();

    /**
     * Returns an edge iterator of <b>directed</b> edges in the graph.
     * @return Returns an edge iterator of <b>directed</b> edges in the graph.
     */
    public EdgeIterable getUndirectedEdges();

    /**
     * Returns <code>true</code> if <code>edge</code> is <b>directed</b>  if
     * <b>undirected</b>.
     * @param edge the edge to be queried
     * @return Returns <code>true</code> if <code>edge</code> is <b>directed</b>  if
     * <b>undirected</b>
     * @throws IllegalArgumentException if <code>edge</code> is <code>null</code>
     */
    public boolean isDirected(Edge edge);
}
