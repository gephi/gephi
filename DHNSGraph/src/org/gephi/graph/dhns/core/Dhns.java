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
package org.gephi.graph.dhns.core;

import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeRowFactory;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.dhns.graph.EdgeIterableImpl;
import org.gephi.graph.dhns.graph.NodeIterableImpl;
import org.openide.util.Lookup;

/**
 * Main class of the DHNS (Durable Hierarchical Network Structure) grapg structure..
 *
 * @author Mathieu Bastian
 */
public class Dhns {

    private TreeStructure treeStructure;
    private StructureModifier structureModifier;
    private GraphVersion graphVersion;
    private GraphFactoryImpl graphFactory;

    //Locking
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    //External
    private AttributeRowFactory attributesFactory;

    public Dhns() {
        treeStructure = new TreeStructure();
        graphVersion = new GraphVersion();
        structureModifier = new StructureModifier(this);
        graphFactory = new GraphFactoryImpl(this);
        init();

        attributesFactory = Lookup.getDefault().lookup(AttributeController.class).rowFactory();
    }

    public void init() {
        //importFakeGraph();
        //treeStructure.showTreeAsTable();
    }

    public void endImport() {
        //freeMode.init();
        //treeStructure.showTreeAsTable(sightManager.getMainSight());
    }

    private void importFakeGraph() {
        /*CompleteTreeImporter importer = new CompleteTreeImporter(treeStructure);

        //importer.importGraph(5, true);
        importer.importGraph(3, 6, false);
        //importer.shuffleEnable();
        System.out.println("Tree size : " + treeStructure.getTreeSize());
        //treeStructure.showTreeAsTable();

        RandomEdgesGenerator reg = new RandomEdgesGenerator(treeStructure);
        reg.generatPhysicalEdges(30);
        freeMode.init();

         */
    }

    public TreeStructure getTreeStructure() {
        return treeStructure;
    }

    public StructureModifier getStructureModifier() {
        return structureModifier;
    }

    public GraphVersion getGraphVersion() {
        return graphVersion;
    }

    public GraphFactoryImpl getGraphFactory() {
        return graphFactory;
    }

    public NodeIterable newNodeIterable(Iterator<Node> iterator) {
        return new NodeIterableImpl(iterator, readWriteLock.readLock());
    }

    public EdgeIterable newEdgeIterable(Iterator<Edge> iterator) {
        return new EdgeIterableImpl(iterator, readWriteLock.readLock());
    }

    public AttributeRow newNodeAttributes() {
        if (attributesFactory == null) {
            return null;
        }
        return attributesFactory.newNodeRow();
    }

    public AttributeRow newEdgeAttributes() {
        if (attributesFactory == null) {
            return null;
        }
        return attributesFactory.newEdgeRow();
    }

    //Locking
    public Lock getReadLock() {
        return readWriteLock.readLock();
    }

    public Lock getWriteLock() {
        return readWriteLock.writeLock();
    }
}
