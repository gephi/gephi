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

package gephi.data.network.sight;

import gephi.data.network.DhnsController;
import gephi.data.network.TreeStructure;
import gephi.data.network.cache.CachedIterator;
import gephi.data.network.edge.VirtualEdge;
import gephi.data.network.node.PreNode;
import gephi.data.network.node.treelist.SingleTreeIterator;
import gephi.data.network.tree.iterators.edges.EdgesOutIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Mathieu
 */
public class SightCache {

    private Sight sight;

    //States
    private AtomicBoolean resetNode         = new AtomicBoolean(true);
    private AtomicBoolean resetEdge         = new AtomicBoolean(true);

    //Cache
    private List<PreNode> nodeCache         = new ArrayList<PreNode>();
    private List<VirtualEdge> edgeCache     = new ArrayList<VirtualEdge>();
    
    public SightCache(Sight sight)
    {
        this.sight = sight;
    }

    public Iterator<PreNode> getNodeIterator()
    {
        DhnsController controller = DhnsController.getInstance();
        TreeStructure treeStructure = controller.getTreeStructure();
        if(resetNode.getAndSet(false))
		{
			nodeCache.clear();
            SingleTreeIterator treeIterator = new SingleTreeIterator(treeStructure, sight);
            CachedIterator<PreNode> cacheIterator = new CachedIterator<PreNode>(treeIterator,nodeCache);
			return cacheIterator;
		}

		return nodeCache.iterator();
    }

    public Iterator<VirtualEdge> getEdgeIterator()
    {
        DhnsController controller = DhnsController.getInstance();
        TreeStructure treeStructure = controller.getTreeStructure();
        if(resetEdge.getAndSet(false))
		{
			edgeCache.clear();
            EdgesOutIterator edgesIterator = new EdgesOutIterator(treeStructure, sight);
            CachedIterator<VirtualEdge> cacheIterator = new CachedIterator<VirtualEdge>(edgesIterator,edgeCache);
			return cacheIterator;
		}

		return edgeCache.iterator();
    }

    public boolean requireNodeUpdate()
    {
        return resetNode.get();
    }


    public boolean requireEdgeUpdate()
    {
        return resetEdge.get();
    }

    public void reset()
    {
        resetNode.set(true);
        resetEdge.set(true);
    }
}
