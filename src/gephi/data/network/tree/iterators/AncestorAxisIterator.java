package gephi.data.network.tree.iterators;

import gephi.data.network.TreeStructure;
import gephi.data.network.node.PreNode;
import gephi.data.network.node.treelist.SingleViewTreeIterator;

public class AncestorAxisIterator extends SingleViewTreeIterator 
{
	protected PreNode[] contextNodes;
	protected PreNode nextContextNode;
	protected int currentContextIndex=1;
	protected int currentPre;
	protected int currentPost;
	protected int nextPre=0;
	protected int nextPost=0;
	protected PreNode pointer;
	protected TreeStructure treeStructure;
	public AncestorAxisIterator(TreeStructure treeStructure, PreNode[] contextNodes)
	{
		super(treeStructure);
		this.contextNodes = contextNodes;
		PreNode contextNode = contextNodes[0];
		currentPre = contextNode.parent.pre;
		currentPost = contextNode.post;
		if(contextNodes.length > 1)
		{
			nextContextNode = contextNodes[1];
			nextPre=nextContextNode.pre;
			nextPost = nextContextNode.post;
		}
		
	}
	
	@Override
	public boolean hasNext() 
	{
		if(currentPre==-1)
			return false;

		while(currentPre < nextPre)
		{
			if(nextPost < currentPost)
			{
				currentPre = nextContextNode.parent.pre;
				currentPost = nextPost;
			}
			
			currentContextIndex++;
			if(contextNodes.length <= currentContextIndex)
			{
				//No more context nodes
				nextPre = 0;
				nextContextNode = null;
			}
			else
			{
				nextContextNode = contextNodes[currentContextIndex];
				nextPre = nextContextNode.pre;
				nextPost = nextContextNode.post;
			}
		}
		
		pointer = treeStructure.getNodeAt(currentPre);
		currentPre = pointer.parent.pre;
		
		return true;
	}
}
