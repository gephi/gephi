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
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.gephi.data.network.Dhns;
import org.gephi.data.network.config.DHNSConfig.ViewType;
import org.gephi.data.network.edge.DhnsEdge;
import org.gephi.data.network.tree.TreeStructure;
import org.gephi.data.network.node.PreNode;
import org.gephi.data.network.node.treelist.SingleTreeIterator;
import org.gephi.data.network.utils.CachedIterator;
import org.gephi.data.network.edge.iterators.EdgesOutIterator;
import org.gephi.data.network.edge.iterators.HierarchyEdgesIterator;
import org.gephi.data.network.node.treelist.SightTreeIterator;

/**
 *
 * @author Mathieu Bastian
 */
public class SightCache {

    private SightImpl sight;
    private Dhns dhns;

    //States
    private AtomicBoolean orderResetNode = new AtomicBoolean();
    private AtomicBoolean orderResetEdge = new AtomicBoolean();
    private AtomicBoolean newDataReady = new AtomicBoolean();

    //Cache
    private AtomicReference<CacheContent> cache;

    //Executor
    private ExecutorService resetExecutor;
    private ExecutorService tasksExecutor;
    private CompletionService<CacheContent> completionService;

    public SightCache(Dhns dhns, SightImpl sight) {
        this.dhns = dhns;
        this.sight = sight;
        this.cache = new AtomicReference<CacheContent>(new CacheContent());

        //Executors
        resetExecutor = Executors.newSingleThreadExecutor();
        tasksExecutor = Executors.newFixedThreadPool(2);
        completionService = new ExecutorCompletionService(tasksExecutor);
    }

    /*private void fakeNodeUpdate() {
    FreeModifier mod = dhns.getFreeModifier();
    for (int i = 0; i < 3; i++) {
    NodeImpl n = new NodeImpl();
    mod.addNode(n, dhns.getTreeStructure().getRoot().getNode());
    n.getPreNode().setEnabled(sight, true);
    }
    }

    private void fakeEdgeUpdate() {
    FreeModifier mod = dhns.getFreeModifier();
    for (int i = 0; i < 10; i++) {
    PreNode n1 = nodeCache.get((int) (Math.random() * nodeCache.size() - 1) + 1);
    PreNode n2 = nodeCache.get((int) (Math.random() * nodeCache.size() - 1) + 1);
    if (n1 != n2) {
    EdgeImpl edge = new EdgeImpl(n1.getNode(), n2.getNode());
    mod.addEdge(edge);
    }
    }
    }*/
    public Iterator<PreNode> getNodeIterator() {
        return cache.get().nodeCache.iterator();
    }

    public Iterator<DhnsEdge> getEdgeIterator() {
        return cache.get().edgeCache.iterator();
    }

    private int i=0;
    public boolean requireNodeUpdate() {
        if(newDataReady.get())
        {
            i=1;
            return true;
        }
        return false;
    }

    public boolean requireEdgeUpdate() {
        if(i==1 && newDataReady.getAndSet(false))
        {
            i=0;
            return true;
        }
        return false;
    }

    public void reset() {
        orderResetNode.set(true);
        orderResetEdge.set(true);
        doReset();
    }

    private void doReset() {
        resetExecutor.execute(new Runnable() {

            public void run() {
                System.out.println("reset Task");
                boolean nodeTask = false, edgeTask = false;
                int tasks = 0;

                //Submit tasks
                if (orderResetNode.getAndSet(false)) {
                    nodeTask = true;
                    tasks++;
                    completionService.submit(new resetNodesCallable());
                }

                if (orderResetEdge.getAndSet(false)) {
                    edgeTask = true;
                    tasks++;
                    completionService.submit(new resetEdgesCallable());
                }

                //New cacheContent
                CacheContent cacheContent = new CacheContent(cache.get());

                //Wait for completion of all tasks
                for (int i = 0; i < tasks; i++) {
                    try {
                        Future<CacheContent> f = completionService.take();
                        CacheContent taskContent = f.get();
                        cacheContent.appendContent(taskContent);
                    } catch (ExecutionException ex) {
                        ex.printStackTrace();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }

                //Swap ref
                cache.set(cacheContent);
                newDataReady.set(true);
                /*if (nodeTask) {
                    newNodesReady.set(true);
                }
                if (edgeTask) {
                    newEdgesReady.set(true);
                }*/
            }
        });
    }

    private class resetNodesCallable implements Callable<CacheContent> {

        public CacheContent call() throws Exception {
            System.out.println("reset Nodes call");
            TreeStructure treeStructure = dhns.getTreeStructure();
            List<PreNode> nodeCache = new ArrayList<PreNode>();
            Iterator<PreNode> treeIterator = null;

            dhns.getReadLock().lock();

            if (dhns.getConfig().getViewType().equals(ViewType.SINGLE)) {
                treeIterator = new SingleTreeIterator(treeStructure, sight);
            } else if (dhns.getConfig().getViewType().equals(ViewType.HIERARCHY)) {
                treeIterator = new SightTreeIterator(treeStructure, sight);
            }
            CachedIterator<PreNode> cacheIterator = new CachedIterator<PreNode>(treeIterator, nodeCache);
            for (; cacheIterator.hasNext(); cacheIterator.next()) {
            }      //Write cache

            dhns.getReadLock().unlock();
            return new CacheContent(nodeCache, null);
        }
    }

    private class resetEdgesCallable implements Callable<CacheContent> {

        public CacheContent call() throws Exception {
            System.out.println("reset Edges call");
            TreeStructure treeStructure = dhns.getTreeStructure();
            List<DhnsEdge> edgeCache = new ArrayList<DhnsEdge>();
            Iterator<DhnsEdge> edgesIterator = null;

            dhns.getReadLock().lock();

            if (dhns.getConfig().getViewType().equals(ViewType.SINGLE)) {
                edgesIterator = new EdgesOutIterator(treeStructure, sight);
            } else if (dhns.getConfig().getViewType().equals(ViewType.HIERARCHY)) {
                edgesIterator = new HierarchyEdgesIterator(treeStructure, sight);
            }
            CachedIterator<DhnsEdge> cacheIterator = new CachedIterator<DhnsEdge>(edgesIterator, edgeCache);
            for (; cacheIterator.hasNext(); cacheIterator.next()) {
            }

            dhns.getReadLock().unlock();
            return new CacheContent(null, edgeCache);
        }
    }

    private class CacheContent {

        private List<PreNode> nodeCache;
        private List<DhnsEdge> edgeCache;

        public CacheContent() {
            this.nodeCache = new ArrayList<PreNode>();
            this.edgeCache = new ArrayList<DhnsEdge>();
        }

        public CacheContent(CacheContent instance) {
            this.nodeCache = instance.nodeCache;
            this.edgeCache = instance.edgeCache;
        }

        public CacheContent(List<PreNode> nodeCache, List<DhnsEdge> edgeCache) {
            this.nodeCache = nodeCache;
            this.edgeCache = edgeCache;
        }

        public void appendContent(CacheContent newContent) {
            if (newContent.nodeCache != null) {
                this.nodeCache = newContent.nodeCache;
            }

            if (newContent.edgeCache != null) {
                this.edgeCache = newContent.edgeCache;
            }
        }
    }
}
