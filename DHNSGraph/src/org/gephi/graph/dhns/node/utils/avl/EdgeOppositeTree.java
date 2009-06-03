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
package org.gephi.graph.dhns.node.utils.avl;

import org.gephi.graph.dhns.edge.ProperEdgeImpl;
import org.gephi.graph.dhns.node.PreNode;
import org.gephi.datastructure.avl.param.AVLItemAccessor;
import org.gephi.datastructure.avl.param.ParamAVLTree;
import org.gephi.graph.dhns.edge.AbstractEdge;

/**
 * Special type of tree which knows his {@link PreNode} owner. The <code>AVLItemAccessor</code> always
 * return the number of the <code>PreNode</code> linked to the owner.
 * <p>
 * This type of tree stores {@link AbstractEdge}. These edges can be <b>IN</b> or <b>OUT</b>. The instance
 * of the edge is duplicated in each node, once as <b>IN</b> and once as <b>OUT</b>. In each node, the
 * tree key must be the neigbour's number. So the <code>getNumber()</code> method compare the given
 * item with the owner and returns the neighbour's number.
 * 
 * @author Mathieu Bastian
 */
public class EdgeOppositeTree extends ParamAVLTree<AbstractEdge> {

    private PreNode owner;

    public EdgeOppositeTree(PreNode owner) {
        super();
        this.owner = owner;
        setAccessor(new EdgeOppositeImplAVLItemAccessor());
    }

    public PreNode getOwner() {
        return owner;
    }

    public boolean hasNeighbour(PreNode node) {
        return getItem(node.getNumber()) != null;
    }

    private class EdgeOppositeImplAVLItemAccessor implements AVLItemAccessor<AbstractEdge> {

        @Override
        public int getNumber(AbstractEdge item) {
            if (item.getSource() == owner) {
                return item.getTarget().getNumber();
            } else {
                return item.getSource().getNumber();
            }
        }
    }
}
