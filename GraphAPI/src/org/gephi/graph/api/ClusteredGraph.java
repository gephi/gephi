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
public interface ClusteredGraph {

    /**
     * Add <code>node</code> as a child of <code>parent</code> in the graph. If <code>parent</code> is
     * <code>null</code>, <code>node</code> is added as a child of the (virtual) root node.
     * Fails if the node already exists.
     * @param node the node to add
     * @param parent the existing node whose a child is to be added or <code>null</code>
     * @return <code>true</code> if add is successful, false otherwise
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code>,
     * or if <code>node</code> or <code>parent</code> is not legal in the graph
     */
    public boolean addNode(Node node, Node parent);

    /**
     * Returns the number of children of <code>node</code>. Returns <code>zero</code> if <code>node</code> is a leaf.
     * @param node the node to be queried
     * @return  the number of <code>node</code>'s children
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in the graph
     */
    public int getChildrenCount(Node node);

    /**
     * Returns the parent of <code>node</code> or <code>null</code> if <code>node</code>'s parent is (virtual) root.
     * @param node the node whose parent is to be returned
     * @return <code>node</code>'s parent
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in the graph
     */
    public Node getParent(Node node);

    /**
     * Returns a node iterator of children of <code>node</code>.
     * @param node the node whose children are to be returned
     * @return a node iterator of <code>node</code>'s children
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in the graph
     */
    public NodeIterable getChildren(Node node);

    /**
     * Returns a node iterator of descendant of <code>node</code>. Descendants are nodes which <code>node</code> is an ancestor.
     * @param node the node whose descendant are to be returned
     * @return a node iterator of <code>node</code>'s descendant
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in the graph
     */
    public NodeIterable getDescendant(Node node);

    /**
     * Returns an edge iterator of edges incident to <code>nodeGroup</code> and <code>nodeGroup</code>'s descendants. Edges connected
     * to nodes which are not descendant of <code>nodeGroup</code> are excluded.
     * @param nodeGroup the node whose inner edges are to be returned
     * @return an edge iterator of edges inner <code>nodeGroup</code>
     * @throws IllegalArgumentException if <code>nodeGroup</code> is <code>null</code> or not legal in the graph
     */
    public EdgeIterable getInnerEdges(Node nodeGroup);

    /**
     * Returns an edge iterator of edges <b>not</b> incident to <code>nodeGroup</code> or <code>nodeGroup</code>'s descendants.
     * Edges connected to nodes which are descendant of <code>nodeGroup</code> are excluded.
     * @param nodeGroup the node whose outer edges are to be returned
     * @return an edge iterator of edges outer <code>nodeGroup</code>
     * @throws IllegalArgumentException if <code>nodeGroup</code> is <code>null</code> or not legal in the graph
     */
    public EdgeIterable getOuterEdges(Node nodeGroup);

    /**
     * Returns a node iterator of roots of the hierarchy forest. They are children of the tree's (virtual) root.
     * @return a node iterator of nodes at the top of the tree
     */
    public NodeIterable getTopNodes();

    public EdgeIterable getMetaEdges();

    public EdgeIterable getMetaEdges(Node node);

    public int getMetaDegree(Node node);

    /**
     * Returns <code>true</code> if <code>descendant</code> is a descendant of <code>node</code>. True if <code>node</code> is an ancestor
     * of <code>descendant</code>.
     * @param node the node to be queried
     * @param descendant the descendant node to be queried
     * @return <code>true</code> if <code>descendant</code> is a descendant of <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> or <code>descendant</code> is <code>null</code> or not legal in the graph
     */
    public boolean isDescendant(Node node, Node descendant);

    /**
     * Returns <code>true</code> if <code>ancestor</code> is an ancestor of <code>node</code>. True if <code>node</code> is a descendant of
     * <code>ancestor</code>.
     * @param node the node to be queried
     * @param ancestor the ancestor to be queried
     * @return <code>true</code> if <code>ancestor</code> is an ancestor of <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> or <code>ancestor</code> is <code>null</code> or not legal in the graph
     */
    public boolean isAncestor(Node node, Node ancestor);

    /**
     * Returns <code>true</code> if <code>following</code> is after <code>node</code>. The definition is similar to <code>XML</code> following
     * axis. Is true when <code>following</code> has a greater <b>pre</b> and <b>post</b> order than <code>node</code>.
     * @param node the node to be queried
     * @param following the following to be queried
     * @return <code>true</code> if <code>following</code> is following <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> or <code>following</code> is <code>null</code> or not legal in the graph
     */
    public boolean isFollowing(Node node, Node following);

    /**
     * Returns <code>true</code> if <code>preceding</code> is before <code>node</code>. The definition is similar to <code>XML</code> preceding
     * axis. Is true when <code>preceding</code> has a lower <b>pre</b> and <b>post</b> order than <code>node</code>.
     * @param node the node to be queried
     * @param following the preceding to be queried
     * @return <code>true</code> if <code>preceding</code> is preceding <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> or <code>preceding</code> is <code>null</code> or not legal in the graph
     */
    public boolean isPreceding(Node node, Node preceding);

    /**
     * Returns <code>true</code> if <code>parent</code> is the parent of <code>node</code>.
     * @param node the node to be queried
     * @param parent the parent to be queried
     * @return <code>true</code> if <code>parent</code> is the parent of <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> or <code>parent</code> is <code>null</code> or not legal in the graph
     */
    public boolean isParent(Node node, Node parent);

    /**
     * Returns the height of the tree.
     * @return Returns the height of the tree
     */
    public int getHeight();

    /**
     * Returns the level of <code>node</code> in the hierarchy. The root has the level <code>zero</code> and it inscreases when going down
     * in the tree.
     * @param node the node to be queried
     * @return the level value of <code>node</code>
     */
    public int getLevel(Node node);

    public void expand(Node node);

    public void retract(Node node);

    public void moveToGroup(Node node, Node nodeGroup);

    public void removeFromGroup(Node node);

    public Node groupNodes(Node[] nodes);

    public void ungroupNodes(Node[] nodes);

    public void clearMetaEdges(Node node);
}
