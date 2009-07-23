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
public interface GraphController {

    public GraphFactory factory();

    public DirectedGraph getDirectedGraph();

    public DirectedGraph getVisibleDirectedGraph();

    public UndirectedGraph getUndirectedGraph();

    public UndirectedGraph getVisibleUndirectedGraph();

    public MixedGraph getMixedGraph();

    public MixedGraph getVisibleMixedGraph();

    public HierarchicalDirectedGraph getHierarchicalDirectedGraph();

    public HierarchicalDirectedGraph getVisibleHierarchicalDirectedGraph();

    public HierarchicalUndirectedGraph getHierarchicalUndirectedGraph();

    public HierarchicalUndirectedGraph getVisibleHierarchicalUndirectedGraph();

    public HierarchicalMixedGraph getHierarchicalMixedGraph();

    public HierarchicalMixedGraph getVisibleHierarchicalMixedGraph();

    public ClusteredDirectedGraph getClusteredDirectedGraph();

    public ClusteredDirectedGraph getVisibleClusteredDirectedGraph();

    public ClusteredUndirectedGraph getClusteredUndirectedGraph();

    public ClusteredUndirectedGraph getVisibleClusteredUndirectedGraph();

    public ClusteredMixedGraph getClusteredMixedGraph();

    public ClusteredMixedGraph getVisibleClusteredMixedGraph();

    public FilteredGraph getFilteredGraph(Graph graph);

    public DynamicGraph getDynamicGraph(Graph graph);
}
