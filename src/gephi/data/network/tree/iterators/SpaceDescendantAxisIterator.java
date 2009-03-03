package gephi.data.network.tree.iterators;

import gephi.data.network.TreeStructure;
import gephi.data.network.node.PreNode;
import gephi.data.network.sight.Sight;

public class SpaceDescendantAxisIterator extends DescendantAxisIterator {

	protected Sight sight;
	protected PreNode pointer;
	public SpaceDescendantAxisIterator(TreeStructure treeStructure, Sight sight, PreNode[] contextNodes)
	{
		super(treeStructure, contextNodes);
		this.sight = sight;
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
				
				while(currentPre <= currentLimit)
				{
					pointer = treeStructure.getNodeAt(currentPre);
					if(pointer.isInSight(sight))
					{
						return true;
					}
					else
					{
						currentPre = pointer.pre + pointer.size +1;
						
					}
				}
			}
		}
		
		pointer = treeStructure.getNodeAt(currentPre);

		return true;
	}
}
