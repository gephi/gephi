package gephi.data.network.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import gephi.data.network.TreeStructure;
import gephi.data.network.edge.PreEdge;
import gephi.data.network.edge.PreEdge.EdgeType;
import gephi.data.network.node.PreNode;

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
