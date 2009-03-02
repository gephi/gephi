package gephi.data.network;

import gephi.data.network.avl.typed.PreNodeAVLTree;
import gephi.data.network.avl.typed.PreNodeAVLTree.PreNodeAVLIterator;
import gephi.data.network.node.PreNode;
import gephi.data.network.node.treelist.PreNodeTreeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;


public class TreeStructure
{
	PreNodeTreeList tree;
	PreNode root;
	public int treeHeight;
	
	private PreNode cacheNode;
	
	public TreeStructure()
	{
		tree = new PreNodeTreeList();
	}
	
	public TreeStructure(int treeCapacity)
	{
		tree = new PreNodeTreeList();
	}
	
	public PreNode getNodeAt(int pre)
	{
		/*if(cacheNode!=null && cacheNode.avlNode.isConsistent() && cacheNode.pre == pre-1)
		{
			cacheNode = cacheNode.avlNode.next().getValue();
			return cacheNode;
		}
		
		cacheNode = tree.get(pre);
		return cacheNode;*/
		return tree.get(pre);
	}
	
	public PreNode getEnabledAncestorOrSelf(PreNode node)
	{
		PreNode parent = node;
		while(!parent.enabled)
		{
			parent = parent.parent;
			if(parent==null || parent.pre==0)
				return null;
		}
		return parent;
	}
	
	public PreNode getEnabledAncestor(PreNode node)
	{
		PreNode parent = node.parent;
		while(!parent.enabled)
		{
			if(parent.pre==0)
				return null;
			parent = parent.parent;
		}
		return parent;
	}
	
	public void insertAtEnd(PreNode node)
	{
		node.pre = tree.size();
		
		tree.add(node);
	}
	
	
	public void insertAsChild(PreNode node, PreNode parent)
	{
		node.parent = parent;
		node.pre = parent.pre+parent.size+1;
		node.level = parent.level+1;
		if(node.level > treeHeight)
			treeHeight++;
		
		tree.add(node.pre, node);
		incrementAncestorsSize(node);
	}
	
	
	public void deleteAtPre(PreNode node)
	{
		int pre = node.getPre();
		tree.remove(pre);
		for(int i=0;i<node.size;i++)
			tree.remove(pre);
		
	}
	
	public void deleteDescendantAndSelf(PreNode node)
	{
		deleteAtPre(node);
		decrementAncestorSize(node, node.size+1);
	}
	
	public void incrementAncestorsSize(PreNode node)
	{
		while(node.parent!=null)
		{
			node = node.parent;
			node.size++;
			node.getPost();
		}
	}
	
	public void decrementAncestorSize(PreNode node, int shift)
	{
		while(node.parent!=null)
		{
			node = node.parent;
			node.size-=shift;
			node.getPost();
		}
	}
	
	public void resetAllEnabled()
	{
		for(PreNode p : tree)
		{
			p.enabled=false;
		}
	}
	
	public void showTreeAsTable()
	{
		System.out.println("pre\tsize\tlevel\tparent\tpost\tenabled\tpreTrace");
		System.out.println("-------------------------------------------------------");
		
		int pre=0;
		for(PreNode p : tree)
		{
			System.out.println(p.pre+"\t"+p.size+"\t"+p.level+"\t"+p.parent+"\t"+p.post+"\t"+p.enabled+"\t"+p.preTrace);
			pre++;
		}
	}
	
	public int getTreeSize()
	{
		return tree.size();
	}

	public PreNodeTreeList getTree() {
		return tree;
	}

	public PreNode getRoot() {
		return root;
	}

	public void setRoot(PreNode root) {
		this.root = root;
	}
}
