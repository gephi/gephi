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
package gephi.data.network;

import java.util.ArrayList;
import java.util.List;

import gephi.data.network.avl.DytsEdgeTree;
import gephi.data.network.avl.param.ParamAVLIterator;
import gephi.data.network.avl.param.ParamAVLTree;
import gephi.data.network.avl.typed.PreNodeAVLTree;
import gephi.data.network.config.DDNSConfig;
import gephi.data.network.edge.DytsEdge;
import gephi.data.network.edge.EdgeProcessing;
import gephi.data.network.edge.PreEdge;
import gephi.data.network.edge.VirtualEdge;
import gephi.data.network.edge.PreEdge.EdgeType;
import gephi.data.network.node.PreNode;
import gephi.data.network.sight.Sight;
import gephi.data.network.tree.iterators.DescendantAxisIterator;

public class Dyts {

	private DDNSConfig config;
	private TreeStructure treeStructure;
	private EdgeProcessing edgeProcessing;
	
	public Dyts()
	{
		config = new DDNSConfig();
		treeStructure = new TreeStructure();
		edgeProcessing = new EdgeProcessing(treeStructure);
	}
	
	public void expand(PreNode node, Sight sight)
	{
		PreNodeAVLTree nodeToReprocess = new PreNodeAVLTree();
		
		//Enable children
		PreNode child = null;
		for(int i=node.pre+1;i<=node.pre+node.size;)
		{
			child = treeStructure.getNodeAt(i);
			child.enabled = true;
			
			i+=child.size+1;
		}
		
		//Reprocess edge-hosting neighbour
		edgeProcessing.appendEdgeHostingNeighbours(node, nodeToReprocess, node.pre, sight);
		edgeProcessing.reprocessInducedEdges(nodeToReprocess, node, sight);
		
		//Process induced edges of direct children
		for(int i=node.pre+1;i<=node.pre+node.size;)
		{
			child = treeStructure.getNodeAt(i);
			edgeProcessing.processLocalInducedEdges(child, sight);
			i+=child.size+1;
		}
		
		//Clean current node
		node.enabled = false;
		edgeProcessing.clearVirtualEdges(node, sight);
	}
	
	public void retract(PreNode parent, Sight sight)
	{
		PreNodeAVLTree nodeToReprocess = new PreNodeAVLTree();
		
		//Enable node
		parent.enabled = true;
		
		//Disable children
		PreNode child = null;
		for(int i=parent.pre+1;i<=parent.pre+parent.size;)
		{
			child = treeStructure.getNodeAt(i);
			child.enabled = false;
			edgeProcessing.appendEdgeHostingNeighbours(child, nodeToReprocess, parent.pre, sight);
			
			i+=child.size+1;
		}
		
		edgeProcessing.reprocessInducedEdges(nodeToReprocess, parent,sight);
		edgeProcessing.processLocalInducedEdges(parent, sight);
		
		for(int i=parent.pre+1;i<=parent.pre+parent.size;)
		{
			child = treeStructure.getNodeAt(i);
			edgeProcessing.clearVirtualEdges(child, sight);
			i+=child.size+1;
		}
	}
	
	
	
	public void deleteNode(PreNode node)
	{
		PreNode enabledAncestor = treeStructure.getEnabledAncestor(node);
		ParamAVLIterator<PreEdge> iterator=null;
		
		int nodeSize = node.getPre()+node.size;
		for(int i=node.pre;i<=nodeSize;i++)		//Children & Self
		{
			PreNode child = treeStructure.getNodeAt(i);
			if(enabledAncestor==null)
			{
				//if(child.enabled)
				//	edgeProcessing.clearVirtualEdges(child, sight);		//Clear virtual edges
				//edgeProcessing.clearPhysicalEdges(child);
			}
			else
			{
				boolean hasBackwardEdges = child.countBackwardEdges() > 0;
				boolean hasForwardEdges = child.countForwardEdges() > 0;
				
				if(iterator==null && (hasForwardEdges || hasBackwardEdges))
					iterator = new ParamAVLIterator<PreEdge>();
					
				//Delete Backward edges
				if(hasBackwardEdges)
				{
					iterator.setNode(child.getBackwardEdges());
					for(;iterator.hasNext();)
					{
						PreEdge edge = iterator.next();
						delEdge(edge);
					}
				}
					
				//Delete Forward edges
				if(hasForwardEdges)
				{
					iterator.setNode(child.getForwardEdges());
					for(;iterator.hasNext();)
					{
						PreEdge edge = iterator.next();
						delEdge(edge);
					}
				}
			}
		}
		
		treeStructure.deleteDescendantAndSelf(node);
	}
	
	/*public void deleteNodes(PreNode[] nodes)
	{
		for(PreNode node : nodes)
		{
			if(node.preTrace!=1)
			{
				boolean childOrSelfEnabled=false;
				
				if(node.size > 0)
				{
					int nodeSize = node.pre+node.size;
					for(int i=node.pre+1;i<=nodeSize;i++)		//Children
					{
						PreNode child = treeStructure.getNodeAt(i);
						if(child.enabled)
						{
							edgeProcessing.clearVirtualEdges(child);		//Clear virual edges
							childOrSelfEnabled=true;
						}
								
						edgeProcessing.clearPhysicalEdges(child);
						child.preTrace=1;
					}
				}
				
				if(node.enabled)
				{
					edgeProcessing.clearVirtualEdges(node);
					childOrSelfEnabled=true;
				}
				
				edgeProcessing.clearPhysicalEdges(node);
				treeStructure.deleteDescendantAndSelf(node);
				
				if(!childOrSelfEnabled)
				{
					PreNode parent = treeStructure.getEnabledAncestor(node);
					if(parent!=null)
					{
						//Reprocess parent
						edgeProcessing.clearVirtualEdges(parent);
						PreNodeAVLTree nodeToReprocess = new PreNodeAVLTree();
						edgeProcessing.appendEdgeHostingNeighbours(parent, nodeToReprocess, parent.pre);
						edgeProcessing.reprocessInducedEdges(nodeToReprocess, parent);
						edgeProcessing.processLocalInducedEdges(parent);
					}
				}
				
				node.preTrace=1;
			}
		}
	}*/
	
	public void addNode(PreNode node)		//We assume parent is well defined
	{
		treeStructure.insertAsChild(node, node.parent);
		
	}
	
	public void addNodes(PreNode[] nodes)
	{
	
	}
	
	public void addEdge(PreEdge edge)
	{
		PreNode minNode = edge.minNode;
		PreNode maxNode = edge.maxNode;
		
		//Add physical edges
		minNode.getForwardEdges().add(edge);
		maxNode.getBackwardEdges().add(edge);
		
		//Get nodes' parent
		PreNode minParent = treeStructure.getEnabledAncestorOrSelf(minNode);
		PreNode maxParent = treeStructure.getEnabledAncestorOrSelf(maxNode);
		
		if(minParent!=null && maxParent!=null && minParent!=maxParent)
		{
			/*DytsEdge dytsEdge = minParent.getVirtualEdge(edge, maxParent.getPre());
			if(dytsEdge!=null)
			{
				VirtualEdge virtualEdge = (VirtualEdge)dytsEdge;
				virtualEdge.addPhysicalEdge(edge);
			}
			else
			{
				//Create the virtual edge
				edgeProcessing.createVirtualEdge(edge, minParent, maxParent);
			}*/
		}
	}
	
	public void delEdge(PreEdge edge)
	{
		PreNode minNode = edge.minNode;
		PreNode maxNode = edge.maxNode;
		
		//Delete physical edges
		minNode.getForwardEdges().remove(edge);
		maxNode.getBackwardEdges().remove(edge);
		
		//Get nodes' parent
		PreNode minParent = treeStructure.getEnabledAncestorOrSelf(minNode);
		PreNode maxParent = treeStructure.getEnabledAncestorOrSelf(maxNode);
		
		if(minParent!=null && maxParent!=null && minParent!=maxParent)
		{
			//Get the virtual edge which represent the physical
			/*DytsEdge dytsEdge = minParent.getVirtualEdge(edge, maxParent.getPre());
			if(dytsEdge!=null)
			{
				VirtualEdge virtualEdge = (VirtualEdge)dytsEdge;
				virtualEdge.removePhysicalEdge(edge);
				
				//If the virtual edge no more contain physical edges, remove it
				if(virtualEdge.isEmpty())
				{
					minParent.removeVirtualEdge(virtualEdge);
					maxParent.removeVirtualEdge(virtualEdge);
				}
			}*/
		}
	}

	public TreeStructure getTreeStructure() {
		return treeStructure;
	}
}
