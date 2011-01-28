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

package org.gephi.filters.spi;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

/**
 * Basic filters for nodes, that works as predicates. For a given node the filter's
 * role is to return <code>true</code> if the node is kept or <code>false</code>
 * if it is removed.
 * 
 * @author Mathieu Bastian
 */
public interface NodeFilter extends Filter {

    public boolean init(Graph graph);

    public boolean evaluate(Graph graph, Node node);

    public void finish();
}
