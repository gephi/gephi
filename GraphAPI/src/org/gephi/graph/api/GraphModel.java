   /*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.graph.api;

import org.gephi.project.api.Workspace;

/**
 * Root interface that contains the complete graph structure and build
 * {@link Graph} objets on demand.
 * <p>
 * The graph structure contains several views. {@link GraphView} are sub-graphs with a unique
 * identifier, the <b>viewId</b>. The model maintains all views, and especially:
 * <ul><li><b>The Main View:</b> The complete graph, with a view id equal to zero.</li>
 * <li><b>The Visible View:</b> The graph currently visualized. Can be the main view
 * or an other view that expose a particular sub-graph.</li></ul>
 * Different type of graph can be queried: <b>directed</b>,
 * <b>undirected</b>, <b>mixed</b> and <b>hierarchical</b>. All types can be queried
 * at any time, regardless how the structure is. Indeed even if only directed edges
 * were appened to the graph, an <b>undirected</b> graph can be obtained.
 * <p>
 * It is preferable to use the main graph for performing updates (add, remove, ...).
 * <p>
 * <h3>Append data to an empty model</h3>
 * The following code shows how to add two nodes an an edge to graph.
 * <pre>
 * Graph graph = model.getGraph();
 * Node n1 = model.factory().newNode();
 * Node n2 = model.factory().newNode();
 * Edge e1 = model.factory().newEdge(n1, n2);
 * graph.addNode(n1);
 * graph.addNode(n2);
 * graph.addEdge(e1);
 * </pre>
 * <h3>Set Visible view</h3>
 * The following code shows how to set the visible view. This is useful if you
 * need to visualize a sub-graph.
 * <pre>
 * GraphView newView = model.newView();     //Duplicate main view
 * Graph subGraph = model.getGraph(newView);
 * //Filter subgraph by removing nodes and edges
 * model.setVisibleView(newView);       //Set the view as current visible view
 * </pre>
 * <b>Note:</b> Pay attention to destroy views when they are not used anymore.
 * @author Mathieu Bastian
 * @see GraphController
 */
public interface GraphModel {

    /**
     * Returns the factory that creates nodes and edges for this model.
     * @return      the graph model factory
     */
    public GraphFactory factory();

    /**
     * Returns the model settings.
     * @return      the graph model settings
     */
    public GraphSettings settings();

    /**
     * Create a new view by duplicating <b>main</b> view. The view contains all
     * nodes and edges in the structure.
     * @return      a new graph view, obtained from duplicating main view
     */
    public GraphView newView();

    /**
     * Copy <code>view</code> to a new graph view. The new view contains all
     * nodes and edges present in <code>view</code>.
     * @return  a new graph view, obtained from duplicating <code>view</code>
     */
    public GraphView copyView(GraphView view);

    /**
     * Destroy <code>view</code>, if exists. Always destroy views that are not
     * needed anymore to avoid memory overhead.
     * @param view  the view that is to be destroyed
     */
    public void destroyView(GraphView view);

    /**
     * Sets the current visible view and nofity listeners the visible view changed.
     * @param view  the view that is to be set as the visible view
     */
    public void setVisibleView(GraphView view);

    /**
     * Returns the current viisble view. By default, returns the <b>main</b>
     * view.
     * @return      the current visible view
     */
    public GraphView getVisibleView();

    /**
     * Build a <code>Graph</code> to access the <b>main</b> view. If no undirected
     * edges have been added, returns a <code>DirectedGraph</code>. If the graph
     * only contains undirected edges, returns a <code>UndirectedGraph</code>.
     * If both directed and undirected edges exists, returns a <code>MixedGraph</code>.
     * @return      a graph object, directed by default
     */
    public Graph getGraph();

    /**
     * Build a <code>DirectedGraph</code> to access the <b>main</b> view.
     * @return      a directed graph object
     */
    public DirectedGraph getDirectedGraph();

    /**
     * Build a <code>UndirectedGraph</code> to access the <b>main</b> view.
     * @return      an undirected graph object
     */
    public UndirectedGraph getUndirectedGraph();

    /**
     * Build a <code>MixedGraph</code> to access the <b>main</b> view.
     * @return      a mixed graph object
     */
    public MixedGraph getMixedGraph();

    /**
     * Build a <code>HierarchicalGraph</code> to access the <b>main</b> view.
     * If no undirected edges have been added, returns a
     * <code>HierarchicalDirectedGraph</code>. If the graph only contains undirected
     * edges, returns a <code>HierarchicalUndirectedGraph</code>. If both directed
     * and undirected edges exists, returns a <code>HierarchicalMixedGraph</code>.
     * <p>
     * Hierarchical graphs are normal graphs with more features to handle graphs
     * within graphs.
     * @return      a hierarchical graph object, directed by default
     */
    public HierarchicalGraph getHierarchicalGraph();

    /**
     * Build a <code>HierarchicalDirectedGraph</code> to access the <b>main</b> view.
     * @return      a hierarchical directed graph object
     */
    public HierarchicalDirectedGraph getHierarchicalDirectedGraph();

    /**
     * Build a <code>HierarchicalUndirectedGraph</code> to access the <b>main</b> view.
     * @return      a hierarchical undirected graph object
     */
    public HierarchicalUndirectedGraph getHierarchicalUndirectedGraph();

    /**
     * Build a <code>HierarchicalMixedGraph</code> to access the <b>main</b> view.
     * @return      a hierarchical mixed graph object
     */
    public HierarchicalMixedGraph getHierarchicalMixedGraph();

    /**
     * Build a <code>Graph</code> to access the <b>visible</b> view. If no undirected
     * edges have been added, returns a <code>DirectedGraph</code>. If the graph
     * only contains undirected edges, returns a <code>UndirectedGraph</code>.
     * If both directed and undirected edges exists, returns a <code>MixedGraph</code>.
     * @return      a graph object, directed by default
     */
    public Graph getGraphVisible();

    /**
     * Build a <code>DirectedGraph</code> to access the <b>visible</b> view.
     * @return      a directed graph object
     */
    public DirectedGraph getDirectedGraphVisible();

    /**
     * Build a <code>UndirectedGraph</code> to access the <b>visible</b> view.
     * @return      an undirected graph object
     */
    public UndirectedGraph getUndirectedGraphVisible();

    /**
     * Build a <code>MixedGraph</code> to access the <b>visible</b> view.
     * @return      a mixed graph object
     */
    public MixedGraph getMixedGraphVisible();

    /**
     * Build a <code>HierarchicalGraph</code> to access the <b>visible</b> view.
     * If no undirected edges have been added, returns a
     * <code>HierarchicalDirectedGraph</code>. If the graph only contains undirected
     * edges, returns a <code>HierarchicalUndirectedGraph</code>. If both directed
     * and undirected edges exists, returns a <code>HierarchicalMixedGraph</code>.
     * <p>
     * Hierarchical graphs are normal graphs with more features to handle graphs
     * within graphs.
     * @return      a hierarchical graph object, directed by default
     */
    public HierarchicalGraph getHierarchicalGraphVisible();

    /**
     * Build a <code>HierarchicalDirectedGraph</code> to access the <b>visible</b> view.
     * @return      a hierarchical directed graph object
     */
    public HierarchicalDirectedGraph getHierarchicalDirectedGraphVisible();

    /**
     * Build a <code>HierarchicalUndirectedGraph</code> to access the <b>visible</b> view.
     * @return      a hierarchical undirected graph object
     */
    public HierarchicalUndirectedGraph getHierarchicalUndirectedGraphVisible();

    /**
     * Build a <code>HierarchicalMixedGraph</code> to access the <b>visible</b> view.
     * @return      a hierarchical mixed graph object
     */
    public HierarchicalMixedGraph getHierarchicalMixedGraphVisible();

    /**
     * Build a <code>Graph</code> to access <code>view/code>. If no undirected
     * edges have been added, returns a <code>DirectedGraph</code>. If the graph
     * only contains undirected edges, returns a <code>UndirectedGraph</code>.
     * If both directed and undirected edges exists, returns a <code>MixedGraph</code>.
     * @return      a graph object on <code>view</code>, directed by default
     */
    public Graph getGraph(GraphView view);

    /**
     * Build a <code>DirectedGraph</code> to access <code>view</code>.
     * @return      a directed graph object on <code>view</code>
     */
    public DirectedGraph getDirectedGraph(GraphView view);

    /**
     * Build a <code>UndirectedGraph</code> to access <code>view</code>.
     * @return      an undirected graph object on <code>view</code>
     */
    public UndirectedGraph getUndirectedGraph(GraphView view);

    /**
     * Build a <code>MixedGraph</code> to access <code>view</code>.
     * @return      a mixed graph object on <code>view</code>
     */
    public MixedGraph getMixedGraph(GraphView view);

    /**
     * Build a <code>HierarchicalGraph</code> to access <code>view</code>.
     * If no undirected edges have been added, returns a
     * <code>HierarchicalDirectedGraph</code>. If the graph only contains undirected
     * edges, returns a <code>HierarchicalUndirectedGraph</code>. If both directed
     * and undirected edges exists, returns a <code>HierarchicalMixedGraph</code>.
     * <p>
     * Hierarchical graphs are normal graphs with more features to handle graphs
     * within graphs.
     * @return      a hierarchical graph object on <code>view</code>, directed by default
     */
    public HierarchicalGraph getHierarchicalGraph(GraphView view);

    /**
     * Build a <code>HierarchicalDirectedGraph</code> to access <code>view</code>.
     * @return      a hierarchical directed graph object on <code>view</code>
     */
    public HierarchicalDirectedGraph getHierarchicalDirectedGraph(GraphView view);

    /**
     * Build a <code>HierarchicalUndirectedGraph</code> to access <code>view</code>.
     * @return      a hierarchical undirected graph object on <code>view</code>
     */
    public HierarchicalUndirectedGraph getHierarchicalUndirectedGraph(GraphView view);

    /**
     * Build a <code>HierarchicalMixedGraph</code> to access <code>view</code>.
     * @return      a hierarchical mixed graph object on <code>view</code>
     */
    public HierarchicalMixedGraph getHierarchicalMixedGraph(GraphView view);

    /**
     * Copy the graph structure from <code>graph</code> to this model. The
     * given <code>graph</code>must come from a different <code>GraphModel</code>,
     * e.g. a different workspace.
     * @param graph the graph that is to be copied in this model
     * @throws IllegalArgumentException if <code>graph</code> belongs to this
     * graph model
     */
    public void pushFrom(Graph graph);

    /**
     * Copy the nodes and edges betweeen these nodes from the <code>graph</code>
     * to this model. The given <code>graph</code>must come from a different
     * <code>GraphModel</code>, e.g. a different workspace.
     * @param graph the graph that is to be copied in this model
     * @param nodes the nodes to copy
     * @throws IllegalArgumentException if <code>graph</code> belongs to this
     * graph model
     */
    public void pushNodes(Graph graph, Node[] nodes);

    /**
     * Returns a complete copy of this model, including all views but not listeners.
     * @return      a copy of this graph model
     */
    public GraphModel copy();

    /**
     * Clears the model by deleting all views and reseting <code>main</code>
     * view. Calling this method immediately makes all <code>Graph</code> on
     * other views than <b>main</b> obsolete.
     */
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

    /**
     * Returns the workspace this graph model belongs to.
     * @return the workspace that owns this graph model
     */
    public Workspace getWorkspace();
}
