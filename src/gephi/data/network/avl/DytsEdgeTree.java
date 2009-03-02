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
package gephi.data.network.avl;

import gephi.data.network.avl.param.AVLItemAccessor;
import gephi.data.network.avl.param.ParamAVLTree;
import gephi.data.network.edge.DytsEdge;
import gephi.data.network.edge.VirtualEdge;
import gephi.data.network.node.PreNode;

/**
 * Special type of tree which knows his {@link PreNode} owner. The {@link AVLItemAccessor} always return
 * the number of the <code>PreNode</code> linked to the owner.
 * <p>
 * This type of tree stores {@link DytsEdge}. These edges can be <b>IN</b> or <b>OUT</b>. The instance
 * of the edge is duplicated in each node, once as <b>IN</b> and once as <b>OUT</b>. In each node, the
 * tree key must be the neigbour's number. So the <code>getNumber()</code> method compare the given
 * item with the owner and returns the neighbour's number.
 * 
 * @author Mathieu Bastian
 */
public class DytsEdgeTree extends ParamAVLTree<DytsEdge> {

	private PreNode owner;
	
	public DytsEdgeTree(PreNode owner)
	{
		super();
		this.owner = owner;
		setAccessor(new DytsEdgeAVLItemAccessor());
	}
	
	private class DytsEdgeAVLItemAccessor implements AVLItemAccessor<DytsEdge>
	{
		@Override
		public int getNumber(DytsEdge item) {
			if(item.getPreNodeFrom()==owner)
				return item.getPreNodeTo().getNumber();
			else
				return item.getPreNodeFrom().getNumber();
		}
	}
}
