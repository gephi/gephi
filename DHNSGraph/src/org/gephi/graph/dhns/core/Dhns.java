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
import org.gephi.graph.api.DecoratorFactory;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphListener;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalDirectedGraph;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.HierarchicalMixedGraph;
import org.gephi.graph.api.HierarchicalUndirectedGraph;
import org.gephi.graph.api.MixedGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.Predicate;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.graph.api.View;
import org.gephi.graph.api.Views;
import org.gephi.graph.dhns.DhnsGraphController;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.iterators.AbstractEdgeIterator;
import org.gephi.graph.dhns.graph.HierarchicalDirectedGraphImpl;
import org.gephi.graph.dhns.graph.HierarchicalDirectedGraphImplFiltered;
import org.gephi.graph.dhns.graph.HierarchicalMixedGraphImpl;
import org.gephi.graph.dhns.graph.HierarchicalMixedGraphImplFiltered;
import org.gephi.graph.dhns.graph.HierarchicalUndirectedGraphImpl;
import org.gephi.graph.dhns.graph.HierarchicalUndirectedGraphImplFiltered;
import org.gephi.graph.dhns.graph.iterators.EdgeIterableImpl;
import org.gephi.graph.dhns.graph.iterators.NodeIterableImpl;
import org.gephi.graph.dhns.node.iterators.AbstractNodeIterator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Main class of the DHNS (Durable Hierarchical Network Structure) graph structure..
 *
 * @author Mathieu Bastian
 */
public class Dhns implements GraphModel {

    //Core
    private DhnsGraphController controller;
    private GraphStructure graphStructure;
    private StructureModifier structureModifier;
    private GraphVersion graphVersion;
    private EventManager eventManager;
    private DynamicManager dynamicManager;
    private DecoratorFactoryImpl decoratorFactory;
    private SettingsManager settingsManager;
    private ViewManager viewManager;

    //Type
    private boolean directed = false;
    private boolean undirected = false;
    private boolean mixed = false;

    //Locking
    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public Dhns(DhnsGraphController controller) {
        this.controller = controller;
        graphStructure = new GraphStructure();
        graphVersion = new GraphVersion();
        structureModifier = new StructureModifier(this);
        eventManager = new EventManager(this);
        dynamicManager = new DynamicManager(this);
        decoratorFactory = new DecoratorFactoryImpl(this);
        settingsManager = new SettingsManager(this);
        viewManager = new ViewManager(this);

        init();
    }

    public void init() {
    }

    public DhnsGraphController getController() {
        return controller;
    }

    public GraphStructure getGraphStructure() {
        return graphStructure;
    }

    public StructureModifier getStructureModifier() {
        return structureModifier;
    }

    public GraphVersion getGraphVersion() {
        return graphVersion;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public IDGen getIdGen() {
        return controller.getIDGen();
    }

    public DynamicManager getDynamicManager() {
        return dynamicManager;
    }

    public DecoratorFactoryImpl getDecoratorFactory() {
        return decoratorFactory;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    public ViewManager getViewManager() {
        return viewManager;
    }

    public NodeIterable newNodeIterable(AbstractNodeIterator iterator) {
        return new NodeIterableImpl(iterator, readWriteLock.readLock());
    }

    public EdgeIterable newEdgeIterable(AbstractEdgeIterator iterator) {
        return new EdgeIterableImpl(iterator, readWriteLock.readLock());
    }

    public NodeIterable newNodeIterable(AbstractNodeIterator iterator, Predicate<Node> predicate) {
        return new NodeIterableImpl(iterator, readWriteLock.readLock());
    }

    public EdgeIterable newEdgeIterable(AbstractEdgeIterator iterator, Predicate<AbstractEdge> predicate) {
        return new EdgeIterableImpl(iterator, readWriteLock.readLock(), predicate);
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

    //API
    public GraphFactoryImpl factory() {
        return controller.factory();
    }

    public DecoratorFactory decorators() {
        return getDecoratorFactory();
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

    public void setDirected(boolean directed) {
        this.directed = directed;
    }

    public void setUndirected(boolean undirected) {
        this.undirected = undirected;
    }

    public void setMixed(boolean mixed) {
        this.mixed = mixed;
    }

    public boolean isHierarchical() {
        return graphStructure.getStructure().treeHeight - 1 > 0;       //height>0
    }

    public boolean isDynamic() {
        return getDynamicManager().isDynamic();
    }

    public void addGraphListener(GraphListener graphListener) {
        eventManager.addListener(graphListener);
    }

    public void removeGraphListener(GraphListener graphListener) {
        eventManager.removeListener(graphListener);
    }

    public Graph getGraph() {
        if (directed) {
            return getDirectedGraph();
        } else if (undirected) {
            return getUndirectedGraph();
        } else if (mixed) {
            return getMixedGraph();
        } else {
            return getDirectedGraph();
        }
    }

    public DirectedGraph getDirectedGraph() {
        return new HierarchicalDirectedGraphImpl(this, graphStructure);
    }

    public UndirectedGraph getUndirectedGraph() {
        return new HierarchicalUndirectedGraphImpl(this, graphStructure);
    }

    public MixedGraph getMixedGraph() {
        return new HierarchicalMixedGraphImpl(this, graphStructure);
    }

    public HierarchicalGraph getHierarchicalGraph() {
        if (directed) {
            return getHierarchicalDirectedGraph();
        } else if (undirected) {
            return getHierarchicalUndirectedGraph();
        } else if (mixed) {
            return getHierarchicalMixedGraph();
        } else {
            return getHierarchicalDirectedGraph();
        }
    }

    public HierarchicalDirectedGraph getHierarchicalDirectedGraph() {
        return new HierarchicalDirectedGraphImpl(this, graphStructure);
    }

    public HierarchicalMixedGraph getHierarchicalMixedGraph() {
        return new HierarchicalMixedGraphImpl(this, graphStructure);
    }

    public HierarchicalUndirectedGraph getHierarchicalUndirectedGraph() {
        return new HierarchicalUndirectedGraphImpl(this, graphStructure);
    }

    public DirectedGraph getDirectedGraph(View view) {
        return new HierarchicalDirectedGraphImplFiltered(this, this.graphStructure, view);
    }

    public Graph getGraph(View view) {
        if (directed) {
            return getDirectedGraph(view);
        } else if (undirected) {
            return getUndirectedGraph(view);
        } else if (mixed) {
            return getMixedGraph(view);
        } else {
            return getDirectedGraph(view);
        }
    }

    public HierarchicalDirectedGraph getHierarchicalDirectedGraph(View view) {
        return new HierarchicalDirectedGraphImplFiltered(this, this.graphStructure, view);
    }

    public HierarchicalGraph getHierarchicalGraph(View view) {
        if (directed) {
            return getHierarchicalDirectedGraph(view);
        } else if (undirected) {
            return getHierarchicalUndirectedGraph(view);
        } else if (mixed) {
            return getHierarchicalMixedGraph(view);
        } else {
            return getHierarchicalDirectedGraph(view);
        }
    }

    public HierarchicalMixedGraph getHierarchicalMixedGraph(View view) {
        return new HierarchicalMixedGraphImplFiltered(this, this.graphStructure, view);
    }

    public HierarchicalUndirectedGraph getHierarchicalUndirectedGraph(View view) {
        return new HierarchicalUndirectedGraphImplFiltered(this, this.graphStructure, view);
    }

    public MixedGraph getMixedGraph(View view) {
        return new HierarchicalMixedGraphImplFiltered(this, this.graphStructure, view);
    }

    public UndirectedGraph getUndirectedGraph(View view) {
        return new HierarchicalUndirectedGraphImplFiltered(this, this.graphStructure, view);
    }

    public Graph getGraphVisible() {
        if (directed) {
            return getDirectedGraph(viewManager.getVisibleView());
        } else if (undirected) {
            return getUndirectedGraph(viewManager.getVisibleView());
        } else if (mixed) {
            return getMixedGraph(viewManager.getVisibleView());
        } else {
            return getDirectedGraph(viewManager.getVisibleView());
        }
    }

    public DirectedGraph getDirectedGraphVisible() {
        return getDirectedGraph(viewManager.getVisibleView());
    }

    public UndirectedGraph getUndirectedGraphVisible() {
        return getUndirectedGraph(viewManager.getVisibleView());
    }

    public MixedGraph getMixedGraphVisible() {
        return getMixedGraph(viewManager.getVisibleView());
    }

    public HierarchicalGraph getHierarchicalGraphVisible() {
        if (directed) {
            return getHierarchicalDirectedGraph(viewManager.getVisibleView());
        } else if (undirected) {
            return getHierarchicalUndirectedGraph(viewManager.getVisibleView());
        } else if (mixed) {
            return getHierarchicalMixedGraph(viewManager.getVisibleView());
        } else {
            return getHierarchicalDirectedGraph(viewManager.getVisibleView());
        }
    }

    public HierarchicalDirectedGraph getHierarchicalDirectedGraphVisible() {
        return getHierarchicalDirectedGraph(viewManager.getVisibleView());
    }

    public HierarchicalMixedGraph getHierarchicalMixedGraphVisible() {
        return getHierarchicalMixedGraph(viewManager.getVisibleView());
    }

    public HierarchicalUndirectedGraph getHierarchicalUndirectedGraphVisible() {
        return getHierarchicalUndirectedGraph(viewManager.getVisibleView());
    }

    public Views views() {
        return viewManager;
    }

    public void readXML(Element element) {
    }

    public Element writeXML(Document document) {
        return null;
    }

    public GraphModel copy() {
        return null;
    }
}
