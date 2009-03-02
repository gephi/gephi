package gephi.data.network.node.treelist;

import gephi.data.network.node.PreNode;
import gephi.data.network.node.treelist.PreNodeTreeList.AVLNode;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;


/**
 * Basic iterator for the {@link PreNodeTreeList}.
 * 
 * @author Mathieu Bastian
 */
public class PreNodeTreeListIterator implements Iterator<PreNode>
{
    /** The TreeList list */
    protected final PreNodeTreeList treeList;
    /**
     * Cache of the next node that will be returned by {@link #next()}.
     */
    protected AVLNode next;
    /**
     * The index of the last node that was returned.
     */
    protected int nextIndex;


    /**
     * Create a ListIterator for a list.
     * 
     * @param parent  the parent list
     * @param fromIndex  the index to start at
     */
    public PreNodeTreeListIterator(PreNodeTreeList treeList, int fromIndex) throws IndexOutOfBoundsException {
        super();
        this.treeList = treeList;
        this.nextIndex = fromIndex;
    }
    
    public PreNodeTreeListIterator(PreNodeTreeList treeList) throws IndexOutOfBoundsException {
    	this(treeList, 0);
    }


    public boolean hasNext()
    {    	
        return (nextIndex < treeList.size);
    }

    public PreNode next() 
    {
        if (next == null) {
            next = treeList.root.get(nextIndex);
        } else {
            next = next.next();
        }
        
        PreNode value = next.value;
        value.avlNode.setIndex(nextIndex);
        ++nextIndex;
        return value;
    }
    
    @Override
    public void remove() {
    	throw new UnsupportedOperationException();
    }
}
