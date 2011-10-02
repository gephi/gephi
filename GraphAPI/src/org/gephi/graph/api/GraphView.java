/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
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
