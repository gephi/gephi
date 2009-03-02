package gephi.data.network.space;

import java.util.Iterator;
import java.util.LinkedList;

import gephi.data.network.TreeStructure;
import gephi.data.network.node.PreNode;

public class SpaceCache implements Iterable<PreNode>
{

	protected TreeStructure treeStructure;
	protected int spaceNum;
	protected boolean reset=true;
	protected LinkedList<PreNode> cache;
	
	public SpaceCache(TreeStructure treeStructure, int spaceNum)
	{
		this.treeStructure=treeStructure;
		this.spaceNum = spaceNum;
		cache = new LinkedList<PreNode>();
	}
	
	public Iterator<PreNode> iterator() {
		if(reset)
		{
			cache.clear();
			//SingleViewCachedSpaceTreeIterator itr = new SingleViewCachedSpaceTreeIterator(treeStructure, spaceNum, cache);
			reset=false;
			return null;
		}		
		
		return cache.iterator();
	}
	
	public void reset()
	{
		reset=true;
	}
}
