package gephi.data.network.edge;

import java.util.List;

import gephi.data.network.TreeStructure;
import gephi.data.network.avl.param.ParamAVLIterator;
import gephi.data.network.avl.typed.PreNodeAVLTree;
import gephi.data.network.edge.PreEdge.EdgeType;
import gephi.data.network.node.PreNode;
import gephi.data.network.node.treelist.SingleViewTreeIterator;

public class EdgeProcessing {

	private TreeStructure treeStructure;
	private ParamAVLIterator<PreEdge> preEdgeIterator;
	
	public EdgeProcessing(TreeStructure treeStructure)
	{
		this.treeStructure = treeStructure;
		preEdgeIterator = new ParamAVLIterator<PreEdge>();
	}
	
	public void clearVirtualEdges()
	{
		SingleViewTreeIterator enabledNodes = new SingleViewTreeIterator(treeStructure);
		while(enabledNodes.hasNext())
		{
			PreNode currentNode = enabledNodes.next();
			currentNode.getVirtualEdgesIN().clear();
			currentNode.getVirtualEdgesOUT().clear();
		}
	}
	
	public void processInducedEdges()
	{
		
		SingleViewTreeIterator enabledNodes = new SingleViewTreeIterator(treeStructure);
		while(enabledNodes.hasNext())
		{
			PreNode currentNode = enabledNodes.next();
			processInducedEdges(currentNode);
		}
		
	}
	
	public void processInducedEdges(PreNode currentNode)
	{
		if(currentNode.isLeaf())
		{
			if(currentNode.countForwardEdges()>0)
			{
				//Leaf
				preEdgeIterator.setNode(currentNode.getForwardEdges());
				while(preEdgeIterator.hasNext())
				{
					PreEdge edge = preEdgeIterator.next();
					PreNode edgeNode = edge.maxNode;
					
					if(edgeNode.pre > currentNode.pre && edgeNode.space==currentNode.space)
					{
						if(edgeNode.enabled)
						{
							//Link between two leafs
							//System.out.println("Lien entre 2 feuilles. "+currentNode.pre+" "+edge.edgeType+" "+edgeNode.pre);
							createEdge(edge, currentNode, edgeNode);
						}
						else
						{
							PreNode clusterAncestor = treeStructure.getEnabledAncestorOrSelf(edgeNode);
							if(clusterAncestor!=null && !checkDouble(clusterAncestor, currentNode.pre, edge))
							{
								//The linked node is a cluster and has never been visited from this leaf
								
								//Link between a leaf and a cluster
								//System.out.println("Lien entre une feuille et un cluster. "+currentNode.pre+" "+edge.edgeType+" "+clusterAncestor.pre);
								VirtualEdge newEdge = createEdge(edge, currentNode, clusterAncestor);
								
								//Set the trace
								clusterAncestor.preTrace = currentNode.pre;
								clusterAncestor.lastEdge = newEdge;
							}
						}
					}
				}
			}
		}
		else
		{
			//Cluster
			int clusterEnd = currentNode.pre+currentNode.size;
			for(int i=currentNode.pre+1;i<=clusterEnd;i++)
			{
				PreNode desc = treeStructure.getNodeAt(i);
				if(desc.isLeaf() && desc.countForwardEdges() > 0)
				{
					preEdgeIterator.setNode(desc.getForwardEdges());
					while(preEdgeIterator.hasNext())
					{
						PreEdge edge = preEdgeIterator.next();
						PreNode edgeNode = edge.maxNode;
						
						if(edgeNode.pre > clusterEnd && edgeNode.space== currentNode.space && !checkDouble(edgeNode,currentNode.pre, edge))
						{
							if(edgeNode.enabled)
							{
								//Link between two leafs
								//System.out.println("Lien entre 1 cluster et une feuille "+currentNode.pre+" "+edge.edgeType+" "+edgeNode.pre);
								VirtualEdge newEdge = createEdge(edge, currentNode, edgeNode);
								
								//Set the trace
								edgeNode.preTrace = currentNode.pre;
								edgeNode.lastEdge = newEdge;
							}
							else
							{
								PreNode clusterAncestor = treeStructure.getEnabledAncestorOrSelf(edgeNode);
								if(clusterAncestor!=null && !checkDouble(clusterAncestor, currentNode.pre, edge))
								{
									//The linked node is a cluster and has never been visited from this leaf
									
									//Link between a leaf and a cluster
									//System.out.println("Lien entre un cluster et un cluster. "+currentNode.pre+" "+edge.edgeType+" "+clusterAncestor.pre);
									VirtualEdge newEdge = createEdge(edge, currentNode, clusterAncestor);
									
									//Set the trace
									edgeNode.preTrace = currentNode.pre;
									clusterAncestor.preTrace = currentNode.pre;
									edgeNode.lastEdge = newEdge;
									clusterAncestor.lastEdge = newEdge;
								}
							}
						}
					}
				}
				
				desc.reinitTrace();
			}
		}
		
		//Reinit trace
		currentNode.reinitTrace();
	}
	
	public void processLocalInducedEdges(PreNode currentNode)
	{
		if(currentNode.isLeaf())
		{
			if(currentNode.countForwardEdges()>0)
			{
				//Leaf
				preEdgeIterator.setNode(currentNode.getForwardEdges());
				while(preEdgeIterator.hasNext())
				{
					PreEdge edge = preEdgeIterator.next();
					PreNode edgeNode = edge.maxNode;
					
					if(edgeNode.pre > currentNode.pre && edgeNode.space==currentNode.space)
					{
						if(edgeNode.enabled)
						{
							//Link between two leafs
							//System.out.println("Lien entre 2 feuilles. "+currentNode.pre+" "+edge.edgeType+" "+edgeNode.pre);
							createEdge(edge, currentNode, edgeNode);
						}
						else
						{
							PreNode clusterAncestor = treeStructure.getEnabledAncestorOrSelf(edgeNode);
							if(clusterAncestor!=null && !checkDouble(clusterAncestor, currentNode.pre, edge))
							{
								//The linked node is a cluster and has never been visited from this leaf
								
								//Link between a leaf and a cluster
								//System.out.println("Lien entre une feuille et un cluster. "+currentNode.pre+" "+edge.edgeType+" "+clusterAncestor.pre);
								VirtualEdge newEdge = createEdge(edge, currentNode, clusterAncestor);
								
								//Set the trace
								clusterAncestor.preTrace = currentNode.pre;
								clusterAncestor.lastEdge = newEdge;
							}
						}
					}
				}
			}
		}
		else
		{
			//Cluster
			int clusterEnd = currentNode.pre+currentNode.size;
			for(int i=currentNode.pre+1;i<=clusterEnd;i++)
			{
				PreNode desc = treeStructure.getNodeAt(i);
				if(desc.isLeaf() && desc.countForwardEdges() > 0)
				{
					preEdgeIterator.setNode(desc.getForwardEdges());
					while(preEdgeIterator.hasNext())
					{
						PreEdge edge = preEdgeIterator.next();
						PreNode edgeNode = edge.maxNode;
						
						if(edgeNode.pre > clusterEnd && edgeNode.space== currentNode.space && !checkDouble(edgeNode,currentNode.pre, edge))
						{
							if(edgeNode.enabled)
							{
								//Link between two leafs
								//System.out.println("Lien entre 1 cluster et une feuille"+currentNode.pre+" "+edge.edgeType+" "+edgeNode.pre);
								VirtualEdge newEdge = createEdge(edge, currentNode, edgeNode);
								
								//Set the trace
								edgeNode.preTrace = currentNode.pre;
								edgeNode.lastEdge = newEdge;
							}
							else
							{
								PreNode clusterAncestor = treeStructure.getEnabledAncestorOrSelf(edgeNode);
								if(clusterAncestor!=null && !checkDouble(clusterAncestor, currentNode.pre, edge))
								{
									//The linked node is a cluster and has never been visited from this leaf
									
									//Link between a leaf and a cluster
									//System.out.println("Lien entre un cluster et un cluster. "+currentNode.pre+" "+edge.edgeType+" "+clusterAncestor.pre);
									VirtualEdge newEdge = createEdge(edge, currentNode, clusterAncestor);
									
									//Set the trace
									//edgeNode.preTrace = currentNode.pre;
									clusterAncestor.preTrace = currentNode.pre;
									clusterAncestor.lastEdge = newEdge;
								}
							}
						}
					}
				}
			}
		}
		
		//Reinit trace
		currentNode.reinitTrace();
	}
	
	public void reprocessInducedEdges(Iterable<PreNode> enabledNodes, PreNode center)
	{
		int centerLimit = center.pre+center.size;
		ParamAVLIterator<PreEdge> preEdgeIterator = new ParamAVLIterator<PreEdge>();
		for(PreNode currentNode : enabledNodes)
		{
			//System.out.println("reprocess "+currentNode.pre);
			if(currentNode.isLeaf())
			{
				if(currentNode.countForwardEdges()>0)
				{
					preEdgeIterator.setNode(currentNode.getForwardEdges());
					while(preEdgeIterator.hasNext())
					{
						PreEdge edge = preEdgeIterator.next();
						PreNode edgeNode = edge.maxNode;
						
						if(edgeNode.pre > center.pre && edgeNode.pre <= centerLimit && edgeNode.space==currentNode.space)
						{
							if(edgeNode.enabled)
							{
								//Link between two leafs
								//System.out.println("Lien entre 2 feuilles. "+currentNode.pre+" "+edge.edgeType+" "+edgeNode.pre);
								createEdge(edge, currentNode, edgeNode);
							}
							else
							{
								PreNode clusterAncestor = treeStructure.getEnabledAncestorOrSelf(edgeNode);
								if(clusterAncestor!=null && !checkDouble(clusterAncestor, currentNode.pre, edge))
								{
									//The linked node is a cluster and has never been visited from this leaf
									
									//Link between a leaf and a cluster
									//System.out.println("Lien entre une feuille et un cluster. "+currentNode.pre+" "+edge.edgeType+" "+clusterAncestor.pre);
									VirtualEdge newEdge = createEdge(edge, currentNode, clusterAncestor);
									
									//Set the trace
									clusterAncestor.preTrace = currentNode.pre;
									clusterAncestor.lastEdge = newEdge;
								}
							}
						}
					}
				}
			}
			else
			{
				//Cluster
				int clusterEnd = currentNode.pre+currentNode.size;
				for(int i=currentNode.pre+1;i<=clusterEnd;i++)
				{
					PreNode desc = treeStructure.getNodeAt(i);
					if(desc.isLeaf() && desc.countForwardEdges() > 0)
					{
						preEdgeIterator.setNode(desc.getForwardEdges());
						while(preEdgeIterator.hasNext())
						{
							PreEdge edge = preEdgeIterator.next();
							PreNode edgeNode = edge.maxNode;
							if(edgeNode.pre > center.pre && edgeNode.pre <= centerLimit && edgeNode.space==currentNode.space && !checkDouble(edgeNode, currentNode.pre, edge))
							{
								if(edgeNode.enabled)
								{
									//Link between two leafs
									//System.out.println("Lien entre 1 cluster et une feuille"+currentNode.pre+" "+edge.edgeType+" "+edgeNode.pre);
									VirtualEdge newEdge = createEdge(edge, currentNode, edgeNode);
									
									//Set the trace
									edgeNode.preTrace = currentNode.pre;
									edgeNode.lastEdge = newEdge;
								}
								else
								{
									PreNode clusterAncestor = treeStructure.getEnabledAncestorOrSelf(edgeNode);
									if(clusterAncestor!=null && !checkDouble(clusterAncestor, currentNode.pre, edge))
									{
										//The linked node is a cluster and has never been visited from this leaf
										
										//Link between a leaf and a cluster
										//System.out.println("Lien entre un cluster et un cluster. "+currentNode.pre+" "+edge.edgeType+" "+clusterAncestor.pre);
										VirtualEdge newEdge = createEdge(edge, currentNode, clusterAncestor);
										
										//Set the trace
										clusterAncestor.preTrace = currentNode.pre;
										clusterAncestor.lastEdge = newEdge;
									}
								}
							}
						}
					}
				}
			}
			//Reinit trace
			currentNode.reinitTrace();
		}
	}
	
	private boolean checkDouble(PreNode edgeNode, int pre, PreEdge edge)
	{
		if(edgeNode.preTrace==pre)
		{
			if(edgeNode.preTraceType > 0 && edgeNode.preTraceType!=edge.edgeType.id)
			{
				edgeNode.preTraceType = -1;
				return false;
			}
			//System.out.println(edgeNode.lastEdge.getPreNodeFrom().pre+" -> "+edgeNode.lastEdge.getPreNodeTo().pre+"    |    cardinal++");
			//edgeNode.lastEdge.incCardinal(edge.cardinal);
			edgeNode.lastEdge.addPhysicalEdge(edge);
			return true;
		}
		edgeNode.preTraceType = edge.edgeType.id;
		return false;
	}
	
	
	
	private VirtualEdge createEdge(PreEdge edge, PreNode currentNode, PreNode edgeNode)
	{
		VirtualEdge newEdge = null;
		
		if(edge.edgeType == EdgeType.IN)
		{
			newEdge = new VirtualEdge(edgeNode, currentNode);
			newEdge.addPhysicalEdge(edge);
			edgeNode.getVirtualEdgesOUT().add(newEdge);
			currentNode.getVirtualEdgesIN().add(newEdge);
		}
		else if(edge.edgeType == EdgeType.OUT)
		{
			newEdge = new VirtualEdge(currentNode, edgeNode);
			newEdge.addPhysicalEdge(edge);
			edgeNode.getVirtualEdgesIN().add(newEdge);
			currentNode.getVirtualEdgesOUT().add(newEdge);
		}
		return newEdge;
	}
	
	public void appendEdgeHostingNeighbours(PreNode node, PreNodeAVLTree physicalNeighbours, int preLimit)
	{
		if(node.getVirtualEdgesIN().getCount()> 0)
		{
			for(DytsEdge e : node.getVirtualEdgesIN())
			{
				PreNode neighbour = e.getPreNodeFrom();
				if(neighbour.pre < preLimit)
					physicalNeighbours.add(neighbour);
			}
		}
		
		if(node.getVirtualEdgesOUT().getCount()> 0)
		{
			for(DytsEdge e : node.getVirtualEdgesOUT())
			{
				PreNode neighbour = e.getPreNodeTo();
				if(neighbour.pre < preLimit)
					physicalNeighbours.add(neighbour);
			}
		}
	}
	
	public void clearVirtualEdges(PreNode node)
	{
		if(node.getVirtualEdgesIN().getCount()> 0)
		{
			for(DytsEdge n : node.getVirtualEdgesIN())
			{
				n.getPreNodeFrom().getVirtualEdgesOUT().remove(n);
				n.getPreNodeFrom().reinitTrace();
			}
			
			node.getVirtualEdgesIN().clear();
		}
		
		if(node.getVirtualEdgesOUT().getCount()> 0)
		{
			for(DytsEdge n : node.getVirtualEdgesOUT())
			{
				n.getPreNodeTo().getVirtualEdgesIN().remove(n);
				n.getPreNodeTo().reinitTrace();
			}
			
			node.getVirtualEdgesOUT().clear();
		}
	}
	
	public void clearPhysicalEdges(PreNode node)
	{
		if(node.getBackwardEdges().getCount() >0)
		{
			for(PreEdge mirroredEdge : node.getBackwardEdges())
			{
				mirroredEdge.minNode.getForwardEdges().remove(mirroredEdge);
			}
			
			node.getBackwardEdges().clear();
		}
		
		if(node.getForwardEdges().getCount() >0)
		{
			for(PreEdge physicalEdge : node.getForwardEdges())
			{
				physicalEdge.maxNode.getBackwardEdges().remove(physicalEdge);
			}
			
			node.getForwardEdges().clear();
		}
	}
	
	public VirtualEdge createVirtualEdge(PreEdge physicalEdge, PreNode minParent, PreNode maxParent)
	{
		VirtualEdge virtualEdge=null;
		if(physicalEdge.edgeType == EdgeType.IN)
		{
			virtualEdge = new VirtualEdge(maxParent, minParent);
			virtualEdge.addPhysicalEdge(physicalEdge);
			maxParent.getVirtualEdgesOUT().add(virtualEdge);
			minParent.getVirtualEdgesIN().add(virtualEdge);
		}
		else
		{
			virtualEdge = new VirtualEdge(minParent, maxParent);
			virtualEdge.addPhysicalEdge(physicalEdge);
			maxParent.getVirtualEdgesIN().add(virtualEdge);
			minParent.getVirtualEdgesOUT().add(virtualEdge);
		}
		return virtualEdge;
	}
}
