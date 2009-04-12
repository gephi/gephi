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
package org.gephi.data.network.utils;

import java.util.ArrayList;
import java.util.List;
import org.gephi.data.network.edge.PreEdge;
import org.gephi.data.network.edge.PreEdge.EdgeType;
import org.gephi.data.network.node.PreNode;
import org.gephi.data.network.tree.TreeStructure;


public class RandomEdgesGenerator 
{	
	private TreeStructure treeStructure;
	public boolean DEBUG=false;
	
	public RandomEdgesGenerator(TreeStructure treeStructure)
	{
		this.treeStructure = treeStructure;
	}
	
	public List<PreEdge> generatPhysicalEdges(int number)
	{
		int treeSize = treeStructure.getTreeSize();
		List<PreEdge> edgeList = new ArrayList<PreEdge>();
		
		for(int i=0;i<number;)
		{
			int from = (int)(Math.random()*treeSize);
			int to = (int)(Math.random()*treeSize);
			//int from = i;
			//int to = i+1;
			//i++;
			
			if(from!=to)
			{				
				PreNode pFrom = treeStructure.getNodeAt(from);
				PreNode pTo = treeStructure.getNodeAt(to);
				
				if(pFrom.size==0 && pTo.size==0)
				{
					i++;
					if(from < to)
					{
						//Edge out to target node
						PreEdge p= new PreEdge(EdgeType.OUT, pFrom, pTo);
						if(DEBUG)
							System.out.println(from+"(OUT) -> "+to);
						pFrom.addForwardEdge(p);
						pTo.addBackwardEdge(p);
						edgeList.add(p);
					}
					else
					{
						//Edge in to source node
						PreEdge p= new PreEdge(EdgeType.IN, pTo, pFrom);
						if(DEBUG)
							System.out.println(from+" -> "+to+" (IN)");
						pTo.addForwardEdge(p);
						pFrom.addBackwardEdge(p);
						edgeList.add(p);
					}
				}
			}
		}
		
		return edgeList;
	}
	
}
