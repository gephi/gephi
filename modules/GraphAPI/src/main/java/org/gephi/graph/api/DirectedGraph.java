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
 * Graph with directed edges.
 *
 * @author Mathieu Bastian
 * @see GraphModel
 */
public interface DirectedGraph extends Graph {

    /**
     * Add an edge between <code>source</code> and <code>target</code> to the graph.
     * Graph does not accept parallel edges.
     * Fails if a such edge already exists in the graph.
     * @param source the source node
     * @param target the target node
     * @return true if add is successful, false otherwise
     * @throws IllegalArgumentException if <code>source</code> or <code>target</code>
     * is <code>null</code> or not legal nodes for this <code>edge</code>
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public boolean addEdge(Node source, Node target);

    /**
     * Finds and returns an edge that connects <code>source</code> and <code>target</code>. Returns
     * <code>null</code> if no such edge is found.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param source the source node of the queried edge
     * @param target the target node of the queried edge
     * @return a directed edge that connects <code>source</code> and <code>target</code>
     * or <code>null</code> if no such edge exists
     * @throws IllegalArgumentException if <code>source</code> or <code>target</code>
     * is <code>null</code> or not legal nodes in the graph
     */
    public Edge getEdge(Node source, Node target);

    /**
     * Returns incoming edges incident to <code>node</code>.
     * @param node the node whose incoming edges are to be returned
     * @return an edge iterable of incoming edges incident to <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code>,
     * or if <code>node</code> is not legal in the graph.
     */
    public EdgeIterable getInEdges(Node node);

    /**
     * Returns outgoing edges incident to <code>node</code>.
     * @param node the node whose outgoing edges are to be returned
     * @return an edge iterable of outgoing edges incident to <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code>,
     * or if <code>node</code> is not legal in the graph.
     */
    public EdgeIterable getOutEdges(Node node);

    /**
     * Returns <code>node</code>'s successors.
     * A successor of <code>node</code> is a node which is connected to <code>node</code>
     * by an outgoing edge going from <code>node</code>.
     * @param node the node whose successors are to be returned
     * @return a node iterable of <code>node</code>'s successors
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code>,
     * or if <code>node</code> is not legal in the graph.
     */
    public NodeIterable getSuccessors(Node node);

    /**
     * Returns <code>node</code>'s predecessors.
     * A predecessor of <code>node</code> is a node which is connected to <code>node</code>
     * by an incoming edge going to <code>node</code>.
     * @param node the node whose predecessors are to be returned
     * @return a node iterable of <code>node</code>'s successors
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code>,
     * or if <code>node</code> is not legal in the graph.
     */
    public NodeIterable getPredecessors(Node node);

    /**
     * Returns <code>true</code> if <code>successor</code> is a successor of <code>node</code>.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node which has <code>successor</code> as a successor
     * @param successor the node which has <code>node</code> as a predecessor
     * @return <code>true</code> if <code>successor</code> is a successor of <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> or <code>successor</code> is
     * <code>null</code> of not legal in the graph
     */
    public boolean isSuccessor(Node node, Node successor);

    /**
     * Returns <code>true</code> if <code>predecessor</code> is a predecessor of <code>node</code>.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node which has <code>predecessor</code> as a predecessor
     * @param predecessor the node which has <code>node</code> as a successor
     * @return <code>true</code> if <code>predecessor</code> is a predecessor of <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> or <code>predecessor</code> is
     * <code>null</code> of not legal in the graph
     */
    public boolean isPredecessor(Node node, Node predecessor);

    /**
     * Returns the number of incoming edges incident to <code>node</code>.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node whose indegree is to be returned
     * @return the number of incoming edges incident to <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> of not legal in
     * the graph
     */
    public int getInDegree(Node node);

    /**
     * Returns the number of outgoing edges incident to <code>node</code>.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node whose outdegree is to be returned
     * @return the number of outgoing edges incident to <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> of not legal in
     * the graph
     */
    public int getOutDegree(Node node);

    /**
     * Returns the number of mutual edges incident to <code>node</code>. Edges are
     * considered mutual when both incoming and outgoing edges exists for a same
     * neighbour. Mutual edges are also called bi-directionnal.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node whose outdegree is to be returned
     * @return the number of outgoing edges incident to <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> of not legal in
     * the graph
     */
    public int getMutualDegree(Node node);
}
