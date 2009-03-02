package gephi.data.network.node.treelist;

import gephi.data.network.TreeStructure;
import gephi.data.network.avl.ResetableIterator;
import gephi.data.network.node.PreNode;
import gephi.data.network.node.treelist.PreNodeTreeList.AVLNode;

import java.util.Iterator;

/**
 * {@link PreNode} iterator for enabled nodes.
 * 
 * @author Mathieu Bastian
 * @see PreNodeTreeList
 */
public class SingleViewTreeIterator implements Iterator<PreNode>, ResetableIterator {
	
	protected int treeSize;
	protected PreNodeTreeList treeList;
	protected int nextIndex;
	protected int diffIndex;;
	protected AVLNode currentNode;
	
	public SingleViewTreeIterator(TreeStructure treeStructure)
	{
		this.treeList = treeStructure.getTree();
		nextIndex=0;
		diffIndex=2;
		treeSize = treeList.size();
	}
	
	public void reset()
	{
		nextIndex=0;
		diffIndex=2;
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
				currentNode=currentNode.next();
			}

			while(!currentNode.value.enabled)
			{
				++nextIndex;
				if(nextIndex >= treeSize)
					return false;
				currentNode=currentNode.next();
			}
			return true;
		}
		return false;
	}
	
	public PreNode next() {
		nextIndex = currentNode.value.getPre()+1+currentNode.value.size;
		diffIndex = nextIndex - currentNode.value.pre;
		return currentNode.value;
	}
	
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
