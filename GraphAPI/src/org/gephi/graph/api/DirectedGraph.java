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

    /**
     * Add an edge between <code>source</code> and <code>target</code> to the graph.
     * Graph does not accept parallel edges.
     * Fails if a such edge already exists in the graph.
     * @param source the source node
     * @param target the target node
     * @return true if add is successful, false otherwise
     * @throws IllegalArgumentException if <code>source</code> or <code>target</code>
     * is <code>null</code> or not legal nodes for this <code>edge</code>
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public boolean addEdge(Node source, Node target);

    /**
     * Find and returns an edge that connects <code>source</code> and <code>target</code>. Returns
     * <code>null</code> if no such edge is found.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param source the source node of the queried edge
     * @param target the target node of the queried edge
     * @return a directed edge that connects <code>source</code> and <code>target</code>
     * or <code>null</code> if no such edge exists
     * @throws IllegalArgumentException if <code>source</code> or <code>target</code>
     * is <code>null</code> or not legal nodes in the graph
     */
    public Edge getEdge(Node source, Node target);

    /**
     * Returns incoming edges incident to <code>node</code>.
     * @param node the node whose incoming edges are to be returned
     * @return an edge iterable of incoming edges incident to <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code>,
     * or if <code>node</code> is not legal in the graph.
     */
    public EdgeIterable getInEdges(Node node);

    /**
     * Returns outgoing edges incident to <code>node</code>.
     * @param node the node whose outgoing edges are to be returned
     * @return an edge iterable of outgoing edges incident to <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code>,
     * or if <code>node</code> is not legal in the graph.
     */
    public EdgeIterable getOutEdges(Node node);

    /**
     * Returns <code>node</code>'s successors.
     * A successor of <code>node</code> is a node which is connected to <code>node</code>
     * by an outgoing edge going from <code>node</code>.
     * @param node the node whose successors are to be returned
     * @return a node iterable of <code>node</code>'s successors
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code>,
     * or if <code>node</code> is not legal in the graph.
     */
    public NodeIterable getSuccessors(Node node);

    /**
     * Returns <code>node</code>'s predecessors.
     * A predecessor of <code>node</code> is a node which is connected to <code>node</code>
     * by an incoming edge going to <code>node</code>.
     * @param node the node whose predecessors are to be returned
     * @return a node iterable of <code>node</code>'s successors
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code>,
     * or if <code>node</code> is not legal in the graph.
     */
    public NodeIterable getPredecessors(Node node);

    /**
     * Returns <code>true</code> if <code>successor</code> is a successor of <code>node</code>.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node which has <code>successor</code> as a successor
     * @param successor the node which has <code>node</code> as a predecessor
     * @return <code>true</code> if <code>successor</code> is a successor of <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> or <code>successor</code> is
     * <code>null</code> of not legal in the graph
     */
    public boolean isSuccessor(Node node, Node successor);

    /**
     * Returns <code>true</code> if <code>predecessor</code> is a predecessor of <code>node</code>.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node which has <code>predecessor</code> as a predecessor
     * @param predecessor the node which has <code>node</code> as a successor
     * @return <code>true</code> if <code>predecessor</code> is a predecessor of <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> or <code>predecessor</code> is
     * <code>null</code> of not legal in the graph
     */
    public boolean isPredecessor(Node node, Node predecessor);

    /**
     * Returns the number of incoming edges incident to <code>node</code>.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node whose indegree is to be returned
     * @return the number of incoming edges incident to <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> of not legal in
     * the graph
     */
    public int getInDegree(Node node);

    /**
     * Returns the number of outgoing edges incident to <code>node</code>.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node whose outdegree is to be returned
     * @return the number of outgoing edges incident to <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> of not legal in
     * the graph
     */
    public int getOutDegree(Node node);
}
