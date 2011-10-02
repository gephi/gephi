/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.graph.dhns.core;

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
    private final Workspace workspace;
    private final DhnsGraphController controller;
    private GraphStructure graphStructure;
    private GraphVersion graphVersion;
    private final EventManager eventManager;
    private final SettingsManager settingsManager;
    private final GraphFactoryImpl factory;
    private final DuplicateManager duplicateManager;
    //Type
    private boolean directed = false;
    private boolean undirected = false;
    private boolean mixed = false;
    //Locking
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public Dhns(DhnsGraphController controller, Workspace workspace) {
        this.controller = controller;
        this.workspace = workspace;
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
    public void readLock() {
        /*if (SwingUtilities.isEventDispatchThread()) {
        Throwable r = new RuntimeException();
        int i = 0;
        for (i = 0; i < r.getStackTrace().length; i++) {
        if (!r.getStackTrace()[i].toString().startsWith("org.gephi.graph.dhns")) {
        break;
        }
        }
        System.err.println("WARNING: readLock() on the EDT - " + r.getStackTrace()[i].toString());
        }*/
        //String t = Thread.currentThread().toString();
        //Logger.getLogger("").log(Level.WARNING, "{0} read lock", Thread.currentThread());
        readWriteLock.readLock().lock();
    }

    public void readUnlock() {
        readWriteLock.readLock().unlock();
    }

    public void readUnlockAll() {
        ReentrantReadWriteLock lock = readWriteLock;
        final int nReadLocks = lock.getReadHoldCount();
        for (int n = 0; n < nReadLocks; n++) {
            lock.readLock().unlock();
        }
    }

    public boolean conditionalWriteLock() {
        if (readWriteLock.getReadHoldCount() > 0) {
            throw new IllegalMonitorStateException("Impossible to acquire a write lock when currently holding a read lock. Use toArray() methods on NodeIterable and EdgeIterable to avoid holding a readLock.");
        }
        if (!readWriteLock.isWriteLockedByCurrentThread()) {
            readWriteLock.writeLock().lock();
            return true;
        }
        return false;
    }
    
    public void conditionalWriteUnlock(boolean locked) {
        if(locked) {
            readWriteLock.writeLock().unlock();
        }
    }

    public void writeLock() {
        if (readWriteLock.getReadHoldCount() > 0) {
            throw new IllegalMonitorStateException("Impossible to acquire a write lock when currently holding a read lock. Use toArray() methods on NodeIterable and EdgeIterable to avoid holding a readLock.");
        }
        /*if (SwingUtilities.isEventDispatchThread()) {
        Throwable r = new RuntimeException();
        int i = 0;
        for (i = 0; i < r.getStackTrace().length; i++) {
        if (!r.getStackTrace()[i].toString().startsWith("org.gephi.graph.dhns")) {
        break;
        }
        }
        System.err.println("WARNING: writeLock() on the EDT - " + r.getStackTrace()[i].toString());
        }*/
        //Logger.getLogger("").log(Level.WARNING, "{0} write lock", Thread.currentThread());
        readWriteLock.writeLock().lock();
    }

    public void writeUnlock() {
        //Logger.getLogger("").log(Level.WARNING, "{0} write unlock", Thread.currentThread());
        readWriteLock.writeLock().unlock();
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

    public void pushNodes(Graph graph, Node[] nodes) {
        if (graph == null) {
            throw new NullPointerException();
        }
        HierarchicalGraphImpl graphImpl = (HierarchicalGraphImpl) graph;
        if (graphImpl.getGraphModel() == this) {
            throw new IllegalArgumentException("The graph must be from a different Workspace");
        }
        Dhns source = (Dhns) graphImpl.getGraphModel();
        source.getDuplicateManager().duplicateNodes(this, nodes);
        graphVersion.incNodeAndEdgeVersion();
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

    public Workspace getWorkspace() {
        return workspace;
    }
}
