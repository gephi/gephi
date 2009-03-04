package gephi.data.network.tests;

import java.awt.event.ActionEvent;
import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;

import gephi.data.network.Benchmarking;
import gephi.data.network.Dyts;
import gephi.data.network.TreeStructure;
import gephi.data.network.avl.BackwardEdgeTree;
import gephi.data.network.avl.simple.SimpleAVLTree;
import gephi.data.network.avl.typed.PreNodeAVLTree;
import gephi.data.network.edge.EdgeProcessing;
import gephi.data.network.edge.PreEdge;
import gephi.data.network.edge.VirtualEdge;
import gephi.data.network.edge.PreEdge.EdgeType;
import gephi.data.network.node.PreNode;
import gephi.data.network.node.treelist.PreNodeTreeList;
import gephi.data.network.node.treelist.SingleTreeIterator;
import gephi.data.network.sight.SightManager;
import gephi.data.network.sight.Sight;
import gephi.data.network.space.SpaceCache;
import gephi.data.network.tree.importer.CompleteTreeImporter;
import gephi.data.network.tree.importer.RecursiveTreeImporter;
import gephi.data.network.tree.iterators.AncestorAxisIterator;
import gephi.data.network.tree.iterators.DescendantAxisIterator;
import gephi.data.network.tree.iterators.SpaceDescendantAxisIterator;
import gephi.data.network.tree.iterators.edges.EdgesOutIterator;
import gephi.data.network.viz.TreeViz;
import gephi.data.network.viz.ControlPanel.ActionType;

public class Test1 {

	private Dyts dyts;
    private SightManager sightManager = new SightManager();
    private Sight sightZero;
	
	public Test1()
	{
		
		
		dyts = new Dyts();
		
		TreeStructure treeStructure = dyts.getTreeStructure();
		CompleteTreeImporter importer = new CompleteTreeImporter(treeStructure,sightManager);
		//RecursiveTreeImporter importer = new RecursiveTreeImporter(treeStructure);
		
		importGraph(importer, NUMBER_SIBLINGS, true);
		shuffleEnable(treeStructure);
		System.out.println("Tree size : "+treeStructure.getTreeSize());

        sightZero = sightManager.getSight(1);

        TreeViz treeViz = new TreeViz(sightZero);

		//treeStructure.showTreeAsTable();
		
		//testAVLRemove();
		
		//testDynamicNodes(treeStructure, treeViz);
		
		//testIteratorSpeed(treeStructure);
		
		//testSpaceCache(treeStructure);
		//treeStructure.resetAllEnabled();
		
		//testDescendentAxis(treeStructure);
		//testAncestorAxis(treeStructure);
		
		//treeViz.showTree(treeStructure);
		//testDeleteDescendantAndSelf(treeStructure, treeViz);
		
		//testAddPhysicalEdges(treeStructure);
		//testAVLIterator();
		//testPhysicalEdgesIterator(treeStructure);
		//testVirtualEdgesIterator(treeStructure);
		//testReprocessInducesEdges(treeStructure, treeViz);
		testComplete(treeStructure, treeViz, true);
		//testDeleteNode(treeStructure);
		//testDeleteNodes(treeStructure, treeViz);
		//testDeleteEdges(treeStructure);
		//testAddEdges(treeStructure);
		//testScenario1(treeStructure);
		//testScenario2(treeStructure);
		//testScenario3(treeStructure);
		//testScenario4(treeStructure);
		//testScenario5();
		//testScenario6();
	}
	
	private static int NUMBER_SIBLINGS=4;
	private static int NUMBER_EDGES=50000;
	
	public void importGraph(CompleteTreeImporter treeImporter, int numSibling, boolean random)
	{
		treeImporter.initImport();
		
		int counter = 0;
		
		for(int i=0;i<numSibling;i++)
		{
			counter++;
			
			treeImporter.addSibling(counter);
			treeImporter.addChild();
			
			int rand = numSibling;
			if(random)
				rand = (int)(Math.random()*numSibling+1);
			
			for(int j=0;j<rand;j++)
			{
				counter++;
				
				treeImporter.addSibling(counter);
				treeImporter.addChild();
				
				int rand2 = numSibling;
				if(random)
					rand2 = (int)(Math.random()*numSibling+1);
				
				for(int k=0;k<rand2;k++)
				{
					counter++;
					treeImporter.addSibling(counter);
				}
				
				treeImporter.closeChild();
			}
			
			treeImporter.closeChild();
		}
		
		treeImporter.endImport();			
	}
	
	public void importGraph(RecursiveTreeImporter treeImporter)
	{
		treeImporter.childMax = NUMBER_SIBLINGS;
		treeImporter.initImport();
		treeImporter.importTree();
		treeImporter.endImport();
	}
	
	public void shuffleEnable(TreeStructure treeStructure)
	{
		for(PreNode p : treeStructure.getTree())
		{
			p.enabled = false;
		}
		
		for(int i=1;i<treeStructure.getTreeSize();i++)
		{
			int enabled = (int)Math.round(Math.random());
			if(enabled==0)
			{
				PreNode n = treeStructure.getNodeAt(i);
				n.enabled = false;
			}
			else if(enabled==1)
			{
				PreNode n = treeStructure.getNodeAt(i);
				n.enabled = true;
				i+=n.size;
			}
		}
	}
	
	public void testIteratorSpeed(TreeStructure treeStructure)
	{
		//Prepare comparaison
		List<PreNode> linkedList = new LinkedList<PreNode>();
		int enabled=0;
		for(PreNode p : treeStructure.getTree())
		{
			if(p.enabled)
			{
				enabled++;
				linkedList.add(new PreNode(p.pre,0,0,null));
			}
		}
		System.out.println("enabled "+enabled);
		List<PreNode> arrayList = new ArrayList<PreNode>();
		for(PreNode p : treeStructure.getTree())
		{
			if(p.enabled)
			{
				arrayList.add(new PreNode(p.pre,0,0,null));
			}
		}
		
		Benchmarking bench = new Benchmarking(4, 10);
		
		//TreeIterator
		for(int t=0;t<10;t++)
		{
			
			//Tree
			bench.startSubject(0);
			SingleTreeIterator treeIterator = new SingleTreeIterator(treeStructure,sightZero);
			int c1 = 0;
			for(;treeIterator.hasNext();)
			{
				PreNode p = treeIterator.next();
				c1++;
			}
			bench.stopSubject(0);
		}
		
		//Space Iterator
		for(int t=0;t<10;t++)
		{
			
			//Space iterator
			bench.startSubject(1);
			SingleTreeIterator treeIterator = new SingleTreeIterator(treeStructure,sightZero);
			int c1 = 0;
			for(;treeIterator.hasNext();)
			{
				PreNode p = treeIterator.next();
				c1++;
			}
			bench.stopSubject(1);
		}
		
		
		for(int t=0;t<10;t++)
		{
			//Table
			bench.startSubject(2);
			int c2 = 0;
			for(PreNode p : linkedList)
			{
				c2++;
			}
			bench.stopSubject(2);
			System.out.println("c2 "+c2);
		}
		
		for(int t=0;t<10;t++)
		{
			//Table
			bench.startSubject(3);
			int c2 = 0;
			for(PreNode p : arrayList)
			{
				c2++;
			}
			bench.stopSubject(3);
			System.out.println("c2 "+c2);
		}
		
		bench.showResults();
	}
	
	
	public void testDynamicNodes(TreeStructure treeStructure, TreeViz treeViz)
	{
		Benchmarking bench = new Benchmarking(1, 2000);
		for(int i=0;i<200;i++)
		{
			int p = (int)(Math.random()*treeStructure.getTreeSize());
			PreNode parent = treeStructure.getNodeAt(p);
			PreNode preNode = new PreNode(p,0,0,null);
			bench.startSubject(0);
			treeStructure.insertAsChild(preNode,parent);
			bench.stopSubject(0);
			treeViz.showTree(treeStructure);

			try
			{
				Thread.sleep(500);
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			
		}
		
		bench.showResults();
	}
	
	public void testSpaceCache(TreeStructure treeStructure)
	{		
		SpaceCache spaceCache = new SpaceCache(treeStructure, 0);
		
		Benchmarking bench = new Benchmarking(2, 1);
		bench.startSubject(0);
		for(int t=0;t<100;t++)
		{
			//Space iterator
			
			SingleTreeIterator treeIterator = new SingleTreeIterator(treeStructure,sightZero);
			int c1 = 0;
			for(;treeIterator.hasNext();)
			{
				PreNode p = treeIterator.next();
				c1++;
			}
			
		}
		bench.stopSubject(0);
		
		bench.startSubject(1);
		for(int t=0;t<100;t++)
		{
			//Space cache
			
			int c2 = 0;
			for(PreNode p : spaceCache)
			{
				c2++;
			}
			
		}
		bench.stopSubject(1);
		
		bench.showResults();
	}
	
	public void testDescendentAxis(TreeStructure treeStructure)
	{
		Benchmarking bench = new Benchmarking(1, 1);
		
		final int numberContextNodes = 10;
		PreNode[] contextNodes = new PreNode[numberContextNodes];
		for(int i=0; i<numberContextNodes;i++)
		{
			int pre = (int)(Math.random()*treeStructure.getTreeSize()-1);
			contextNodes[i] = treeStructure.getNodeAt(pre);
			
			if(contextNodes[i].size ==0)
				i--;
			else
			{
				contextNodes[i].enabled = true;
				System.out.println(contextNodes[i].pre);
			}
				
		}
		
		bench.startSubject(0);
		for(int t=0;t<1000;t++)
		{
			SpaceDescendantAxisIterator itr = new SpaceDescendantAxisIterator(treeStructure,sightZero,contextNodes);
			int c1=0;
			for(;itr.hasNext();){
				PreNode p = itr.next();
				p.enabled=true;
				c1++;
			}
			//System.out.println("c1 : "+c1);
		}
		bench.stopSubject(0);
		
		bench.showResults();
	}
	
	public void testAncestorAxis(TreeStructure treeStructure)
	{
		//Prepare
		final int numberContextNodes = 50;
		PreNode[] contextNodes = new PreNode[numberContextNodes];
		Integer[] randomTab = new Integer[numberContextNodes];
		for(int i=0; i<numberContextNodes;i++)
		{
			Integer pre = new Integer((int)(Math.random()*treeStructure.getTreeSize()-1));
			if(!Arrays.asList(randomTab).contains(pre))
				randomTab[i] = pre;
			else
				i--;
		}
		Arrays.sort(randomTab);
		for(int i=0; i<numberContextNodes;i++)
		{
			contextNodes[i] = treeStructure.getNodeAt(randomTab[randomTab.length-i-1]);
			
			contextNodes[i].enabled = true;			
		}
		
		
		//Bench
		Benchmarking bench = new Benchmarking(1, 1);
		bench.startSubject(0);
		for(int i=0;i<30;i++)
		{
			AncestorAxisIterator itr = new AncestorAxisIterator(treeStructure, contextNodes);
			int c1=0;
			for(;itr.hasNext();)
			{
				PreNode p = itr.next();
				p.enabled=true;
				//System.out.println("--> "+p.pre);
				c1++;
			}
			//System.out.println("c1 : "+c1);
		}
		bench.stopSubject(0);
		bench.showResults();
	}
		
	public void testDeleteDescendantAndSelf(TreeStructure treeStructure, TreeViz treeViz)
	{
		Benchmarking bench = new Benchmarking(1, 1);
		bench.startSubject(0);
		for(int i=0;i<2000;i++)
		{
			int p = (int)(Math.random()*treeStructure.getTreeSize());
			//System.out.println(p);
			PreNode node = treeStructure.getNodeAt(p);
			treeStructure.deleteDescendantAndSelf(node);
			
		}
		bench.stopSubject(0);
		bench.showResults();
	}
	
	public void testAddPhysicalEdges(TreeStructure treeStructure)
	{
		RandomEdgesGenerator generator = new RandomEdgesGenerator(treeStructure);
		Benchmarking bench = new Benchmarking(1,20);
		
		for(int i=0;i<20;i++)
		{
			bench.startSubject(0);
			generator.generatPhysicalEdges(100000);
			bench.stopSubject(0);
		}
		
		
		bench.showResults();
	}
	
	public void testAVLRemove()
	{
		SimpleAVLTree tree = new SimpleAVLTree();
		for(int i=0;i<1000;i++)
		{
			tree.add(new PreNode(i,0,0,null));
		}
		
		int removed = 0;
		for(int i=0;i<1000;i++)
		{
			int rd = (int)(Math.random()*1000);
			if(tree.contains(new PreNode(rd, 0,0,null)))
			{
				try
				{
					tree.remove(new PreNode(rd, 0,0,null));
				}
				catch(Exception e)
				{
					System.out.println(rd+" exception at "+removed);
				}
				
				removed++;
			}
		}
		System.out.println(removed+" removed");
	}
	
	public void testPhysicalEdgesIterator(TreeStructure treeStructure)
	{
		RandomEdgesGenerator generator = new RandomEdgesGenerator(treeStructure);
		generator.generatPhysicalEdges(500000);
		
		
		EdgeProcessing edgeProcessing = new EdgeProcessing(treeStructure);
		
		Benchmarking bench = new Benchmarking(2,20);
		
		//Edges Processing
		for(int i=0;i<20;i++)
		{
			bench.startSubject(0);
			edgeProcessing.processInducedEdges(sightZero);
			bench.stopSubject(0);
		}
		
		//Compare
		for(int i=0;i<20;i++)
		{
			bench.startSubject(1);
			SingleTreeIterator itr = new SingleTreeIterator(treeStructure,sightZero);
			for(;itr.hasNext();)
			{
				PreNode node = itr.next();
				if(node.countForwardEdges() > 0)
				{
					for(PreEdge e : node.getForwardEdges())
					{
						if(e.maxNode.size > 0)
						{
							int clusterEnd = e.maxNode.pre+e.maxNode.size;
							for(int j=e.maxNode.pre+1;j<=clusterEnd;j++)
							{
								treeStructure.getNodeAt(j);
							}
						}
					}
				}
			}
			bench.stopSubject(1);
		}
		
		bench.showResults();
	}
	
	public void testAVLIterator()
	{
		//Prepare tree
		BackwardEdgeTree tree = new BackwardEdgeTree();
		for(int i=0;i<100000;i++)
		{
			PreNode node = new PreNode(i,0,0,null);
			PreNode node2 = new PreNode(i+1,0,0,null);
			PreEdge item = new PreEdge(EdgeType.IN, node, node2);
			tree.add(item);
		}
		
		//Prepare list
		LinkedList<PreEdge> list = new LinkedList<PreEdge>();
		for(int i=0;i<100000;i++)
		{
			PreNode node = new PreNode(i,0,0,null);
			PreNode node2 = new PreNode(i+1,0,0,null);
			PreEdge item = new PreEdge(EdgeType.IN, node, node2);
			list.add(item);
		}
		
		Benchmarking bench = new Benchmarking(2, 50);
		
		//Tree
		for(int i=0;i<50;i++)
		{
			bench.startSubject(0);
			for(PreEdge p : tree)
			{
				
			}
			bench.stopSubject(0);
		}
		
		//List
		for(int i=0;i<50;i++)
		{
			bench.startSubject(1);
			for(PreEdge p : list)
			{
				
			}
			bench.stopSubject(1);
		}
		
		bench.showResults();
	}
	
	public void testVirtualEdgesIterator(TreeStructure treeStructure)
	{
		RandomEdgesGenerator generator = new RandomEdgesGenerator(treeStructure);
		generator.generatPhysicalEdges(1000000);
		EdgeProcessing edgeProcessing = new EdgeProcessing(treeStructure);
		
		Benchmarking bench2 = new Benchmarking(1, 1);
		bench2.startSubject(0);
		edgeProcessing.processInducedEdges(sightZero);
		bench2.stopSubject(0);
		bench2.showResults();
		
		//Prepare comparaison
		LinkedList<PreNode> list = new LinkedList<PreNode>();
		SingleTreeIterator itr = new SingleTreeIterator(treeStructure,sightZero);
		while(itr.hasNext())
		{
			PreNode node = itr.next();
			for(int i=0;i<node.getVirtualEdgesOUT(sightZero).getCount();i++)
			{
				list.add(node);
			}
		}
		
		Benchmarking bench = new Benchmarking(2, 30);

		//Tree
		for(int i=0;i<30;i++)
		{
			bench.startSubject(0);
			EdgesOutIterator itr2 = new EdgesOutIterator(treeStructure, sightZero);
			while(itr2.hasNext())
			{
				VirtualEdge e = itr2.next();
			}
			bench.stopSubject(0);
		}

		//List
		for(int i=0;i<30;i++)
		{
			bench.startSubject(1);
			for(PreNode p : list)
			{
				
			}
			bench.stopSubject(1);
		}
		bench.showResults();
		
	}
	
	public void testComplete(final TreeStructure treeStructure,final TreeViz treeviz, boolean edgeProcess)
	{
		if(edgeProcess)
		{
			RandomEdgesGenerator generator = new RandomEdgesGenerator(treeStructure);
			generator.generatPhysicalEdges(20);
			EdgeProcessing edgeProcessing = new EdgeProcessing(treeStructure);
			edgeProcessing.processInducedEdges(sightZero);
		}
		
		treeviz.showTree(treeStructure);
		
		treeviz.getControlPanel().setAction(ActionType.EXPAND, new AbstractAction("Expand") {
		
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int number = treeviz.getControlPanel().getExpandContractNumber();
				 dyts.expand(treeStructure.getNodeAt(number),sightZero);
				 treeviz.showTree(treeStructure);
			}
		});
		
		treeviz.getControlPanel().setAction(ActionType.RETRACT, new AbstractAction("Retract") {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int number = treeviz.getControlPanel().getExpandContractNumber();
				 dyts.retract(treeStructure.getNodeAt(number),sightZero);
				 treeviz.showTree(treeStructure);
			}
		});
		
		treeviz.getControlPanel().setAction(ActionType.ADDNODE, new AbstractAction("Add node as a child") {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int number = treeviz.getControlPanel().getAddDeleteNodeNumber();
				PreNode parent = treeStructure.getNodeAt(number);
				 dyts.addNode(new PreNode(0,0,0, parent));
				 treeviz.showTree(treeStructure);
			}
		});
		
		treeviz.getControlPanel().setAction(ActionType.DELNODE, new AbstractAction("Delete node") {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int number = treeviz.getControlPanel().getAddDeleteNodeNumber();
				 dyts.deleteNode(treeStructure.getNodeAt(number));
				 treeviz.showTree(treeStructure);
			}
		});
		
		treeviz.getControlPanel().setAction(ActionType.DELNODES, new AbstractAction("Delete") {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int[] numbers = treeviz.getControlPanel().getMultipleDeleteNodeNumber();
				PreNode[] tab = new PreNode[numbers.length];
				for(int i=0;i<numbers.length;i++)
				{
					int pre = numbers[i];
					tab[i] = treeStructure.getNodeAt(pre);
				}
				 //dyts.deleteNodes(tab);
				 treeviz.showTree(treeStructure);
			}
		});
		
		treeviz.getControlPanel().setAction(ActionType.GETEDGES, new AbstractAction("Get Edges") {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int pre = treeviz.getControlPanel().getDelEdgeNumber();
				PreNode node = treeStructure.getNodeAt(pre);
				if(node.size==0)
				{
					treeviz.getControlPanel().setSelectedNode(node);
					PreEdge[] edges = new PreEdge[node.getForwardEdges().getCount()];
					int i=0;
					for(PreEdge p : node.getForwardEdges())
					{
						edges[i] = p;
						i++;
					}
					treeviz.getControlPanel().setEdgesTab(edges);
				}
			}
		});
		
		treeviz.getControlPanel().setAction(ActionType.DELEDGE, new AbstractAction("Delete Edge") {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				PreEdge selectedEdge = treeviz.getControlPanel().getSelectedEdge();
				dyts.delEdge(selectedEdge);
				treeviz.showTree(treeStructure);
				treeStructure.showTreeAsTable();
			}
		});
	}
	
	public void testReprocessInducesEdges(final TreeStructure treeStructure,final TreeViz treeviz)
	{
		
		
		/*int number=-1;
		while(number!=0)
		{
			System.out.print("expand:");
			Scanner in = new Scanner(System.in);
		    number  = Integer.parseInt(in.nextLine());
		   
		    
		    System.out.print("retract:");
		    Scanner in2 = new Scanner(System.in);
		    number  = Integer.parseInt(in2.nextLine());
		    dyts.retract(treeStructure.getNodeAt(number));
		    treeviz.showTree(treeStructure);
		    //treeStructure.showTreeAsTable();
		    //in.close();  
		}*/

		
	}
	
	public void testDeleteNode(final TreeStructure treeStructure)
	{
		RandomEdgesGenerator generator = new RandomEdgesGenerator(treeStructure);
		generator.generatPhysicalEdges(100000);
		EdgeProcessing edgeProcessing = new EdgeProcessing(treeStructure);
		edgeProcessing.processInducedEdges(sightZero);
		
		Benchmarking bench = new Benchmarking(1,20);
		
		int treeSize = treeStructure.getTreeSize();
		
		//Delete nodes
		for(int i=0;i<20;i++)
		{
			bench.startSubject(0);
			for(int j=0;j<4000;j++)
			{
				int pre = (int)(Math.random()*(treeSize-1)+1);
				PreNode node = treeStructure.getNodeAt(pre);
				if(node!=null)
				{
					dyts.deleteNode(node);
					treeSize-=node.size+1;
				}
			}
			bench.stopSubject(0);	
		}
		
		//Reprocess	
		/*for(int i=0;i<25;i++)
		{
			bench.startSubject(1);
			SingleViewTreeIterator treeIterator = new SingleViewTreeIterator(treeStructure);
			for(;treeIterator.hasNext();)
			{
				PreNode n = treeIterator.next();
				n.getVirtualEdgesIN().clear();
				n.getVirtualEdgesOUT().clear();
			}
			edgeProcessing.processInducedEdges();
			bench.stopSubject(1);
		}*/
		
		bench.showResults();
		
		System.out.println("Tree size : "+treeStructure.getTreeSize());
	}
	
	public void testDeleteNodes(final TreeStructure treeStructure, TreeViz treeViz)
	{
		RandomEdgesGenerator generator = new RandomEdgesGenerator(treeStructure);
		generator.generatPhysicalEdges(20);
		EdgeProcessing edgeProcessing = new EdgeProcessing(treeStructure);
		edgeProcessing.processInducedEdges(sightZero);
		
		PreNode[] tab = new PreNode[10];
		for(int i=0;i<10;i++)
		{
			int pre = (int)(Math.random()*(treeStructure.getTreeSize()-1)+1);
			tab[i] = treeStructure.getNodeAt(pre);
		}
		
		//dyts.deleteNodes(tab);
		treeStructure.showTreeAsTable();
		treeViz.showTree(treeStructure);
		
	}
	
	public void testDeleteEdges(TreeStructure treeStructure)
	{
		RandomEdgesGenerator generator = new RandomEdgesGenerator(treeStructure);
		List<PreEdge> edgeList = generator.generatPhysicalEdges(300000);
		EdgeProcessing edgeProcessing = new EdgeProcessing(treeStructure);
		edgeProcessing.processInducedEdges(sightZero);
		
		int edgeCount = edgeList.size();
		
		Benchmarking bench = new Benchmarking(1,10);
		for(int j=0;j<10;j++)
		{
			bench.startSubject(0);
			for(int i=0;i<500;i++)
			{
				int edgeN = (int)(Math.random()*edgeCount);
				PreEdge toDelete = edgeList.get(edgeN);
				
				dyts.delEdge(toDelete);
				
				edgeCount--;
			}
			bench.stopSubject(0);
		}
		
		
		bench.showResults();
	}
	
	public void testAddEdges(TreeStructure treeStructure)
	{
		RandomEdgesGenerator generator = new RandomEdgesGenerator(treeStructure);
		List<PreEdge> edgeList = generator.generatPhysicalEdges(100000);

		//Reset edges
		for(PreNode node : treeStructure.getTree())
		{
			node.getBackwardEdges().clear();
			node.getForwardEdges().clear();
		}
		
		Benchmarking bench = new Benchmarking(1,10);
		for(int j=0;j<10;j++)
		{
			bench.startSubject(0);
			for(int i=0;i<10000;i++)
			{
				PreEdge toAdd = edgeList.get(i*(j+1));
				
				dyts.addEdge(toAdd);
				
			}
			bench.stopSubject(0);
		}
		
		
		bench.showResults();
	}
	
	public void testBuffer(TreeStructure treeStructure)
	{
		Benchmarking bench1 = new Benchmarking(1, 10);
		
		for(int j=0;j<10;j++)
		{
			bench1.startSubject(0);
			for(int i=0;i<100000;i++)
			{
				treeStructure.getNodeAt(i);
			}
			bench1.stopSubject(0);
		}
		bench1.showResults();
		
	}
	
	public void testScenario1(TreeStructure treeStructure)
	{
		//Add nodes
		
		Benchmarking bench = new Benchmarking(30,30);
		
		int numAdd=50000;
		for(int i=0;i<30;i++)
		{
			PreNode[] randomNodes = new PreNode[numAdd];
			
			for(int j=0;j<numAdd;j++)
			{
				int preRandom = (int)(Math.random()*treeStructure.getTreeSize());
				PreNode parent = treeStructure.getNodeAt(preRandom);
				PreNode newNode = new PreNode(0,0,0,parent);
				randomNodes[j] = newNode;
			}
			
			bench.startSubject(i,treeStructure.getTreeSize());
			for(PreNode newNode : randomNodes)
			{
				newNode.pre = newNode.parent.getPre();
				dyts.addNode(newNode);
			}
			bench.stopSubject(i, 0);
			
			randomNodes=null;
			System.gc();
			System.out.println(treeStructure.getTreeSize());
			//numAdd+=100;
		}
		
		bench.showTable();
	}
	
	public void testScenario2(TreeStructure treeStructure)
	{
		//Delete nodes
		
		Benchmarking bench = new Benchmarking(100,100);
		
		int numDel=50000;
		int treeSize = treeStructure.getTreeSize();
		for(int i=0;i<100;i++)
		{
			PreNode[] randomNodes = new PreNode[numDel];
			Set<Integer> set = new TreeSet<Integer>();
			int j=0;
			while(j<numDel)
			{
				int preRandom = (int)(Math.random()*treeSize-1);
				PreNode node = treeStructure.getNodeAt(preRandom);
				if(node.size==0 && !set.contains(preRandom))
				{
					randomNodes[j] = node;
					set.add(preRandom);
					treeSize--;
					j++;
				}
			}
			

			bench.startSubject(i,treeStructure.getTreeSize());
			for(PreNode toDelete : randomNodes)
			{
				toDelete.getPre();
				dyts.deleteNode(toDelete);
			}
			bench.stopSubject(i, 0);
			
			randomNodes=null;
			System.gc();
			System.out.println(treeStructure.getTreeSize());
			//numAdd+=100;
		}
		
		bench.showTable();
	}
	
	
	public void testScenario3(TreeStructure treeStructure)
	{
		//Add & Delete nodes
		
		Benchmarking bench = new Benchmarking(500,500);
		
		int numAdd=10000;
		int numDel=10000;
		
		for(int i=0;i<500;i++)
		{
			Set<Integer> set = new TreeSet<Integer>();
			
			//Random Add
			PreNode[] randomNodes = new PreNode[numAdd];
			for(int j=0;j<numAdd;j++)
			{
				int preRandom = (int)(Math.random()*treeStructure.getTreeSize());
				PreNode parent = treeStructure.getNodeAt(preRandom);
				PreNode newNode = new PreNode(0,0,0,parent);
				newNode.enabled=true;
				set.add(preRandom);
				randomNodes[j] = newNode;
			}
			
			int treeSize = treeStructure.getTreeSize();
			//Random delete
			PreNode[] randomNodes2 = new PreNode[numDel];
			int j=0;
			while(j<numDel)
			{
				int preRandom = (int)(Math.random()*treeSize-1);
				PreNode node = treeStructure.getNodeAt(preRandom);
				if(node.size==0 && !set.contains(preRandom))
				{
					randomNodes2[j] = node;
					set.add(preRandom);
					treeSize--;
					j++;
				}
			}

			bench.startSubject(i,treeStructure.getTreeSize());
			
			//Delete
			for(PreNode toDelete : randomNodes2)
			{
				toDelete.getPre();
				dyts.deleteNode(toDelete);
			}
			
			//Add
			for(PreNode newNode : randomNodes)
			{
				newNode.pre = newNode.parent.getPre();
				dyts.addNode(newNode);
			}
			
			bench.stopSubject(i, 0);
			
			randomNodes=null;
			System.gc();
			//System.out.println(treeStructure.getTreeSize());
			//numAdd+=100;
		}
		
		bench.showTable();
	}
	
	public void testScenario4(TreeStructure treeStructure)
	{
		//Add & Delete nodes - small amounts each test - alternace high
		
		Benchmarking bench = new Benchmarking(500,500);
		
		int numAdd=10000;
		int numDel=10000;
		
		for(int i=0;i<500;i++)
		{
			Set<Integer> set = new TreeSet<Integer>();
			
			//Random Add
			PreNode[] randomNodes = new PreNode[numAdd];
			for(int j=0;j<numAdd;j++)
			{
				int preRandom = (int)(Math.random()*treeStructure.getTreeSize());
				PreNode parent = treeStructure.getNodeAt(preRandom);
				PreNode newNode = new PreNode(0,0,0,parent);
				newNode.enabled=true;
				set.add(preRandom);
				randomNodes[j] = newNode;
			}
			
			int treeSize = treeStructure.getTreeSize();
			//Random delete
			PreNode[] randomNodes2 = new PreNode[numDel];
			int j=0;
			while(j<numDel)
			{
				int preRandom = (int)(Math.random()*treeSize-1);
				PreNode node = treeStructure.getNodeAt(preRandom);
				if(node.size==0 && !set.contains(preRandom))
				{
					randomNodes2[j] = node;
					set.add(preRandom);
					treeSize--;
					j++;
				}
			}

			bench.startSubject(i,treeStructure.getTreeSize());
			
			int iDelete=0;
			int iAdd=0;
			
			for(;iDelete<randomNodes2.length && iAdd<randomNodes.length;)
			{
				//Delete
				for(int k=0;k<10;k++)
				{
					PreNode toDelete = randomNodes2[iDelete];
					toDelete.getPre();
					dyts.deleteNode(toDelete);
					iDelete++;
				}
				
				//Add
				for(int k=0;k<10;k++)
				{
					PreNode newNode = randomNodes[iAdd];
					newNode.pre = newNode.parent.getPre();
					dyts.addNode(newNode);
					iAdd++;
				}
			}
			
			bench.stopSubject(i, 0);
			
			randomNodes=null;
			System.gc();
			System.out.println(treeStructure.getTreeSize()+" \t"+treeStructure.treeHeight);
			//numAdd+=100;
		}
		
		bench.showTable();
	}

	
	public void testScenario5()
	{
		Benchmarking bench = new Benchmarking(100,100);
		
		int numEdges=10000;
		int stepEdge=10000;
		int numAdd=10000;
		
		TreeStructure treeStructure = new TreeStructure();
		CompleteTreeImporter importer = new CompleteTreeImporter(treeStructure,sightManager);
		importGraph(importer, 100, false);
		
		for(int i=0;i<100;i++)
		{
			RandomEdgesGenerator generator = new RandomEdgesGenerator(treeStructure);
			List<PreEdge> edgeList = generator.generatPhysicalEdges(numEdges);

			//Reset edges
			for(PreNode node : treeStructure.getTree())
			{
				node.getBackwardEdges().clear();
				node.getForwardEdges().clear();
				node.getVirtualEdgesIN(sightZero).clear();
				node.getVirtualEdgesOUT(sightZero).clear();
			}
		
			bench.startSubject(i,numEdges);
			for(int j=0;j<numEdges;j++)
			{
				PreEdge toAdd = edgeList.get(j);
				
				dyts.addEdge(toAdd);
					
			}
			bench.stopSubject(i,0);
			
			//Add new nodes
			/*PreNode[] randomNodes = new PreNode[numAdd];
			
			for(int j=0;j<numAdd;j++)
			{
				int preRandom = (int)(Math.random()*treeStructure.getTreeSize());
				PreNode parent = treeStructure.getNodeAt(preRandom);
				PreNode newNode = new PreNode(0,0,0,parent);
				newNode.enabled = true;
				randomNodes[j] = newNode;
			}
			
			for(PreNode newNode : randomNodes)
			{
				newNode.pre = newNode.parent.getPre();
				dyts.addNode(newNode);
			}*/
			//End
			
			numEdges+=stepEdge;
			System.out.println(treeStructure.getTreeSize());
		}
		
		bench.showTable();
	}

	
	public void testScenario6()
	{
		Benchmarking benchmarking = new Benchmarking(30,30);
		
		int numSibling=10;
		int siblingStep=4;
		
		for(int i=0;i<30;i++)
		{
			//Create TreeStructure
			TreeStructure treeStructure = new TreeStructure();
			CompleteTreeImporter importer = new CompleteTreeImporter(treeStructure,sightManager);
			importGraph(importer, numSibling, false);
			shuffleEnable(treeStructure);
			
			//Generate edges
			RandomEdgesGenerator generator = new RandomEdgesGenerator(treeStructure);
			List<PreEdge> edgeList = generator.generatPhysicalEdges(100);
			EdgeProcessing edgeProcessing = new EdgeProcessing(treeStructure);
			
			//Process
			benchmarking.startSubject(i, treeStructure.getTreeSize());
			edgeProcessing.processInducedEdges(sightZero);
			benchmarking.stopSubject(i, 0);
			
			numSibling+=siblingStep;
			System.out.println("TreeSize : "+treeStructure.getTreeSize());
		}
		
		benchmarking.showTable();
	}

}
