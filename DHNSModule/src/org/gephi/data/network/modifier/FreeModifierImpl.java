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
package org.gephi.data.network.modifier;

import org.gephi.data.network.api.FreeModifier;
import org.gephi.data.network.api.Sight;
import org.gephi.data.network.edge.EdgeImpl;
import org.gephi.data.network.node.NodeImpl;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

/**
 *
 * @author Mathieu Bastian
 */
public class FreeModifierImpl implements FreeModifier {

    public void expand(Node node, Sight sight) {
        NodeImpl nodeImpl = (NodeImpl) node;
    }

    public void retract(Node node, Sight sight) {
        NodeImpl nodeImpl = (NodeImpl) node;
    }

    public void addNode(Node node) {
        NodeImpl nodeImpl = (NodeImpl) node;
    }

    public void deleteNode(Node node) {
        NodeImpl nodeImpl = (NodeImpl) node;
    }

    public void addEdge(Edge edge) {
        EdgeImpl edgeImpl = (EdgeImpl) edge;
    }

    public void deleteEdge(Edge edge) {
        EdgeImpl edgeImpl = (EdgeImpl) edge;
    }

}
