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
package org.gephi.graph.dhns;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeRowFactory;
import org.gephi.graph.api.ClusteredDirectedGraph;
import org.gephi.graph.api.ClusteredMixedGraph;
import org.gephi.graph.api.ClusteredUndirectedGraph;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.DynamicGraph;
import org.gephi.graph.api.FilteredGraph;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.HierarchicalDirectedGraph;
import org.gephi.graph.api.HierarchicalMixedGraph;
import org.gephi.graph.api.HierarchicalUndirectedGraph;
import org.gephi.graph.api.MixedGraph;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.core.GraphFactoryImpl;
import org.gephi.graph.dhns.core.IDGen;
import org.gephi.graph.dhns.graph.ClusteredDirectedGraphImpl;
import org.gephi.graph.dhns.graph.ClusteredGraphImpl;
import org.gephi.graph.dhns.graph.ClusteredMixedGraphImpl;
import org.gephi.graph.dhns.graph.ClusteredUndirectedGraphImpl;
import org.gephi.graph.dhns.graph.DynamicGraphImpl;
import org.openide.util.Lookup;

/**
 * Singleton which manages the graph access.
 *
 * @author Mathieu Bastian
 */
public class DhnsGraphController implements GraphController {

    protected IDGen iDGen;
    protected GraphFactoryImpl factory;
    protected Dhns dhns;
    private AttributeRowFactory attributesFactory;
    private Executor eventBus;

    public DhnsGraphController() {
        iDGen = new IDGen();
        dhns = new Dhns(this);

        if (Lookup.getDefault().lookup(AttributeController.class) != null) {
            attributesFactory = Lookup.getDefault().lookup(AttributeController.class).rowFactory();
        }

        factory = new GraphFactoryImpl(iDGen, attributesFactory);
        eventBus = new ThreadPoolExecutor(0, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {

            public Thread newThread(Runnable r) {
                return new Thread(r, "DHNS Event Bus");
            }
        });
    }

    public Dhns newDhns() {
        return new Dhns(this);
    }

    public Dhns getMainDhns() {
        return dhns;
    }

    public Executor getEventBus() {
        return eventBus;
    }

    public GraphFactoryImpl factory() {
        return factory;
    }

    public IDGen getIDGen() {
        return iDGen;
    }

    public DirectedGraph getDirectedGraph() {
        return new ClusteredDirectedGraphImpl(dhns, false, false);
    }

    public DirectedGraph getVisibleDirectedGraph() {
        return new ClusteredDirectedGraphImpl(dhns, true, false);
    }

    public UndirectedGraph getUndirectedGraph() {
        return new ClusteredUndirectedGraphImpl(dhns, false, false);
    }

    public UndirectedGraph getVisibleUndirectedGraph() {
        return new ClusteredUndirectedGraphImpl(dhns, true, false);
    }

    public MixedGraph getMixedGraph() {
        return new ClusteredMixedGraphImpl(dhns, false, false);
    }

    public MixedGraph getVisibleMixedGraph() {
        return new ClusteredMixedGraphImpl(dhns, true, false);
    }

    public ClusteredDirectedGraph getClusteredDirectedGraph() {
        return new ClusteredDirectedGraphImpl(dhns, false, true);
    }

    public ClusteredDirectedGraph getVisibleClusteredDirectedGraph() {
        return new ClusteredDirectedGraphImpl(dhns, true, true);
    }

    public ClusteredUndirectedGraph getClusteredUndirectedGraph() {
        return new ClusteredUndirectedGraphImpl(dhns, false, true);
    }

    public ClusteredUndirectedGraph getVisibleClusteredUndirectedGraph() {
        return new ClusteredUndirectedGraphImpl(dhns, true, true);
    }

    public ClusteredMixedGraph getClusteredMixedGraph() {
        return new ClusteredMixedGraphImpl(dhns, false, true);
    }

    public ClusteredMixedGraph getVisibleClusteredMixedGraph() {
        return new ClusteredMixedGraphImpl(dhns, true, true);
    }

    public HierarchicalDirectedGraph getHierarchicalDirectedGraph() {
        return new ClusteredDirectedGraphImpl(dhns, false, false);
    }

    public HierarchicalDirectedGraph getVisibleHierarchicalDirectedGraph() {
        return new ClusteredDirectedGraphImpl(dhns, true, false);
    }

    public HierarchicalMixedGraph getHierarchicalMixedGraph() {
        return new ClusteredMixedGraphImpl(dhns, false, false);
    }

    public HierarchicalMixedGraph getVisibleHierarchicalMixedGraph() {
        return new ClusteredMixedGraphImpl(dhns, true, false);
    }

    public HierarchicalUndirectedGraph getHierarchicalUndirectedGraph() {
        return new ClusteredUndirectedGraphImpl(dhns, false, false);
    }

    public HierarchicalUndirectedGraph getVisibleHierarchicalUndirectedGraph() {
        return new ClusteredUndirectedGraphImpl(dhns, true, false);
    }

    public FilteredGraph getFilteredGraph(Graph graph) {
        ClusteredGraphImpl graphImpl = (ClusteredGraphImpl) graph;
        return graphImpl;
    }

    public DynamicGraph getDynamicGraph(Graph graph) {
        return new DynamicGraphImpl((ClusteredGraphImpl) graph);
    }
}
