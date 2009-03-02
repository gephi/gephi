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

import java.util.List;

import gephi.data.network.TreeStructure;
import gephi.data.network.avl.ResetableIterator;
import gephi.data.network.node.PreNode;

public class SingleViewCachedSpaceTreeIterator extends SingleViewSpaceTreeIterator 
{
	private List<PreNode> cachedList;
	private boolean cacheReady=false;
	
	public SingleViewCachedSpaceTreeIterator(TreeStructure treeStructure, int space)
	{
		super(treeStructure, space);
	}
	
	@Override
	public void reset() {
		
	}
	
	public void setCache(List<PreNode> cache)
	{
		this.cachedList = cache;
		cache.clear();
		cacheReady=false;
	}
	
	@Override
	public boolean hasNext() {
		boolean hasNext = super.hasNext();
		cacheReady=!hasNext;
		return hasNext;
	}
	
	@Override
	public PreNode next() {
		PreNode nextItem = super.next();
		cachedList.add(nextItem);
		return nextItem;
	}

	public boolean isCacheReady() {
		return cacheReady;
	}	
}
