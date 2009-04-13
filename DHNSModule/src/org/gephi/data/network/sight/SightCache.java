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
package org.gephi.data.network.sight;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.gephi.data.network.Dhns;
import org.gephi.data.network.api.FreeModifier;
import org.gephi.data.network.config.DHNSConfig.ViewType;
import org.gephi.data.network.edge.DhnsEdge;
import org.gephi.data.network.tree.TreeStructure;
import org.gephi.data.network.node.PreNode;
import org.gephi.data.network.node.treelist.SingleTreeIterator;
import org.gephi.data.network.utils.CachedIterator;
import org.gephi.data.network.edge.iterators.EdgesOutIterator;
import org.gephi.data.network.edge.iterators.HierarchyEdgesIterator;
import org.gephi.data.network.node.NodeImpl;
import org.gephi.data.network.node.treelist.SightTreeIterator;

/**
 *
 * @author Mathieu Bastian
 */
public class SightCache {

    private SightImpl sight;
    private Dhns dhns;

    //States
    private AtomicBoolean resetNode = new AtomicBoolean(true);
    private AtomicBoolean resetEdge = new AtomicBoolean(true);

    //Cache
    private List<PreNode> nodeCache = new ArrayList<PreNode>();
    private List<DhnsEdge> edgeCache = new ArrayList<DhnsEdge>();

    public SightCache(Dhns dhns, SightImpl sight) {
        this.dhns = dhns;
        this.sight = sight;
    }

    private void resetNodes() {
        TreeStructure treeStructure = dhns.getTreeStructure();
        nodeCache.clear();
        Iterator<PreNode> treeIterator = null;
        if (dhns.getConfig().getViewType().equals(ViewType.SINGLE)) {
            treeIterator = new SingleTreeIterator(treeStructure, sight);
        } else if (dhns.getConfig().getViewType().equals(ViewType.HIERARCHY)) {
            treeIterator = new SightTreeIterator(treeStructure, sight);
        }
        CachedIterator<PreNode> cacheIterator = new CachedIterator<PreNode>(treeIterator, nodeCache);
        for (; cacheIterator.hasNext(); cacheIterator.next()) {
        }      //Write cache
    }

    private void resetEdges() {
        TreeStructure treeStructure = dhns.getTreeStructure();
        edgeCache.clear();
        Iterator<DhnsEdge> edgesIterator = null;
        if (dhns.getConfig().getViewType().equals(ViewType.SINGLE)) {
            edgesIterator = new EdgesOutIterator(treeStructure, sight);
        } else if (dhns.getConfig().getViewType().equals(ViewType.HIERARCHY)) {
            edgesIterator = new HierarchyEdgesIterator(treeStructure, sight);
        }
        CachedIterator<DhnsEdge> cacheIterator = new CachedIterator<DhnsEdge>(edgesIterator, edgeCache);
        for (; cacheIterator.hasNext(); cacheIterator.next()) {
        }
    }

    private void fakeNodeUpdate()
    {
        FreeModifier mod = dhns.getFreeModifier();
        for(int i=0;i<10;i++)
        {
            NodeImpl n = new NodeImpl();
            mod.addNode(n, dhns.getTreeStructure().getRoot().getNode());
            n.getPreNode().setEnabled(sight, true);
        }
    }

    public Iterator<PreNode> getNodeIterator() {
        if (resetNode.getAndSet(true)) {
            fakeNodeUpdate();
            resetNodes();
        }
        return nodeCache.iterator();
    }

    public Iterator<DhnsEdge> getEdgeIterator() {
        if (resetEdge.getAndSet(false)) {
            resetEdges();
        }
        return edgeCache.iterator();
    }

    public boolean requireNodeUpdate() {
        return resetNode.get();
    }

    public boolean requireEdgeUpdate() {
        return resetEdge.get();
    }

    public void reset() {
        resetNode.set(true);
        resetEdge.set(true);
    }
}
