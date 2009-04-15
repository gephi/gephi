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
    private SightManager manager;

    //States
    private AtomicBoolean orderResetNode = new AtomicBoolean();
    private AtomicBoolean orderResetEdge = new AtomicBoolean();

    //Cache
    private AtomicReference<SightCacheContent> cache;

    //Executor
    private ExecutorService resetExecutor;
    private ExecutorService tasksExecutor;
    private CompletionService<SightCacheContent> completionService;

    public SightCache(Dhns dhns, SightImpl sight) {
        this.sight = sight;
        this.dhns = dhns;
        this.manager = dhns.getSightManager();
        this.cache = new AtomicReference<SightCacheContent>(new SightCacheContent());

        //Executors
        resetExecutor = Executors.newSingleThreadExecutor();
        tasksExecutor = Executors.newFixedThreadPool(2);
        completionService = new ExecutorCompletionService(tasksExecutor);
    }

    public SightCacheContent getCacheContent() {
        return cache.get();
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
                int tasks = 0;

                //Submit tasks
                if (orderResetNode.getAndSet(false)) {
                    tasks++;
                    completionService.submit(new resetNodesCallable());
                }

                if (orderResetEdge.getAndSet(false)) {
                    tasks++;
                    completionService.submit(new resetEdgesCallable());
                }

                //New cacheContent
                SightCacheContent cacheContent = new SightCacheContent(cache.get());

                //Wait for completion of all tasks
                for (int i = 0; i < tasks; i++) {
                    try {
                        Future<SightCacheContent> f = completionService.take();
                        SightCacheContent taskContent = f.get();
                        cacheContent.appendContent(taskContent);
                    } catch (ExecutionException ex) {
                        ex.printStackTrace();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }

                //Swap ref
                cache.set(cacheContent);
            }
        });
    }

    private class resetNodesCallable implements Callable<SightCacheContent> {

        public SightCacheContent call() throws Exception {
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
            return new SightCacheContent(nodeCache, null);
        }
    }

    private class resetEdgesCallable implements Callable<SightCacheContent> {

        public SightCacheContent call() throws Exception {
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
            return new SightCacheContent(null, edgeCache);
        }
    }
}
