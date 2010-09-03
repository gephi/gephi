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
