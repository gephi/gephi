/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.graph.dhns.core;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeRowFactory;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphListener;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphSettings;
import org.gephi.graph.api.HierarchicalDirectedGraph;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.HierarchicalMixedGraph;
import org.gephi.graph.api.HierarchicalUndirectedGraph;
import org.gephi.graph.api.MixedGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.dhns.DhnsGraphController;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.iterators.AbstractEdgeIterator;
import org.gephi.graph.dhns.graph.HierarchicalDirectedGraphImpl;
import org.gephi.graph.dhns.graph.HierarchicalGraphImpl;
import org.gephi.graph.dhns.graph.HierarchicalMixedGraphImpl;
import org.gephi.graph.dhns.graph.HierarchicalUndirectedGraphImpl;
import org.gephi.graph.dhns.graph.iterators.EdgeIterableImpl;
import org.gephi.graph.dhns.graph.iterators.NodeIterableImpl;
import org.gephi.graph.dhns.node.iterators.AbstractNodeIterator;
import org.gephi.graph.dhns.predicate.Predicate;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
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
    private GraphVersion graphVersion;
    private EventManager eventManager;
    private SettingsManager settingsManager;
    private GraphFactoryImpl factory;
    private DuplicateManager duplicateManager;
    //Type
    private boolean directed = false;
    private boolean undirected = false;
    private boolean mixed = false;
    //Locking
    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public Dhns(DhnsGraphController controller, Workspace workspace) {
        this.controller = controller;
        graphVersion = new GraphVersion();
        eventManager = new EventManager(this);
        settingsManager = new SettingsManager(this);
        graphStructure = new GraphStructure(this);
        duplicateManager = new DuplicateManager(this);

        eventManager.start();

        //AttributeFactory
        AttributeRowFactory attributeRowFactory = null;
        if (workspace != null) {
            AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel(workspace);
            if (attributeModel != null) {
                attributeRowFactory = attributeModel.rowFactory();
            }
        }
        factory = new GraphFactoryImpl(controller.getIDGen(), attributeRowFactory);

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

    public GraphVersion getGraphVersion() {
        return graphVersion;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public IDGen getIdGen() {
        return controller.getIDGen();
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    public DuplicateManager getDuplicateManager() {
        return duplicateManager;
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

    public ReentrantReadWriteLock getReadWriteLock() {
        return readWriteLock;
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
        return factory;
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
        return graphStructure.getMainView().getStructure().getTreeHeight() - 1 > 0;       //height>0
    }

    public void addGraphListener(GraphListener graphListener) {
        eventManager.addGraphListener(graphListener);
    }

    public void removeGraphListener(GraphListener graphListener) {
        eventManager.removeGraphListener(graphListener);
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
        return new HierarchicalDirectedGraphImpl(this, graphStructure.getMainView());
    }

    public UndirectedGraph getUndirectedGraph() {
        return new HierarchicalUndirectedGraphImpl(this, graphStructure.getMainView());
    }

    public MixedGraph getMixedGraph() {
        return new HierarchicalMixedGraphImpl(this, graphStructure.getMainView());
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
        return new HierarchicalDirectedGraphImpl(this, graphStructure.getMainView());
    }

    public HierarchicalMixedGraph getHierarchicalMixedGraph() {
        return new HierarchicalMixedGraphImpl(this, graphStructure.getMainView());
    }

    public HierarchicalUndirectedGraph getHierarchicalUndirectedGraph() {
        return new HierarchicalUndirectedGraphImpl(this, graphStructure.getMainView());
    }

    public DirectedGraph getDirectedGraph(GraphView view) {
        return new HierarchicalDirectedGraphImpl(this, (GraphViewImpl) view);
    }

    public Graph getGraph(GraphView view) {
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

    public HierarchicalDirectedGraph getHierarchicalDirectedGraph(GraphView view) {
        return new HierarchicalDirectedGraphImpl(this, (GraphViewImpl) view);
    }

    public HierarchicalGraph getHierarchicalGraph(GraphView view) {
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

    public HierarchicalMixedGraph getHierarchicalMixedGraph(GraphView view) {
        return new HierarchicalMixedGraphImpl(this, (GraphViewImpl) view);
    }

    public HierarchicalUndirectedGraph getHierarchicalUndirectedGraph(GraphView view) {
        return new HierarchicalUndirectedGraphImpl(this, (GraphViewImpl) view);
    }

    public MixedGraph getMixedGraph(GraphView view) {
        return new HierarchicalMixedGraphImpl(this, (GraphViewImpl) view);
    }

    public UndirectedGraph getUndirectedGraph(GraphView view) {
        return new HierarchicalUndirectedGraphImpl(this, (GraphViewImpl) view);
    }

    public Graph getGraphVisible() {
        if (directed) {
            return getDirectedGraph(graphStructure.getVisibleView());
        } else if (undirected) {
            return getUndirectedGraph(graphStructure.getVisibleView());
        } else if (mixed) {
            return getMixedGraph(graphStructure.getVisibleView());
        } else {
            return getDirectedGraph(graphStructure.getVisibleView());
        }
    }

    public DirectedGraph getDirectedGraphVisible() {
        return getDirectedGraph(graphStructure.getVisibleView());
    }

    public UndirectedGraph getUndirectedGraphVisible() {
        return getUndirectedGraph(graphStructure.getVisibleView());
    }

    public MixedGraph getMixedGraphVisible() {
        return getMixedGraph(graphStructure.getVisibleView());
    }

    public HierarchicalGraph getHierarchicalGraphVisible() {
        if (directed) {
            return getHierarchicalDirectedGraph(graphStructure.getVisibleView());
        } else if (undirected) {
            return getHierarchicalUndirectedGraph(graphStructure.getVisibleView());
        } else if (mixed) {
            return getHierarchicalMixedGraph(graphStructure.getVisibleView());
        } else {
            return getHierarchicalDirectedGraph(graphStructure.getVisibleView());
        }
    }

    public HierarchicalDirectedGraph getHierarchicalDirectedGraphVisible() {
        return getHierarchicalDirectedGraph(graphStructure.getVisibleView());
    }

    public HierarchicalMixedGraph getHierarchicalMixedGraphVisible() {
        return getHierarchicalMixedGraph(graphStructure.getVisibleView());
    }

    public HierarchicalUndirectedGraph getHierarchicalUndirectedGraphVisible() {
        return getHierarchicalUndirectedGraph(graphStructure.getVisibleView());
    }

    public GraphSettings settings() {
        return settingsManager;
    }

    public void pushFrom(Graph graph) {
        if (graph == null) {
            throw new NullPointerException();
        }
        HierarchicalGraphImpl graphImpl = (HierarchicalGraphImpl) graph;
        if (graphImpl.getGraphModel() == this) {
            throw new IllegalArgumentException("The graph must be from a different Workspace");
        }
        Dhns source = (Dhns) graphImpl.getGraphModel();
        source.getDuplicateManager().duplicate(this, (GraphViewImpl) graphImpl.getView());
        graphVersion.incNodeAndEdgeVersion();
        // eventManager.fireEvent(EventType.NODES_AND_EDGES_UPDATED);
    }

    public void clear() {
        graphVersion = new GraphVersion();
        graphStructure = new GraphStructure(this);
    }

    public void readXML(Element element) {
    }

    public Element writeXML(Document document) {
        return null;
    }

    public GraphModel copy() {
        return null;
    }

    public GraphView newView() {
        return graphStructure.getNewView();
    }

    public GraphView copyView(GraphView view) {
        return graphStructure.copyView((GraphViewImpl) view);
    }

    public void destroyView(GraphView view) {
        graphStructure.destroyView((GraphViewImpl) view);
    }

    public void setVisibleView(GraphView view) {
        graphStructure.setVisibleView(view != null ? (GraphViewImpl) view : null);
    }

    public GraphView getVisibleView() {
        return graphStructure.getVisibleView();
    }
}
