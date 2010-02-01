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
 * Meta edges are edges between a group and a leaf
 * or between two groups. They represents proper edges between descendants of groups. Meta
 * edges are always located only on nodes which are in the current view.
 * <p>
 * By default meta edges are automatically generated and maintained when
 * the hierarchy is manipulated (expand/contract).
 * 
 * @author Mathieu Bastian
 * @see HierarchicalGraph
 */
public interface MetaEdge extends Edge {

    /**
     * Returns the number of edges this meta-edge represents
     * @return  the number of edges this meta-edge represents
     */
    public int getCount();
}
