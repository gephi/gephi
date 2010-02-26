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
 * Implementation of graphs within graphs concept, by proposing methods to manipulate
 * the hierarchy of nodes.
 *
 * @author Mathieu Bastian
 * @see GraphModel
 */
public interface HierarchicalGraph extends Graph {

    /**
     * Add <code>node</code> as a child of <code>parent</code> in the graph. If <code>parent</code> is
     * <code>null</code>, <code>node</code> is added as a child of the (virtual) root node.
     * Fails if the node already exists.
     * @param node the node to add
     * @param parent the existing node whose a child is to be added or <code>null</code>
     * @return <code>true</code> if add is successful, false otherwise
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code>,
     * or if <code>parent</code> is not legal in the graph
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public boolean addNode(Node node, Node parent);

    /**
     * Returns the number of children of <code>node</code>. Returns <code>zero</code> if <code>node</code> is a leaf.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node to be queried
     * @return  the number of <code>node</code>'s children
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in the graph
     */
    public int getChildrenCount(Node node);

    /**
     * Returns the number of descendant of <code>node</code>. Returns <code>zero</code> if <code>node</code> is a leaf.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node to be queried
     * @return  the number of <code>node</code>'s descendant
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in the graph
     */
    public int getDescendantCount(Node node);

    /**
     * Returns the parent of <code>node</code> or <code>null</code> if <code>node</code>'s parent is (virtual) root.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node whose parent is to be returned
     * @return <code>node</code>'s parent
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in the graph
     */
    public Node getParent(Node node);

    /**
     * Returns children of <code>node</code>.
     * @param node the node whose children are to be returned
     * @return a node iterable of <code>node</code>'s children
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in the graph
     */
    public NodeIterable getChildren(Node node);

    /**
     * Returns descendants of <code>node</code>. Descendants are nodes which <code>node</code> is an ancestor.
     * @param node the node whose descendant are to be returned
     * @return a node iterable of <code>node</code>'s descendant
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in the graph
     */
    public NodeIterable getDescendant(Node node);

    /**
     * Returns edges incident to <code>nodeGroup</code> and <code>nodeGroup</code>'s descendants. Edges connected
     * to nodes which are not descendant of <code>nodeGroup</code> are excluded.
     * @param nodeGroup the node whose inner edges are to be returned
     * @return an edge iterable of edges inner <code>nodeGroup</code>
     * @throws IllegalArgumentException if <code>nodeGroup</code> is <code>null</code> or not legal in the graph
     */
    public EdgeIterable getInnerEdges(Node nodeGroup);

    /**
     * Returns edges <b>not</b> incident to <code>nodeGroup</code> or <code>nodeGroup</code>'s descendants.
     * Edges connected to nodes which are descendant of <code>nodeGroup</code> are excluded.
     * @param nodeGroup the node whose outer edges are to be returned
     * @return an edge iterable of edges outer <code>nodeGroup</code>
     * @throws IllegalArgumentException if <code>nodeGroup</code> is <code>null</code> or not legal in the graph
     */
    public EdgeIterable getOuterEdges(Node nodeGroup);

    /**
     * Returns roots of the hierarchy forest. They are children of the tree's (virtual) root an have
     * the level equal <code>zero</code>. If all nodes have the same level (i.e. no hierarchy) this
     * method is similar as <code>getNodes()</code>.
     * @return a node iterable of nodes at the top of the tree
     */
    public NodeIterable getTopNodes();

    public NodeIterable getNodesTree();

    /**
     * Returns nodes at the given <code>level</code> in the hierarchy. Top nodes
     * have the level <code>zero</code> and leaves' level is the height of the tree.
     * @param level the level whose nodes are to be returned
     * @return a node iterable of nodes located at <code>level</code> in the tree
     * @throws IllegalArgumentException if <code>level</code> is not between 0 and the height of the tree
     */
    public NodeIterable getNodes(int level);

    /**
     * The number of nodes located at the given <code>level</code> int the hierarchy. Similar as
     * <code>getNodes(level).toArray().length</code>.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param level the level whose nodes are to be returned
     * @return the number of nodes at <code>level</code>
     * @throws IllegalArgumentException if <code>level</code> is not between 0 and the height of the tree
     */
    public int getLevelSize(int level);

    /**
     * Returns <code>true</code> if <code>descendant</code> is a descendant of <code>node</code>. True if <code>node</code> is an ancestor
     * of <code>descendant</code>.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node to be queried
     * @param descendant the descendant node to be queried
     * @return <code>true</code> if <code>descendant</code> is a descendant of <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> or <code>descendant</code> is <code>null</code> or not legal in the graph
     */
    public boolean isDescendant(Node node, Node descendant);

    /**
     * Returns <code>true</code> if <code>ancestor</code> is an ancestor of <code>node</code>. True if <code>node</code> is a descendant of
     * <code>ancestor</code>.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node to be queried
     * @param ancestor the ancestor to be queried
     * @return <code>true</code> if <code>ancestor</code> is an ancestor of <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> or <code>ancestor</code> is <code>null</code> or not legal in the graph
     */
    public boolean isAncestor(Node node, Node ancestor);

    /**
     * Returns <code>true</code> if <code>following</code> is after <code>node</code>. The definition is similar to <code>XML</code> following
     * axis. Is true when <code>following</code> has a greater <b>pre</b> and <b>post</b> order than <code>node</code>.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node to be queried
     * @param following the following to be queried
     * @return <code>true</code> if <code>following</code> is following <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> or <code>following</code> is <code>null</code> or not legal in the graph
     */
    public boolean isFollowing(Node node, Node following);

    /**
     * Returns <code>true</code> if <code>preceding</code> is before <code>node</code>. The definition is similar to <code>XML</code> preceding
     * axis. Is true when <code>preceding</code> has a lower <b>pre</b> and <b>post</b> order than <code>node</code>.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node to be queried
     * @param preceding the preceding to be queried
     * @return <code>true</code> if <code>preceding</code> is preceding <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> or <code>preceding</code> is <code>null</code> or not legal in the graph
     */
    public boolean isPreceding(Node node, Node preceding);

    /**
     * Returns <code>true</code> if <code>parent</code> is the parent of <code>node</code>.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node to be queried
     * @param parent the parent to be queried
     * @return <code>true</code> if <code>parent</code> is the parent of <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> or <code>parent</code> is <code>null</code> or not legal in the graph
     */
    public boolean isParent(Node node, Node parent);

    /**
     * Returns the height of the tree. The height is <code>zero</code> when all nodes have the same level.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @return the height of the tree
     */
    public int getHeight();

    /**
     * Returns the level of <code>node</code> in the hierarchy. Roots have the level <code>zero</code> and it inscreases when going down
     * in the tree.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node to be queried
     * @return the level value of <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in the graph
     */
    public int getLevel(Node node);

    /**
     * Move <code>node</code> and descendants of <code>node</code> to <code>nodeGroup</code>, as <code>node</code> will be a child of
     * <code>nodeGroup</code>. Be aware <code>nodeGroup</code> can't be a descendant of <code>node</code>.
     * @param node the node to be appened to <code>nodeGroup</code> children
     * @param nodeGroup the node to receive <code>node</code> as a child
     * @throws IllegalArgumentException if <code>node</code> or <code>nodeGroup</code> is <code>null</code> or not legal in the graph,
     * or if <code>nodeGroup</code> is a descendant of node
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public void moveToGroup(Node node, Node nodeGroup);

    /**
     * Remove <code>node</code> from its parent group and append it to <code>node</code>'s parent. In other words <code>node</code> rise
     * one level in the tree and is no more a child of its parent.
     * @param node the node to be removed from it's group
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in the graph,
     * or if <code>node</code> is already at the top of the tree
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public void removeFromGroup(Node node);

    /**
     * Group <code>nodes</code> into a new node group (i.e. cluster). Creates an upper node in the tree and appends <code>nodes</code> to it.
     * Content of <code>nodes</code> can be existing groups. In that case, <code>nodes</code> must only contains roots of groups.
     * Therefore all nodes in <code>nodes</code> must have the same <b>parent</b>. The method returns the newly
     * created group of nodes.
     * @param nodes the nodes to be grouped in a new group
     * @return the newly created group of nodes which contains <code>nodes</code> and descendants of <code>nodes</code>
     * @throws IllegalArgumentException if <code>nodes</code> is <code>null</code>,
     * or if <code>nodes</code> is empty,
     * or if content nodes are not legal in the graph,
     * or if <code>nodes</code>' parent is not similar between elements
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public Node groupNodes(Node[] nodes);

    /**
     * Ungroup nodes in <code>nodeGroup</code> and destroy <code>nodeGroup</code>. Descendants of <code>nodeGroup</code> are appened to
     * <code>nodeGroup</code>'s parent node. This method is the opposite of <code>groupNodes()</code>. If called with the result of
     * <code>groupNodes()</code> the state will be equal to the state before calling <code>groupNodes()</code>.
     * @param nodeGroup the parent node of nodes to be ungrouped
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code>, empty or not legal in the graph
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public void ungroupNodes(Node nodeGroup);

    public EdgeIterable getHierarchyEdges();

    public ImmutableTreeNode wrapToTreeNode();

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
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node to be queried
     * @return <code>true</code> if <code>node</code> is in the view, <code>false</code> otherwise
     * @throws IllegalArgumentException if <code>nodeGroup</code> is <code>null</code> or not legal in
     * the graph
     */
    public boolean isInView(Node node);

    /**
     * Reset the current view to leaves of the clustered graph tree. Therefore the
     * <code>getNodesInView()</code> method returns only these leaves.
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public void resetViewToLeaves();

    /**
     * Reset the current view to top nodes of the clustered graph tree. Therefore the
     * <code>getNodesInView()</code> method returns only these nodes.
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public void resetViewToTopNodes();

    /**
     * Reset the current view to nodes located at the specified <code>level</code> in the
     * clustered graph hierarchy. Therefore the <code>getNodesInView()</code> method returns
     * only these nodes.
     * @throws IllegalArgumentException if <code>level</code> is not between 0 and the height of the tree
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
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

    public EdgeIterable getEdgesAndMetaEdges();

    /**
     * Returns meta edges for <code>nodeGroup</code>.
     * @param nodeGroup the node whose meta edges are queried
     * @return an edge iterable of meta edges incident to nodeGroup
     * @throws IllegalArgumentException if <code>nodeGroup</code> is <code>null</code> or not legal in
     * the graph
     */
    public EdgeIterable getMetaEdges(Node nodeGroup);


    /**
     * Returns the number of <code>node</code>'s incident meta edges.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
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
