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
public interface ClusteredDirectedGraph extends HierarchicalDirectedGraph, ClusteredGraph {

    /**
     * Returns incoming meta edges incident to <code>node</code>.
     * @param node the node whose incoming meta edges are to be returned
     * @return an edge iterable of <code>node</code>'s incoming meta edges
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> of not legal in
     * the graph
     */
    public EdgeIterable getMetaInEdges(Node node);

    /**
     * Returns outgoing meta edges incident to <code>node</code>.
     * @param node the node whose outgoing meta edges are to be returned
     * @return an edge iterable of <code>node</code>'s outgoing meta edges
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> of not legal in
     * the graph
     */
    public EdgeIterable getMetaOutEdges(Node node);

    /**
     * Returns the number of <code>node</code>'s incoming meta edges.
     * @param node the node whose meta in-degree is queried
     * @return the number of meta edges incoming to <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> of not legal in
     * the graph.
     */
    public int getMetaInDegree(Node node);

    /**
     * Returns the number of <code>node</code>'s outgoing meta edges.
     * @param node the node whose meta out-degree is queried
     * @return the number of meta edges outgoing from <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> of not legal in
     * the graph.
     */
    public int getMetaOutDegree(Node node);

    public MetaEdge getMetaEdge(Node source, Node target);
}
