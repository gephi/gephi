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
import org.gephi.project.api.ProjectController;
import org.gephi.workspace.api.Workspace;
import org.openide.util.Lookup;

/**
 * Singleton which manages the graph access.
 *
 * @author Mathieu Bastian
 */
public class DhnsGraphController implements GraphController {

    protected IDGen iDGen;
    protected GraphFactoryImpl factory;
    private AttributeRowFactory attributesFactory;
    private Executor eventBus;
    private GraphWorkspaceDataProvider workspaceDataProvider;

    public DhnsGraphController() {
        iDGen = new IDGen();

        if (Lookup.getDefault().lookup(AttributeController.class) != null) {
            attributesFactory = Lookup.getDefault().lookup(AttributeController.class).rowFactory();
        }

        factory = new GraphFactoryImpl(iDGen, attributesFactory);
        eventBus = new ThreadPoolExecutor(0, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {

            public Thread newThread(Runnable r) {
                return new Thread(r, "DHNS Event Bus");
            }
        });

        workspaceDataProvider = Lookup.getDefault().lookup(GraphWorkspaceDataProvider.class);
    }

    public Dhns newDhns(Workspace workspace) {
        Dhns dhns = new Dhns(this);
        workspace.getWorkspaceData().setData(workspaceDataProvider.getWorkspaceDataKey(), dhns);
        dhns.setCentralDynamicGraph(new DynamicGraphImpl(dhns, getHierarchicalDirectedGraph()));
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

    private Dhns getCurrentDhns() {
        Workspace currentWorkspace = Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace();
        if (currentWorkspace == null) {
            return null;
        }
        Dhns dhns = currentWorkspace.getWorkspaceData().getData(workspaceDataProvider.getWorkspaceDataKey());
        if (dhns == null) {
            dhns = newDhns(currentWorkspace);
        }
        return dhns;
    }

    public DirectedGraph getDirectedGraph() {
        Dhns dhns = getCurrentDhns();
        if (dhns == null) {
            return null;
        }
        return new ClusteredDirectedGraphImpl(dhns, false, false);
    }

    public DirectedGraph getVisibleDirectedGraph() {
        Dhns dhns = getCurrentDhns();
        if (dhns == null) {
            return null;
        }
        return new ClusteredDirectedGraphImpl(dhns, true, false);
    }

    public UndirectedGraph getUndirectedGraph() {
        Dhns dhns = getCurrentDhns();
        if (dhns == null) {
            return null;
        }
        return new ClusteredUndirectedGraphImpl(dhns, false, false);
    }

    public UndirectedGraph getVisibleUndirectedGraph() {
        Dhns dhns = getCurrentDhns();
        if (dhns == null) {
            return null;
        }
        return new ClusteredUndirectedGraphImpl(dhns, true, false);
    }

    public MixedGraph getMixedGraph() {
        Dhns dhns = getCurrentDhns();
        if (dhns == null) {
            return null;
        }
        return new ClusteredMixedGraphImpl(dhns, false, false);
    }

    public MixedGraph getVisibleMixedGraph() {
        Dhns dhns = getCurrentDhns();
        if (dhns == null) {
            return null;
        }
        return new ClusteredMixedGraphImpl(dhns, true, false);
    }

    public ClusteredDirectedGraph getClusteredDirectedGraph() {
        Dhns dhns = getCurrentDhns();
        if (dhns == null) {
            return null;
        }
        return new ClusteredDirectedGraphImpl(dhns, false, true);
    }

    public ClusteredDirectedGraph getVisibleClusteredDirectedGraph() {
        Dhns dhns = getCurrentDhns();
        if (dhns == null) {
            return null;
        }
        return new ClusteredDirectedGraphImpl(dhns, true, true);
    }

    public ClusteredUndirectedGraph getClusteredUndirectedGraph() {
        Dhns dhns = getCurrentDhns();
        if (dhns == null) {
            return null;
        }
        return new ClusteredUndirectedGraphImpl(dhns, false, true);
    }

    public ClusteredUndirectedGraph getVisibleClusteredUndirectedGraph() {
        Dhns dhns = getCurrentDhns();
        if (dhns == null) {
            return null;
        }
        return new ClusteredUndirectedGraphImpl(dhns, true, true);
    }

    public ClusteredMixedGraph getClusteredMixedGraph() {
        Dhns dhns = getCurrentDhns();
        if (dhns == null) {
            return null;
        }
        return new ClusteredMixedGraphImpl(dhns, false, true);
    }

    public ClusteredMixedGraph getVisibleClusteredMixedGraph() {
        Dhns dhns = getCurrentDhns();
        if (dhns == null) {
            return null;
        }
        return new ClusteredMixedGraphImpl(dhns, true, true);
    }

    public HierarchicalDirectedGraph getHierarchicalDirectedGraph() {
        Dhns dhns = getCurrentDhns();
        if (dhns == null) {
            return null;
        }
        return new ClusteredDirectedGraphImpl(dhns, false, false);
    }

    public HierarchicalDirectedGraph getVisibleHierarchicalDirectedGraph() {
        Dhns dhns = getCurrentDhns();
        if (dhns == null) {
            return null;
        }
        return new ClusteredDirectedGraphImpl(dhns, true, false);
    }

    public HierarchicalMixedGraph getHierarchicalMixedGraph() {
        Dhns dhns = getCurrentDhns();
        if (dhns == null) {
            return null;
        }
        return new ClusteredMixedGraphImpl(dhns, false, false);
    }

    public HierarchicalMixedGraph getVisibleHierarchicalMixedGraph() {
        Dhns dhns = getCurrentDhns();
        if (dhns == null) {
            return null;
        }
        return new ClusteredMixedGraphImpl(dhns, true, false);
    }

    public HierarchicalUndirectedGraph getHierarchicalUndirectedGraph() {
        Dhns dhns = getCurrentDhns();
        if (dhns == null) {
            return null;
        }
        return new ClusteredUndirectedGraphImpl(dhns, false, false);
    }

    public HierarchicalUndirectedGraph getVisibleHierarchicalUndirectedGraph() {
        Dhns dhns = getCurrentDhns();
        if (dhns == null) {
            return null;
        }
        return new ClusteredUndirectedGraphImpl(dhns, true, false);
    }

    public <T extends Graph> FilteredGraph<T> getFilteredGraph(T graph) {
        Dhns dhns = getCurrentDhns();
        if (dhns == null) {
            return null;
        }
        return copyGraph((ClusteredGraphImpl) graph);
    }

    public DynamicGraph getDynamicGraph(Graph graph) {
        Dhns dhns = getCurrentDhns();
        if (dhns == null) {
            return null;
        }
        return new DynamicGraphImpl(dhns, copyGraph((ClusteredGraphImpl) graph));
    }

    public DynamicGraph getCentralDynamicGraph() {
        Dhns dhns = getCurrentDhns();
        if (dhns == null) {
            return null;
        }
        return dhns.getCentralDynamicGraph();
    }

    public ClusteredGraphImpl copyGraph(ClusteredGraphImpl graph) {
        ClusteredGraphImpl absGraph = (ClusteredGraphImpl) graph;
        ClusteredGraphImpl copy = absGraph.copy(absGraph);
        copy.setNodeProposition(absGraph.getNodeProposition().copy());
        copy.setEdgeProposition(absGraph.getEdgeProposition().copy());
        copy.setAllowMultilevel(absGraph.isAllowMultilevel());
        copy.setView(absGraph.getView());
        return copy;
    }
}
