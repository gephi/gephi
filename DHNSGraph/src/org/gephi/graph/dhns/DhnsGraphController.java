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
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.core.IDGen;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.WorkspaceDuplicateProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * Singleton which manages the graph access.
 *
 * @author Mathieu Bastian
 */
@ServiceProviders({
    @ServiceProvider(service = GraphController.class),
    @ServiceProvider(service = WorkspaceDuplicateProvider.class, position = 1000)})
public class DhnsGraphController implements GraphController, WorkspaceDuplicateProvider {

    private static final int EVENTBUS_BOUND = 30;
    //Common architecture
    protected IDGen iDGen;
    private Executor eventBus;

    public DhnsGraphController() {
        iDGen = new IDGen();
        eventBus = new ThreadPoolExecutor(0, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(EVENTBUS_BOUND), new ThreadFactory() {

            public Thread newThread(Runnable r) {
                return new Thread(r, "DHNS Event Bus");
            }
        }, new ThreadPoolExecutor.DiscardOldestPolicy());
    }

    public Dhns newDhns(Workspace workspace) {
        Dhns dhns = new Dhns(this, workspace);
        workspace.add(dhns);
        return dhns;
    }

    public Executor getEventBus() {
        return eventBus;
    }

    public IDGen getIDGen() {
        return iDGen;
    }

    private Dhns getCurrentDhns() {
        Workspace currentWorkspace = Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace();
        if (currentWorkspace == null) {
            return null;
        }
        Dhns dhns = currentWorkspace.getLookup().lookup(Dhns.class);
        if (dhns == null) {
            dhns = newDhns(currentWorkspace);
        }
        return dhns;
    }

    public GraphModel getModel(Workspace workspace) {
        Dhns dhns = workspace.getLookup().lookup(Dhns.class);
        if (dhns == null) {
            dhns = newDhns(workspace);
        }
        return dhns;
    }

    public GraphModel getModel() {
        return getCurrentDhns();
    }

    public void duplicate(Workspace source, Workspace destination) {
        Dhns sourceModel = source.getLookup().lookup(Dhns.class);
        if (sourceModel != null) {
            Dhns destModel = destination.getLookup().lookup(Dhns.class);
            if (destModel == null) {
                destModel = newDhns(destination);
            }
            sourceModel.getDuplicateManager().duplicate(destModel);
        }
    }
}
