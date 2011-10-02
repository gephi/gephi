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
package org.gephi.graph.spi;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.MetaEdge;
import org.gephi.graph.api.Node;

/**
 * Responsible for building meta edges weight and attributes from aggregated edges during
 * the process of meta edge creation and deletion.
 * <p>
 * Meta-edges are build from aggregation of edges in lower levels in the hierarchy tree.
 * This builder is responsible of setting the meta edge weight (and possibly attributes)
 * when edges are added/removed from the meta edge.
 * <p>
 * The weight of the meta edge could for instance be the <b>average</b> or the <b>sum</b>
 * of edges' weight. Define your own meta edge builder to control how the weight should
 * be computed.
 * <p>Set the builder by doing:
 * <pre>
 * GraphModel model = ...;
 * model.settings().setMetaEdgeBuilder(builder);
 * </pre>
 *
 * @author Mathieu Bastian
 */
public interface MetaEdgeBuilder {

    /**
     * Adds <code>edge</code> as a <code>metaEdge</code> member.
     * @param edge      the edge added as a member
     * @param source    the edge's source, in the view
     * @param target    the edge's target, in the view
     * @param metaEdge  the meta edge to build
     */
    public void pushEdge(Edge edge, Node source, Node target, MetaEdge metaEdge);

    /**
     * Removes <code>edge</code> as a <code>metaEdge</code> member.
     * @param edge      the edge removed from <code>metaEdge</code>'s members
     * @param source    the edge's source, in the view
     * @param target    the edge's target, in the view
     * @param metaEdge  the meta edge to build
     */
    public void pullEdge(Edge edge, Node source, Node target, MetaEdge metaEdge);
}
