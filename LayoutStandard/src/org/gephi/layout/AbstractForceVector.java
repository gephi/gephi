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
package org.gephi.layout;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeLayoutInterface;
import org.gephi.graph.api.LayoutDataFactory;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeLayoutInterface;
import org.gephi.layout.api.Layout;

/**
 *
 * @author Mathieu Bastian
 */
public abstract class AbstractForceVector implements Layout<NodeLayout, EdgeLayout>, LayoutDataFactory {

    public LayoutDataFactory getLayoutDataFactory() {
        return this;
    }

    public EdgeLayoutInterface getEdgeLayout(Edge edge) {
        return new EdgeLayout(edge);
    }

    public NodeLayoutInterface getNodeLayout(Node node) {
        return new NodeLayout(node);
    }
}
