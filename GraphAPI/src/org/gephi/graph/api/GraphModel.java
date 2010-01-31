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
public interface GraphModel {

    public GraphFactory factory();

    public GraphSettings settings();

    public GraphView newView();

    public void destroyView(GraphView view);

    public void setVisibleView(GraphView view);

    public GraphView getVisibleView();

    public Graph getGraph();

    public DirectedGraph getDirectedGraph();

    public UndirectedGraph getUndirectedGraph();

    public MixedGraph getMixedGraph();

    public HierarchicalGraph getHierarchicalGraph();

    public HierarchicalDirectedGraph getHierarchicalDirectedGraph();

    public HierarchicalUndirectedGraph getHierarchicalUndirectedGraph();

    public HierarchicalMixedGraph getHierarchicalMixedGraph();

    public Graph getGraphVisible();

    public DirectedGraph getDirectedGraphVisible();

    public UndirectedGraph getUndirectedGraphVisible();

    public MixedGraph getMixedGraphVisible();

    public HierarchicalGraph getHierarchicalGraphVisible();

    public HierarchicalDirectedGraph getHierarchicalDirectedGraphVisible();

    public HierarchicalUndirectedGraph getHierarchicalUndirectedGraphVisible();

    public HierarchicalMixedGraph getHierarchicalMixedGraphVisible();

    public Graph getGraph(GraphView view);

    public DirectedGraph getDirectedGraph(GraphView view);

    public UndirectedGraph getUndirectedGraph(GraphView view);

    public MixedGraph getMixedGraph(GraphView view);

    public HierarchicalGraph getHierarchicalGraph(GraphView view);

    public HierarchicalDirectedGraph getHierarchicalDirectedGraph(GraphView view);

    public HierarchicalUndirectedGraph getHierarchicalUndirectedGraph(GraphView view);

    public HierarchicalMixedGraph getHierarchicalMixedGraph(GraphView view);

    public void pushFrom(Graph graph);

    public GraphModel copy();

    public void clear();

    /**
     * Add <code>graphListener</code> as a listener to this graph, if it is not already.
     * To pass a <code>WeakReference</code>, use Netbeans <code>WeakListeners</code> utility class.
     * @param graphListener the listener to add
     */
    public void addGraphListener(GraphListener graphListener);

    /**
     * Remove <code>graphListener</code> as a listener to this graph.
     * @param graphListener the listener to remove
     */
    public void removeGraphListener(GraphListener graphListener);

    /**
     * Returns <code>true</code> if the graph is <b>directed</b> by default. This value is an
     * indicator of the current state and it means that so far all edges are directed in the graph.
     * @return <code>true</code> if the graph is only directed or <code>false</code> otherwise
     * @see DirectedGraph
     */
    public boolean isDirected();

    /**
     * Returns <code>true</code> if the graph is <b>undirected</b> by default. This value is an
     * indicator of the current state and it means that so far all edges are undirected in the graph.
     * @return <code>true</code> if the graph is only undirected or <code>false</code> otherwise
     * @see UndirectedGraph
     */
    public boolean isUndirected();

    /**
     * Returns <code>true</code> if the graph is <b>mixed</b> by default. This value is an
     * indicator of the current state and it means that directed and undirected edges has been
     * added to the graph. When it returns <code>true</code>, <code>isDirected()</code> and
     * <code>isUndirected()</code> methods always returns <code>false</code>.
     * @return <code>true</code> if the graph is mixed or <code>false</code> otherwise
     * @see MixedGraph
     */
    public boolean isMixed();

    /**
     * Returns <code>true</code> if the graph is <b>hierarchical</b>. This indicates the presence
     * of a hierarchy, in other words the height of the tree is greater than 0.
     * @return <code>true</code> if the graph is clustered or <code>false</code> otherwise
     * @see HierarchicalGraph
     */
    public boolean isHierarchical();
}
