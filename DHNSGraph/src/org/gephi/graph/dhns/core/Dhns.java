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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.dhns.DhnsGraphController;
import org.gephi.graph.dhns.edge.iterators.AbstractEdgeIterator;
import org.gephi.graph.dhns.graph.Condition;
import org.gephi.graph.dhns.graph.EdgeIterableImpl;
import org.gephi.graph.dhns.graph.NodeIterableImpl;
import org.gephi.graph.dhns.node.iterators.AbstractNodeIterator;
import org.gephi.graph.dhns.view.ViewManager;

/**
 * Main class of the DHNS (Durable Hierarchical Network Structure) graph structure..
 *
 * @author Mathieu Bastian
 */
public class Dhns {

    //Core
    private DhnsGraphController controller;
    private TreeStructure treeStructure;
    private StructureModifier structureModifier;
    private GraphVersion graphVersion;
    private EventManager eventManager;
    private ViewManager viewManager;
    private PropositionManager propositionManager;
    private DynamicManager dynamicManager;

    //Type
    private boolean directed = false;
    private boolean undirected = false;
    private boolean mixed = false;

    //Locking
    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public Dhns(DhnsGraphController controller) {
        this.controller = controller;
        viewManager = new ViewManager(this);
        treeStructure = new TreeStructure(this);
        graphVersion = new GraphVersion();
        structureModifier = new StructureModifier(this);
        eventManager = new EventManager(this);
        propositionManager = new PropositionManager(this);
        dynamicManager = new DynamicManager(this);
        init();
    }

    public void init() {
    }

    public DhnsGraphController getController() {
        return controller;
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
        return controller.factory();
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public ViewManager getViewManager() {
        return viewManager;
    }

    public PropositionManager getPropositionManager() {
        return propositionManager;
    }

    public IDGen getIdGen() {
        return controller.getIDGen();
    }

    public DynamicManager getDynamicManager() {
        return dynamicManager;
    }

    public NodeIterable newNodeIterable(AbstractNodeIterator iterator) {
        return new NodeIterableImpl(iterator, readWriteLock.readLock());
    }

    public EdgeIterable newEdgeIterable(AbstractEdgeIterator iterator) {
        return new EdgeIterableImpl(iterator, readWriteLock.readLock());
    }

    public NodeIterable newNodeIterable(AbstractNodeIterator iterator, Condition<Node> condition) {
        return new NodeIterableImpl(iterator, readWriteLock.readLock(), condition);
    }

    public EdgeIterable newEdgeIterable(AbstractEdgeIterator iterator, Condition<Edge> condition) {
        return new EdgeIterableImpl(iterator, readWriteLock.readLock(), condition);
    }

    //Locking
    public Lock getReadLock() {
        return readWriteLock.readLock();
    }

    public Lock getWriteLock() {
        if (readWriteLock.getReadHoldCount() > 0) {
            throw new IllegalMonitorStateException("Impossible to acquire a write lock when currently holding a read lock. Use toArray() methods on NodeIterable and EdgeIterable to avoid holding a readLock.");
        }
        return readWriteLock.writeLock();
    }

    //Type
    public void touchDirected() {
        if (undirected || mixed) {
            touchMixed();
        } else {
            directed = true;
        }
    }

    public void touchUndirected() {
        if (directed || mixed) {
            touchMixed();
        } else {
            undirected = true;
        }
    }

    public void touchMixed() {
        directed = false;
        undirected = false;
        mixed = true;
    }

    public boolean isDirected() {
        return directed;
    }

    public boolean isMixed() {
        return mixed;
    }

    public boolean isUndirected() {
        return undirected;
    }
}
