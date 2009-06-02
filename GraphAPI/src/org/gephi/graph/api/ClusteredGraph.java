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

    public void addNode(Node node, Node parent);

    public int getChildrenCount(Node node);

    public Node getParent(Node node);

    public NodeIterable getChildren(Node node);

    public NodeIterable getDescendant(Node node);

    public EdgeIterable getInnerEdges(Node nodeGroup);

    public EdgeIterable getOuterEdges(Node nodeGroup);

    public NodeIterable getTopNodes();

        public EdgeIterable getMetaEdges();

    public EdgeIterable getMetaEdges(Node node);

    public int getMetaDegree(Node node);

    public boolean isDescendant(Node node, Node descendant);

    public boolean isAncestor(Node node, Node ancestor);

    public boolean isFollowing(Node node, Node following);

    public boolean isPreceding(Node node, Node preceding);

    public boolean isParent(Node node, Node parent);

    public int getHeight();

    public int getLevel(Node node);

    public void expand(Node node);

    public void retract(Node node);

    public void addToGroup(Node node, Node nodeGroup);

    public void removeFromGroup(Node node);

    public void groupNodes(Node[] nodes);

    public void ungroupNodes(Node[] nodes);

    public void clearMetaEdges(Node node);
}
