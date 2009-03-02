package gephi.data.network.edge;

import gephi.data.network.avl.simple.AVLItem;
import gephi.data.network.node.PreNode;

public class PreEdge implements AVLItem 
{
	private static int AUTO_ID=0;
	public enum EdgeType {
	    IN (1),
	    OUT (2);

	    public final int id;
	    EdgeType(int id) {
	        this.id = id;
	    }
	}

	
	public PreNode minNode;
	public PreNode maxNode;
	public EdgeType edgeType;
	public int cardinal=1;
	public int ID = PreEdge.AUTO_ID++;
	
	public PreEdge(EdgeType edgeType, PreNode minNode, PreNode maxNode)
	{
		if(minNode.pre > maxNode.pre)
		{
			this.minNode = maxNode;
			this.maxNode = minNode;
		}
		else
		{
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
