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
package gephi.data.network.cache;

import java.util.ArrayList;
import java.util.Iterator;

import gephi.data.network.TreeStructure;
import gephi.data.network.node.PreNode;
import gephi.data.network.node.treelist.SingleViewCachedSpaceTreeIterator;
import gephi.data.network.node.treelist.SingleViewSpaceTreeIterator;

public class NodeCache implements Iterable<PreNode> {
	
	private ArrayList<PreNode> cacheList;
	private SingleViewCachedSpaceTreeIterator cacheIterator;
	private boolean reset=true;
	private TreeStructure treeStructure;
	private int space;
	
	public NodeCache(TreeStructure treeStructure, int space)
	{
		this.treeStructure = treeStructure;
		this.space = space;
		cacheList = new ArrayList<PreNode>();
		cacheIterator = new SingleViewCachedSpaceTreeIterator(treeStructure, space);
	}
	
	public void reset()
	{
		reset=true;
	}
	
	@Override
	public Iterator<PreNode> iterator() {
		if(reset)
		{
			cacheIterator.setCache(cacheList);
			return cacheIterator;
		}
		else
		{
			if(cacheIterator.isCacheReady())
			{
				return cacheList.iterator();
			}
			else
			{
				return new SingleViewSpaceTreeIterator(treeStructure, space);
			}
		}
	}	
}
