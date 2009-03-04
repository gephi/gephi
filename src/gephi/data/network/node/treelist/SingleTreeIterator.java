package gephi.data.network.node.treelist;

import gephi.data.network.TreeStructure;
import gephi.data.network.avl.ResetableIterator;
import gephi.data.network.node.PreNode;
import gephi.data.network.node.treelist.PreNodeTreeList.AVLNode;

import gephi.data.network.sight.Sight;
import java.util.Iterator;

/**
 * {@link PreNode} iterator for enabled nodes.
 * 
 * @author Mathieu Bastian
 * @see PreNodeTreeList
 */
public class SingleTreeIterator implements Iterator<PreNode>, ResetableIterator {
	
	protected int treeSize;
	protected PreNodeTreeList treeList;
	protected int nextIndex;
	protected int diffIndex;;
	protected AVLNode currentNode;

    protected Sight sight;
	
	public SingleTreeIterator(TreeStructure treeStructure, Sight sight)
	{
		this.treeList = treeStructure.getTree();
		nextIndex=0;
		diffIndex=2;
		treeSize = treeList.size();

        this.sight = sight;
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
				currentNode = currentNode.next();
			}

			while(!currentNode.value.isEnabled(sight) || !currentNode.value.isInSight(sight))
			{
				if(currentNode.value.isEnabled(sight))
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
	
	public PreNode next() {
		nextIndex = currentNode.value.getPre()+1+currentNode.value.size;
		diffIndex = nextIndex - currentNode.value.pre;
		return currentNode.value;
	}
	
	public void remove() {
		throw new UnsupportedOperationException();
	}

     public void setSight(Sight sight) {
        this.sight = sight;
    }
}
