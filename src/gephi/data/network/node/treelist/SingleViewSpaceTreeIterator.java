package gephi.data.network.node.treelist;

import gephi.data.network.TreeStructure;
import gephi.data.network.node.PreNode;


/**
 * {@link PreNode} iterator for the given space. returns enabled nodes of the given space number.
 * 
 * @author Mathieu Bastian
 * @see PreNodeTreeList
 */
public class SingleViewSpaceTreeIterator extends SingleViewTreeIterator {

	protected int space;
	
	public SingleViewSpaceTreeIterator(TreeStructure treeStructure, int space)
	{
		super(treeStructure);
		this.space = space;
	}
	
	public boolean hasNext() 
	{
		if(nextIndex < treeSize)
		{
			if(diffIndex > 1)
			{
				currentNode = treeList.root.get(nextIndex);
			}
			else
			{
				currentNode = currentNode.next();
			}
		
			while(!currentNode.value.enabled || currentNode.value.space!=space)
			{
				if(currentNode.value.enabled)
				{
					nextIndex = currentNode.value.pre+1+currentNode.value.size;
					if(nextIndex >= treeSize)
						return false;
					currentNode = treeList.root.get(nextIndex);
				}
				else
				{
					++nextIndex;
					if(nextIndex >= treeSize)
						return false;
					currentNode=currentNode.next();
				}
				
			}
			return true;
		}
		return false;
	}
}
