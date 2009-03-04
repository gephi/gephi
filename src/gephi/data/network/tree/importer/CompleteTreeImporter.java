package gephi.data.network.tree.importer;

import gephi.data.network.TreeStructure;
import gephi.data.network.node.PreNode;
import gephi.data.network.sight.Sight;
import gephi.data.network.sight.SightManager;

/**
 * Hierarchical graph importer. Must import the complete tree.
 * 
 * @author Mathieu Bastian
 */
public class CompleteTreeImporter {

	TreeStructure treeStructure;
	int currentLevel;
	PreNode currentParent;
	int currentSize;
	PreNode lastPos;
	int treeHeight;
	int currentPre=0;
    Sight sight;
	
	public CompleteTreeImporter(TreeStructure tree, SightManager sightManager)
	{
		this.treeStructure = tree;
        sight = sightManager.createSight();
	}
	
	/**
	 * Create the (virtual) root of the tree and prepare import.
	 */
	public void initImport()
	{
		PreNode root = new PreNode(0,0,0,null);
		treeStructure.insertAtEnd(root);
		treeStructure.setRoot(root);
		currentLevel=1;
		currentParent=root;
		currentSize=0;
		lastPos=root;
		treeHeight=0;
	}
	

	/**
	 * Go down into the tree and increase the level. Next <code>addSibling()</code> will be a child 
	 * of the current node.
	 */
	public void addChild()
	{
		currentParent.size = currentSize;
		currentLevel++;
		currentParent= lastPos;
		currentSize=0;
		treeHeight= Math.max(treeHeight, currentLevel);
	}
	

	/**
	 * Add a new node to the current parent. Create the {@link PreNode} object.
	 */
	public void addSibling(int node)
	{
		PreNode p = new PreNode(currentPre, 0, currentLevel, currentParent);
				
		//Insert
		treeStructure.insertAtEnd(p);
		currentSize++;
		lastPos=p;
		currentPre++;
	}
	
	/**
	 * Go up into the tree and decrease the current level.
	 */
	public void closeChild()
	{
		PreNode parent = currentParent;
		parent.size = currentSize;
		if(parent.parent!=null)
		{
			PreNode parentParent = parent.parent;
			parentParent.size+=currentSize;
			currentSize = parentParent.size;
			currentParent=parent.parent;
		}
			
		currentLevel--;
	}
	

	/**
	 * Finish import.
	 */
	public void endImport()
	{
		closeChild();
		treeStructure.treeHeight = treeHeight;
		
		for(PreNode p : treeStructure.getTree())
		{
			p.getPost();
			if(p.size==0)
				p.enabled = true;
			p.addSight(sight);
		}
	}
}
