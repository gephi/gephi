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
package org.gephi.data.network.edge;

import org.gephi.data.network.node.PreNode;
import org.gephi.datastructure.avl.simple.AVLItem;

/**
 *
 * @author Mathieu Bastian
 */
public class PreEdge implements AVLItem {

    private static int AUTO_ID = 0;

    public enum EdgeType {

        IN(1),
        OUT(2);
        public final int id;

        EdgeType(int id) {
            this.id = id;
        }
    }
    public PreNode minNode;
    public PreNode maxNode;
    public EdgeType edgeType;
    public int cardinal = 1;
    public int ID = PreEdge.AUTO_ID++;

    public PreEdge(EdgeType edgeType, PreNode minNode, PreNode maxNode) {
        if (minNode.pre > maxNode.pre) {
            this.minNode = maxNode;
            this.maxNode = minNode;
        } else {
            this.minNode = minNode;
            this.maxNode = maxNode;
        }
        this.edgeType = edgeType;
    }

    @Override
    public int getNumber() {
        return ID;
    }
}
