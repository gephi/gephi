/*
Copyright 2008-2009 Gephi
Authors : Helder Suzuki <heldersuzuki@gmail.com>
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
import org.gephi.graph.api.EdgeData;
import org.gephi.graph.api.Node;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
public class EdgeImpl implements Edge {

    private Node source,  target;

    public EdgeImpl(Node source, Node target) {
        this.source = source;
        this.target = target;
    }

    public Node N1() {
        return source;
    }

    public Node N2() {
        return target;
    }

    public int getId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Node getSource() {
        return source;
    }

    public Node getTarget() {
        return target;
    }

    public float getWeight() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isVisible() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isDirected() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EdgeData getEdgeData() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean equals(Object obj) {
        Edge e = (Edge) obj;
        return (e.getSource() == getSource() && e.getTarget() == getTarget()) ||
               (e.getSource() == getTarget() && e.getTarget() == getSource());
    }

    @Override
    public int hashCode() {
        return 10 * source.hashCode() + target.hashCode();
    }

    public boolean isSelfLoop() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}