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
 * Graph that contains both directed and undirected edges.
 * 
 * @author Mathieu Bastian
 * @see GraphModel
 */
public interface MixedGraph extends Graph {

    /**
     * Add to the graph a <b>directed</b> or <b>undirected</b> edge between <code>source</code> and
     * <code>target</code>. Graph does not accept parallel edges.
     * Fails if a such edge already exists in the graph.
     * @param source the source node
     * @param target the target node
     * @param directed the type of edge to be created
     * @return true if add is successful, false otherwise
     * @throws IllegalArgumentException if <code>source</code> or <code>target</code>
     * is <code>null</code> or not legal nodes for this <code>edge</code>
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public boolean addEdge(Node source, Node target, boolean directed);

    /**
     * Finds and returns a <b>directed</b> or <b>undirected</b> edge that connects <code>node1</code> and
     * <code>node2</code>. Returns <code>null</code> if no such edge is found.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node1 the first incident node of the queried edge
     * @param node2 thge second incident node of the queried edge
     * @return an edge that connects <code>node1</code> and <code>node2</code>
     * or <code>null</code> if no such edge exists
     * @throws IllegalArgumentException if <code>node1</code> or <code>node2</code>
     * is <code>null</code> or not legal nodes in the graph
    */
    public Edge getEdge(Node node1, Node node2);

    /**
     * Returns an edge iterator of <b>directed</b> edges in the graph.
     * @return Returns an edge iterator of <b>directed</b> edges in the graph.
     */
    public EdgeIterable getDirectedEdges();

    /**
     * Returns an edge iterator of <b>directed</b> edges in the graph.
     * @return Returns an edge iterator of <b>directed</b> edges in the graph.
     */
    public EdgeIterable getUndirectedEdges();

    /**
     * Returns <code>true</code> if <code>edge</code> is <b>directed</b>  if
     * <b>undirected</b>.
     * @param edge the edge to be queried
     * @return Returns <code>true</code> if <code>edge</code> is <b>directed</b>  if
     * <b>undirected</b>
     * @throws IllegalArgumentException if <code>edge</code> is <code>null</code>
     */
    public boolean isDirected(Edge edge);
}
