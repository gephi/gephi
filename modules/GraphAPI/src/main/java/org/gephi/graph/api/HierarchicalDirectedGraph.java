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
 * Hierarchical directed graph.
 * 
 * @author Mathieu Bastian
 * @see GraphModel
 */
public interface HierarchicalDirectedGraph extends HierarchicalGraph, DirectedGraph {

    /**
     * Returns incoming meta edges incident to <code>node</code>.
     * @param node the node whose incoming meta edges are to be returned
     * @return an edge iterable of <code>node</code>'s incoming meta edges
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in
     * the graph
     */
    public EdgeIterable getMetaInEdges(Node node);

    /**
     * Returns incoming edges and meta edges incident to <code>node</code>.
     * @param node the node whose incoming edges and meta edges are to be returned
     * @return an edge iterable of <code>node</code>'s incoming edges and meta edges
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in
     * the graph
     */
    public EdgeIterable getInEdgesAndMetaInEdges(Node node);

    /**
     * Returns outgoing meta edges incident to <code>node</code>.
     * @param node the node whose outgoing meta edges are to be returned
     * @return an edge iterable of <code>node</code>'s outgoing meta edges
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in
     * the graph
     */
    public EdgeIterable getMetaOutEdges(Node node);

    /**
     * Returns outgoing edges and meta edges incident to <code>node</code>.
     * @param node the node whose outgoing edges and meta edges are to be returned
     * @return an edge iterable of <code>node</code>'s outgoing edges and meta edges
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in
     * the graph
     */
    public EdgeIterable getOutEdgesAndMetaOutEdges(Node node);

    /**
     * Returns the number of <code>node</code>'s incoming meta edges.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node whose meta in-degree is queried
     * @return the number of meta edges incoming to <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in
     * the graph.
     */
    public int getMetaInDegree(Node node);

    /**
     * Returns the sum of the in-degree for edges and meta-edge. Equivalent to
     * <code>getInDegree(Node) + getMetaInDegree(Node)</code>.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node whose total in-degree is queried
     * @return the number of edges and meta edges incoming to <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in
     * the graph.
     */
    public int getTotalInDegree(Node node);

    /**
     * Returns the sum of the out-degree for edges and meta-edge. Equivalent to
     * <code>getOutDegree(Node) + getMetaOutDegree(Node)</code>.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node whose total out-degree is queried
     * @return the number of edges and meta edges outgoing from <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in
     * the graph.
     */
    public int getTotalOutDegree(Node node);

    /**
     * Returns the number of <code>node</code>'s outgoing meta edges.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node whose meta out-degree is queried
     * @return the number of meta edges outgoing from <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in
     * the graph.
     */
    public int getMetaOutDegree(Node node);

    /**
     * Finds and returns a meta-edge that connects <code>source</code> and
     * <code>target</code>. Returns <code>null</code> if no such meta-edge is found.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param source the first incident node of the queried edge
     * @param target thge second incident node of the queried edge
     * @return an edge that connects <code>source</code> and <code>target</code>
     * or <code>null</code> if no such edge exists
     * @throws IllegalArgumentException if <code>source</code> or <code>target</code>
     * are <code>null</code> or not legal nodes in the graph
    */
    public MetaEdge getMetaEdge(Node source, Node target);
}
