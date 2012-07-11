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
 * Graph factory that builds node and edges elements. Belongs to a {@link GraphModel}.
 * 
 * @author Mathieu Bastian
 */
public interface GraphFactory {

    /**
     * Create a new node, with default identifier.
     * @return          a new node instance
     */
    public Node newNode();

    /**
     * Create a new node with an identifier. If <code>id</code> is <code>null</code>
     * a default identifier is used.
     * @param id        a unique identifier, could be <code>null</code>
     * @return          a new node instance
     */
    public Node newNode(String id);

    /**
     * Create a new edge. This method don't force the type of edge (directed or
     * undirected). That means it is directed by default but will be considered
     * as undirected if queried from an <code>UndirectedGraph</code>.
     * <p>
     * Edge's weight is 1.0 by default.
     * @param source    the edge's source
     * @param target    the edge's target
     * @return      a new proper edge instance
     */
    public Edge newEdge(Node source, Node target);

    /**
     * Creates a new edge.
     * @param source    the edge's source
     * @param target    the edge's targer
     * @param weight    the edge's weight
     * @param directed  the edge's type
     * @return      a new mixed edge instance
     */
    public Edge newEdge(Node source, Node target, float weight, boolean directed);

    /**
     * Creates a new edge.
     * @param id        a unique identifier, could be <code>null</code>
     * @param source    the edge's source
     * @param target    the edge's targer
     * @param weight    the edge's weight
     * @param directed  the edge's type
     * @return      a new mixed edge instance
     */
    public Edge newEdge(String id, Node source, Node target, float weight, boolean directed);
}
