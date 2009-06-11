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
public interface Edge {

    /**
     * Returns the unique identifier of the edge.
     * @return the Id of the edge
     */
    public int getId();

    /**
     * Returns the source of the edge.
     * @return the source of the edge
     */
    public Node getSource();

    /**
     * Returns the target of the edge.
     * @return the target of the edge
     */
    public Node getTarget();

    /**
     * Returns the weight of the edge. Default value is 1.0.
     * @return the weight of the edge
     */
    public float getWeight();

    /**
     * Returns <code>true</code> if the egde is visible of <code>false</code> if its hidden.
     * @return  <code>true</code> if the egde is visible of <code>false</code> otherwise
     */
    public boolean isVisible();

    /**
     * Returns <code>true</code> if the edge is directed or <code>false</code> if it's undirected.
     * Default is directed. Note that value is immutable.<p>
     * Special cases:
     * <ul><li>If the edge has been created from an <b>undirected</b> graph, returns false.</li>
     * <li>If the edge has been created from a <b>directed</b> graph, returns true.</li>
     * <li>If the edge has been created from a <b>mixed</b> graph, returns the value set to the
     * <code>MixedGraph.addEdge()</code> method.</li></ul>
     * @return
     */
    public boolean isDirected();

    /**
     * Returns edge data.
     * @return edge data instance
     */
    public EdgeData getEdgeData();
}
