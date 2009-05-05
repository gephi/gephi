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

import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeLayoutInterface;

/**
 *
 * @author Mathieu Bastian
 */
public class NodeLayout implements NodeLayoutInterface {

    //Architecture
    public Node node;

    //Data
    public float dx = 0;
    public float dy = 0;
    public float old_dx = 0;
    public float old_dy = 0;
    public boolean fixed = false;
    public float freeze = 0f;

    public NodeLayout(Node node) {
        this.node = node;
    }

    public float x() {
        return node.x();
    }

    public float y() {
        return node.y();
    }

    public float size() {
        return node.getSize();
    }

    public void setX(float x) {
        node.setX(x);
    }

    public void setY(float y) {
        node.setY(y);
    }

    public int getNeighboursCount() {
        return node.countNeighbours();
    }
}
