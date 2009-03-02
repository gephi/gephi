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
package gephi.data.network.node.treelist;

import gephi.data.network.TreeStructure;

public class ManualViewSpaceTreeIterator extends ManualViewTreeIterator 
{
	protected int space;
	
	public ManualViewSpaceTreeIterator(TreeStructure treeStructure, int space)
	{
		super(treeStructure);
		this.space = space;
	}
	
	@Override
	public boolean hasNext() 
	{
		if(nextIndex >= treeSize)
			return false;
		
		while(nextIndex < treeSize && !pointer.enabled && pointer.space==space)
		{
			pointer = pointer.avlNode.next().value;
			nextIndex++;
		}
		return true;
	}
}
