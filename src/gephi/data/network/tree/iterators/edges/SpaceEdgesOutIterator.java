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
package gephi.data.network.tree.iterators.edges;

import java.util.Iterator;


import gephi.data.network.TreeStructure;
import gephi.data.network.avl.param.ParamAVLIterator;
import gephi.data.network.edge.VirtualEdge;
import gephi.data.network.node.PreNode;
import gephi.data.network.node.treelist.SingleViewSpaceTreeIterator;

/**
 * Edge Iterator for <b>OUT</b> edges of the tree. Use a {@link SingleViewSpaceTreeIterator} for getting edges
 * from the tree nodes.
 * 
 * @author Mathieu Bastian
 */
public class SpaceEdgesOutIterator implements Iterator<VirtualEdge> {

	protected SingleViewSpaceTreeIterator spaceTreeIterator;
	protected ParamAVLIterator<VirtualEdge> edgeIterator;
	protected PreNode currentNode;
	protected VirtualEdge pointer;
	
	public SpaceEdgesOutIterator(TreeStructure treeStructure, int space)
	{
		spaceTreeIterator= new SingleViewSpaceTreeIterator(treeStructure, space);
		edgeIterator = new ParamAVLIterator<VirtualEdge>();
	}
	
	@Override
	public boolean hasNext()
	{
		while(!edgeIterator.hasNext())
		{
			if(spaceTreeIterator.hasNext())
			{
				currentNode = spaceTreeIterator.next();
				edgeIterator.setNode(currentNode.getVirtualEdgesOUT());
			}
			else
				return false;
		}
		
		//pointer = edgeIterator.next();
		return true;
	}
	
	@Override
	public VirtualEdge next()
	{
		return pointer;
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
