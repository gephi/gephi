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

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Main interface for accessing the graph structure and develop algorithms.
 * <p>
 * A graph object belongs to a graph model, which really contains the data. Graph
 * objects are therefore only accessors, with all convinient methods to read and
 * modify the structure. Hence, <b>multiple</b> <code>Graph</code> objects can
 * exists.
 * <h3>Get current graph</h3>
 * <pre>
 * GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
 * GraphModel model = graphController.getModel();
 * Graph graph = model.getGraph();
 * </pre>
 * <b>Note:</b> This shows how to get the complete graph, for getting only the
 * currently visualized graph, call <code>model.getGraphVisible()</code> instead.
 * <h3>Graph Locking</h3>
 * Graph structure possess a locking mechanism that avoids multiple threads
 * to modify the structure concurrently. However, several readers are allowed.
 * <p>
 * There is two different way to perform read locking:
 * <ul><li>Manually by calling <code>readLock()</code> and <code>readUnlock()</code></li>
 * <li>Structure is automatically locked when you read the various <code>NodeIterable</code>
 * and <code>EdgeIterable</code></li></ul>
 * Be always sure you don't leave the structure blocked. use <code>readUnlockAll()</code>
 * to release all the locks.
 * <p>
 * <b>Note:</b> Write locking is automatically done when modifying th graph
 * (add, remove, ...).
 * @author Mathieu Bastian
 */
public interface Graph {

    /**
     * Add <code>edge</code> to the graph. Graph does not accept parallel edges.
     * Fails if <code>edge</code> is already in the graph.
     * @param edge the edge to add
     * @return true if add is successful, false otherwise
     * @throws IllegalArgumentException if <code>edge</code> is <code>null</code>,
     * or if nodes are not legal nodes for this <code>edge</code>,
     * or if <code>edge</code> is directed when the graph is undirected,
     * or if <code>edge</code> is undirected when the graph is directed,
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public boolean addEdge(Edge edge);

    /**
     * Add a node to the graph.
     * Fails if the node already exists.
     * @param node the node to add
     * @return true if add is successful, false otherwise
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code>
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public boolean addNode(Node node);

    /**
     * Remove <code>edge</code> from the graph.
     * Fails if the edge doesn't exist.
     * @param edge the edge to remove
     * @return true if remove is successful, false otherwise
     * @throws IllegalArgumentException if <code>edge</code> is <code>null</code> or nodes not legal in
     * the graph
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public boolean removeEdge(Edge edge);

    /**
     * Remove <code>node</code> from the graph. All <code>node</code>'s children and incident edges will
     * also be removed.
     * @param node the node to remove
     * @return true if remove is successful, false otherwise
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code>
     * or not legal in the graph.
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public boolean removeNode(Node node);

    /**
     * Returns true if the graph contains <code>node</code>.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node whose presence is required
     * @return true if the graph contains <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code>
     */
    public boolean contains(Node node);

    /**
     * Returns true if the graph contains <code>edge</code>.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param edge the edge whose presence is required
     * @return true if the graph contains <code>edge</code>
     * @throws IllegalArgumentException if <code>edge</code> is <code>null</code>
     */
    public boolean contains(Edge edge);

    /**
     * Returns the node with identifier equal to <code>id</code>. If not found,
     * returns <code>null</code>.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param id a positive number
     * @return the node with given <code>id</code>, or <code>null</code> if not found
     */
    public Node getNode(int id);

    /**
     * Returns the edge with identifier equal to <code>id</code>. If not found,
     * returns <code>null</code>.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param id a positive number
     * @return the edge with given <code>id</code>, or <code>null</code> if not found
     */
    public Edge getEdge(int id);

    /**
     * Returns nodes contained in the graph.
     * @return a node iterable of nodes contained in the graph.
     */
    public NodeIterable getNodes();

    /**
     * Returns edges contained in the graph. Self-loops will be present only once.
     * <p>
     * If the graph is <b>undirected</b>, directed mutual edges will be present only once.
     * @return an edge iterable of edges contained in the graph.
     */
    public EdgeIterable getEdges();

    /**
     * Returns neighbors of <code>node</code>. Neighbors are nodes connected to
     * <code>node</code> with any edge of the graph. Neighbors exclude <code>node</code> itself,
     * therefore self-loops are ignored.
     * @param node the node whose neighbors are to be returned
     * @return a node iterable of <code>node</code>'s neighbors
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code>
     * or not legal in the graph.
     */
    public NodeIterable getNeighbors(Node node);

    /**
     * Returns edges incident to <code>node</code>.
     * <p>
     * For <b>directed</b> graph, note that self-loops are repeated only once. <b>Undirected</b>
     * graphs repeats edges once by default.
     * @param node the node whose incident edges are to be returned
     * @return an edge iterable of edges incident to <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code>
     * or not legal in the graph.
     */
    public EdgeIterable getEdges(Node node);

    /**
     * Returns the number of nodes in the graph.
     * <p>
     * Special case of interest:
     * <ul><li>For <b>hierarchical</b> graph, returns the number of nodes in the
     * current view only.</li>
     * </ul>
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @return the number of nodes in the graph
     */
    public int getNodeCount();

    /**
     * Returns the number of edges in the graph
     * <p>
     * Special case of interest:
     * <ul><li>Count self-loops once only.</li>
     * <li>For <b>hierarchical</b> graph, count edges incident only to nodes
     * in the current view.</li></ul>
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @return the number of edges in the graph.
     */
    public int getEdgeCount();

    /**
     * Return the current node version of the graph. When node structure is touched (i.e. add, remove...) the
     * node version is <b>incremented</b>.
     * <p>External modules can compare local copy of version to determine if update
     * is necessary.
     * @return the current node version of the graph
     */
    public int getNodeVersion();

    /**
     * Return the current edge version of the graph. When edge structure is touched (i.e. add, remove...) the
     * edge version is <b>incremented</b>.
     * <p>External modules can compare local copy of version to determine if update
     * is necessary.
     * @return the current edge version of the graph
     */
    public int getEdgeVersion();

    /**
     * Returns the adjacent node of <code>node</code> through <code>edge</code>.
     * @param node the node whose adjacent node is to be returned
     * @param edge the edge whose the opposite node is to be returned
     * @return the adjacent node of <code>node</code> through <code>edge</code>
     * @throws IllegalArgumentException if <code>node</code> or <code>edge</code> is <code>null</code>,
     * or if <code>node</code> is not incident to <code>edge</code>
     */
    public Node getOpposite(Node node, Edge edge);

    /**
     * Returns the degree of <code>node</code>. Self-loops are counted twice for <b>directed</b> graphs.
     * @param node the node whose degree is to be returned
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @return the degree of <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in the graph
     */
    public int getDegree(Node node);

    /**
     * Returns <code>true</code> if <code>edge</code> is a self-loop.
     * @param edge the edge to be queried
     * @return <code>true</code> if <code>edge</code> is a self-loop, <code>false</code> otherwise
     * @throws IllegalArgumentException if <code>edge</code> is <code>null</code> or adjacent nodes not
     * legal in the graph
     */
    public boolean isSelfLoop(Edge edge);

    /**
     * Returns <code>true</code> if <code>edge</code> is a directed edge in the current graph. Always
     * returns <code>true</code> when the graph is <b>directed</b> and <code>false</code> when the graph
     * is <b>undirected</b>. In case of a <b>mixed</b> graph returns </code>Edge.isDirected()</code>.
     * @param edge
     * @return <code>true</code> is <code>edge</code> is directed
     * @throws IllegalArgumentException if <code>edge</code> is <code>null</code> or adjacent nodes not
     * legal in the graph
     */
    public boolean isDirected(Edge edge);

    /**
     * Returns <code>true</code> if <code>node1</code> is adjacent to <code>node2</code>. Is adjacent
     * when an edge exists between <code>node1</code> and <code>node2</code>.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node1 the first node to be queried
     * @param node2 the seconde node to be queried
     * @return <code>true</code> if <code>node1</code> is adjacent to <code>node2</code>
     * @throws IllegalArgumentException if <code>node1</code> or <code>node2</code> is <code>null</code> of
     * not legal in the graph
     */
    public boolean isAdjacent(Node node1, Node node2);

    /**
     * Returns <code>true</code> if <code>edge1</code> is adjacent to <code>edge2</code>. Is adjacent
     * when an node is incident to both edges.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param edge1 the first node to be queried
     * @param edge2 the seconde node to be queried
     * @return <code>true</code> if <code>edge1</code> is adjacent to <code>edge2</code>
     * @throws IllegalArgumentException if <code>edge1</code> or <code>edge2</code> is <code>null</code> of
     * not legal in the graph
     */
    public boolean isAdjacent(Edge edge1, Edge edge2);

    /**
     * Removes all edges incident to <code>node</code>.
     * @param node the node whose edges is to be cleared
     * @throws IllegalArgumentException if <code>node</code> if null or not legal in the graph
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public void clearEdges(Node node);

    /**
     * Removes all nodes and edges in the graph.
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public void clear();

    /**
     * Removes all edges in the graph.
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public void clearEdges();

    /**
     * Acquires a read lock on the graph. Calling thread will be blocked until all write locks are released.
     * Several threads can read but only once can write.
     * <p>
     * A read lock can be acquired several times by a thread, but be sure to call <code>readUnlock()</code>
     * the same number of time you called this method, or use <code>readUnlockAll()</code> to release all
     * locks.
     * @see ReentrantReadWriteLock
     */
    public void readLock();

    /**
     * Releases the read lock on the graph. Must be called from the same thread that locked the graph.
     * <p>
     * Use <code>readUnlockAll()</code> if you ignore the number of times the read lock has been acquired.
     */
    public void readUnlock();

    /**
     * Safe method that releases all read locks the calling thread has acquired. Use this method if you are
     * cancelling a task and you don't know how many read locks have been acquired.
     */
    public void readUnlockAll();

    /**
     * Acquires a write lock on the graph. Calling thread will be blocked until all read locks are released.
     * Several threads can read but only once can write.
     * @see ReentrantReadWriteLock
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public void writeLock();

    /**
     * Release the write lock on the graph. Must be called from the same thread that locked the graph.
     */
    public void writeUnlock();

    /**
     * Returns the graph model this graph belongs to.
     * @return the graph model this graph belongs to.
     */
    public GraphModel getGraphModel();

    /**
     * Returns the graph view this graph belongs to.
     * @return the graph view this graph belongs to.
     */
    public GraphView getView();
}
