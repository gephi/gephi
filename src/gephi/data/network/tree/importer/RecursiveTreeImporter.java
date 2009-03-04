/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package gephi.data.network.tree.importer;

import gephi.data.network.TreeStructure;
import gephi.data.network.node.PreNode;
import gephi.data.network.sight.Sight;
import gephi.data.network.sight.SightManager;

public class RecursiveTreeImporter {

	public int childMax=3;
	TreeStructure treeStructure;
	int treeHeight;
	PreNode root;
	public int levelLimit=10;
	private Sight sight;

	public RecursiveTreeImporter(TreeStructure tree)
	{
		this.treeStructure = tree;
        SightManager sightManager = new SightManager();
        sight = sightManager.createSight();
	}
	
	public void initImport()
	{
		treeHeight=0;
	}

	public void importTree()
	{
		int size = addNode(null, 0);
	}

	private int addNode(PreNode parent, int pre)
	{
		int level = 0;
		if(parent!=null)
		{
			level = parent.level+1;
		}
		
		PreNode node = new PreNode(pre,0,level,parent);
		if(parent==null)
			root=node;
		
		treeHeight= Math.max(treeHeight, node.level);
		
		int sizeChild=0;
		
		int numChild;
		if(level==levelLimit)
			numChild=0;
		else if(level<3)
			numChild = (int)(Math.random()*childMax)+1;
		else
			numChild = (int)(Math.random()*childMax);
		
		int currentPre = pre;
		for (int i=0; i<numChild;i++)
		{
			currentPre++;
			int size = addNode(node, currentPre);

			sizeChild+=size;
			currentPre+=size-1;
		}
		
		node.size = sizeChild;
		node.getPost();
		
		//treeStructure.insert(node);
		return sizeChild+1;
	}
	

	public void endImport()
	{
		treeStructure.treeHeight = treeHeight;
		treeStructure.setRoot(root);
		
		for(PreNode p : treeStructure.getTree())
		{
			p.getPost();
			if(p.size==0)
				p.setEnabled(sight, true);
			p.addSight(sight);
		}
	}
}
