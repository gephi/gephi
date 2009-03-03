package gephi.data.network.tree.iterators;

import gephi.data.network.TreeStructure;
import gephi.data.network.node.PreNode;
import gephi.data.network.node.treelist.SingleTreeIterator;

public class DescendantAxisIterator extends SingleTreeIterator {

	protected PreNode[] contextNodes;
	protected int currentContextIndex=0;
	protected int currentPre;
	protected int currentLimit;
	protected int currentPost;
	protected PreNode pointer;
	protected TreeStructure treeStructure;
	public DescendantAxisIterator(TreeStructure treeStructure, PreNode[] contextNodes)
	{
		super(treeStructure,null);
		this.contextNodes = contextNodes;
		PreNode contextNode = contextNodes[currentContextIndex];
		currentPre = contextNode.pre;
		currentPost = contextNode.post;
		currentLimit = contextNode.pre + contextNode.size;
	}
	
	@Override
	public boolean hasNext()
	{
		currentPre++;
		
		//Change context node phase
		while(currentPre > currentLimit)
		{
			currentContextIndex++;
			if(currentContextIndex==contextNodes.length)			//No more context nodes
				return false;
			PreNode contextNode = contextNodes[currentContextIndex];
			
			if(contextNode.post <= currentPost)						//Pruning
			{
				currentLimit=0;										//Skip this context node
			}
			else
			{
				currentPre = contextNode.pre+1;
				currentPost = contextNode.post;
				currentLimit = contextNode.pre + contextNode.size;
			}
		}
		
		pointer = treeStructure.getNodeAt(currentPre);
		return true;
	}
}
