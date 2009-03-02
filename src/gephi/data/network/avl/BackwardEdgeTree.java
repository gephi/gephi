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
import gephi.data.network.edge.PreEdge;
import gephi.data.network.node.PreNode;

/**
 * Type of AVL tree with a special {@link AVLItemAccessor} which always return the <b><code>minNode</code></b>
 * number of the {@link PreEdge} item.
 * <p>
 * This type of tree is used by the {@link PreNode} to store <b>Backward edges</b>. These edges can use
 * <code>minNode</code> or <code>maxNode</code> as a key for this AVL tree. When backward edges are stored
 * in a <code>PreNode</code>, the <code>minNode</code> is actually the <code>PreNode</code> number and the
 * <code>maxNode</code> the neigbours' number. That's why the <code>getNumber()</code> of the tree returns
 * the <code>mixNode</code>.
 * 
 * @see BackwardEdgeTree
 * @author Mathieu Bastian
 */
public class BackwardEdgeTree extends ParamAVLTree<PreEdge> {

	public BackwardEdgeTree()
	{
		super(new AVLItemAccessor<PreEdge>()
				{
			@Override
			public int getNumber(PreEdge item) {
				return item.minNode.pre;
			}
				}
		);
	}
}
