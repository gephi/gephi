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
public interface ClusteredGraph extends HierarchicalGraph {

    /**
     * Expands the graph view from <code>node</code> to its children. The children of <code>node</code> are put in the view and
     * <code>node</code> is pulled off. Fails if <code>node</code> is not currently in the view or if <code>node</code> don't
     * have any children.
     * <p>
     * Meta edges are automatically updated.
     * @param node the node to be expanded
     * @return <code>true</code> if the expand succeed or <code>false</code> if not
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in the graph
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public boolean expand(Node node);

    /**
     * Retracts the graph view from <code>node</code>'s children to <code>node</code>. The children of <code>node</code> are pulled
     * off the view and <code>node</code> is added to the view. Fails if <code>node</code> is already in the view of if <code>node</code>
     * don't have any children.
     * <p>
     * Meta edges are automatically updated.
     * @param node the nodes' parent to be retracted
     * @return <code>true</code> if the expand succeed or <code>false</code> if not
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in the graph
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public boolean retract(Node node);

    /**
     * Returns true if <code>node</code> is currently in the graph view.
     * @param node the node to be queried
     * @return <code>true</code> if <code>node</code> is in the view, <code>false</code> otherwise
     * @throws IllegalArgumentException if <code>nodeGroup</code> is <code>null</code> or not legal in
     * the graph
     */
    public boolean isInView(Node node);

    /**
     * Reset the current view to leaves of the clustered graph tree. Therefore the <code>getNodes()</code>
     * method will return only these leaves.
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public void resetViewToLeaves();


    public void resetViewToTopNodes();

    public void resetViewToLevel(int level);

    /**
     * Returns meta edges for the whole graph. Meta edges are edges between a group and a leaf
     * or between two groups. They represents proper edges between descendants of groups. Meta
     * edges are always located only on nodes which are in the current view.
     * <p>
     * <b>Example:</b>
     * In a clustered graph, let's define <code>group1</code> and <code>group2</code>, both with
     * two leaves as children. Leaves are named <code>l11</code>, <code>l12</code>, <code>l21</code>
     * and <code>l22</code>. Then we add an edge between <code>l11</code> and <code>l22</code>.
     * Then we look at the view of the graph. Let's say the view is set for groups only, that means
     * only groups are visible and leaves are not. At this point we can say a meta edge exist between
     * <code>group1</code> and <code>group2</code> and it represents the edge <code>l11-l22</code>.
     * <p>
     * Therefore meta edges are useful when a graph is retracted/collapsed into clusters. Relations
     * between clusters can be get with meta edges directly. Note that a meta edge knows which edges
     * it represents and its weight is the sum of content edges' weight.
     * @return an edge iterable of all meta edges in the current graph view
     */
    public EdgeIterable getMetaEdges();

    /**
     * Returns meta edges for <code>nodeGroup</code>.
     * @param nodeGroup the node whose meta edges are queried
     * @return an edge iterable of meta edges incident to nodeGroup
     * @throws IllegalArgumentException if <code>nodeGroup</code> is <code>null</code> or not legal in
     * the graph
     */
    public EdgeIterable getMetaEdges(Node nodeGroup);

    /**
     * Returns proper edges represented by <code>metaEdge</code>. These proper edges are connected to
     * descendants of <code>metaEdges</code>'s incident nodes.
     * @param metaEdge the meta edge whose content edges is to be returned
     * @return an edge iterable of proper edges contained by <code>metaEdge</code>
     * @throws IllegalArgumentException if <code>metaEdge</code> is null,
     * or if <code>metaEdge</code> is not a meta edge
     */
    public EdgeIterable getMetaEdgeContent(Edge metaEdge);

    /**
     * Returns the number of <code>node</code>'s incident meta edges.
     * @param node the node whose meta degree is queried
     * @return the number of meta edges connected to <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> of not legal in
     * the graph.
     */
    public int getMetaDegree(Node node);

    /**
     * Clears all meta edges for <code>node</code>.
     * @param node the node whose meta edges will be deleted
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> of not legal in
     * the graph.
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public void clearMetaEdges(Node node);
}
