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
package org.gephi.data.network;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.gephi.data.network.api.FreeModifier;
import org.gephi.data.network.tree.TreeStructure;
import org.gephi.data.network.config.DHNSConfig;
import org.gephi.data.network.dictionary.DictionaryImpl;
import org.gephi.data.network.mode.FreeMode;
import org.gephi.data.network.potato.PotatoManager;
import org.gephi.data.network.cache.NetworkCache;
import org.gephi.data.network.tree.importer.CompleteTreeImporter;
import org.gephi.data.network.utils.RandomEdgesGenerator;

public class Dhns {

    private DHNSConfig config;
    private TreeStructure treeStructure;
    private FreeMode freeMode;
    private PotatoManager potatoManager;
    private DictionaryImpl dictionary;
    private NetworkCache networkCache;

    //Locking
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public Dhns() {
        config = new DHNSConfig();
        treeStructure = new TreeStructure();
        freeMode = new FreeMode(this);
        dictionary = new DictionaryImpl();
        potatoManager = new PotatoManager(this);
        networkCache = new NetworkCache(this);
        init();
    }

    public void init() {
       //importFakeGraph();
       //treeStructure.showTreeAsTable();
    }

    public void endImport() {
        freeMode.init();
        //treeStructure.showTreeAsTable(sightManager.getMainSight());
    }

    private void importFakeGraph() {
        CompleteTreeImporter importer = new CompleteTreeImporter(treeStructure);

        //importer.importGraph(5, true);
        importer.importGraph(3, 6, false);
        //importer.shuffleEnable();
        System.out.println("Tree size : " + treeStructure.getTreeSize());
        //treeStructure.showTreeAsTable();

        RandomEdgesGenerator reg = new RandomEdgesGenerator(treeStructure);
        reg.generatPhysicalEdges(30);
        freeMode.init();


    }

    public TreeStructure getTreeStructure() {
        return treeStructure;
    }

    public DHNSConfig getConfig() {
        return config;
    }

    public FreeModifier getFreeModifier() {
        return freeMode;
    }

    public PotatoManager getPotatoManager() {
        return potatoManager;
    }

    public DictionaryImpl getDictionary() {
        return dictionary;
    }

    public NetworkCache getNetworkCache() {
        return networkCache;
    }

    //Locking
    public Lock getReadLock() {
        return readWriteLock.readLock();
    }

    public Lock getWriteLock() {
        return readWriteLock.writeLock();
    }
}
