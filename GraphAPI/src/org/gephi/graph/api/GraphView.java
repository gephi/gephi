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
 * Graph view define a <b>subgraph</b>, i.e. a subset of the original graph.
 * <code>GraphModel</code> has at least a single view, called the <b>main</b>
 * view, which contains 100% of the graph. Other views can be defined by users
 * to define subgraphs with a reduced number of nodes and edges. This is typically
 * used by filters, which returns filtered graphs.
 * <p>
 * <code>GraphModel</code> also contains the <b>visible</b> view, which is the
 * main view by default but can be set to configure the view that is visualized.
 * <p>
 * What exactly contains the view? The complete hierarchy of nodes, the edges
 * and meta-edges. Each view has separate hierarchy, therefore grouping and
 * meta-edges are independent.
 * <p>
 * Note that nodes' id is unique, and is not touched by views.
 * <h3>Main or Visible View?</h3>
 * When you need to interact with the graph structure, you have the choice
 * between working on the complete graph, i.e. the <b>main</b> view, or the
 * <b>visible</b> view. In most cases, it's preferable to use the <b>visible</b>
 * view because your action will use the graph currently visualized, which has
 * been potentially filtered by users on purpose.
 * <p>
 * To get the graph in the visible view, see {@link GraphModel#getGraphVisible()}.
 * <h3>Set the Visible View</h3>
 * This is the typical workflow for filtering a graph and display the results,
 * as done by <code>FiltersAPI</code>.
 * <ul><li>Create a new view, which duplicates the <b>main</b> view, with all
 * nodes and edges in it.</li>
 * <li>Remove nodes and edges in the view.</li>
 * <li>Set the view as the curently visible view.</li></ul>
 * To set a view as the currently visible view, see
 * {@link GraphModel#setVisibleView(org.gephi.graph.api.GraphView)}.
 *
 * @author Mathieu Bastian
 */
public interface GraphView {

    /**
     * Returns this view unique identifier. The id is a positive integer. The
     * main view always has it's id equal to zero.
     * @return      the view identifier
     */
    public int getViewId();

    /**
     * Returns <code>true</code> if this view is the main view. Each
     * <code>GraphModel</code> has a single main view, which contains all nodes
     * and edges in the model.
     * @return      <code>true</code> if this is the main view, <code>false</code>
     * otherwise.
     */
    public boolean isMainView();

    /**
     * Returns the graph model this view belongs.
     * @return      the graph model this view belongs.
     */
    public GraphModel getGraphModel();
}
