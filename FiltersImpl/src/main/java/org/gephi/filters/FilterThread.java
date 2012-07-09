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
package org.gephi.filters;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import org.gephi.filters.api.PropertyExecutor.Callback;
import org.gephi.filters.api.Query;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.utils.progress.ProgressTicketProvider;
import org.gephi.visualization.api.VisualizationController;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class FilterThread extends Thread {

    private FilterModelImpl model;
    private AtomicReference<AbstractQueryImpl> rootQuery;
    ConcurrentHashMap<String, PropertyModifier> modifiersMap;
    private boolean running = true;
    private final Object lock = new Object();
    private final boolean filtering;

    public FilterThread(FilterModelImpl model) {
        super("Filter Thread");
        setDaemon(true);
        this.model = model;
        this.filtering = model.isFiltering();
        rootQuery = new AtomicReference<AbstractQueryImpl>();
        modifiersMap = new ConcurrentHashMap<String, PropertyModifier>();
    }

    @Override
    public void run() {

        while (running) {
            AbstractQueryImpl q;
            while ((q = rootQuery.getAndSet(null)) == null && running) {
                try {
                    synchronized (this.lock) {
                        lock.wait();
                    }
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (!running) {
                return;
            }
            Query modifiedQuery = null;
            for (Iterator<PropertyModifier> itr = modifiersMap.values().iterator(); itr.hasNext();) {
                PropertyModifier pm = itr.next();
                itr.remove();
                pm.callback.setValue(pm.value);
                modifiedQuery = pm.query;
            }
            if (modifiedQuery != null) {
                model.updateParameters(modifiedQuery);
            }

            //Progress
            ProgressTicket progressTicket = null;
            ProgressTicketProvider progressTicketProvider = Lookup.getDefault().lookup(ProgressTicketProvider.class);
            if (progressTicketProvider != null) {
                progressTicket = progressTicketProvider.createTicket("Filtering", null);
                Progress.start(progressTicket);
            }

            if (filtering) {
                filter(q);
            } else {
                select(q);
            }

            Progress.finish(progressTicket);
            /*try {
            //System.out.println("filter query " + q.getName());
            Thread.sleep(5000);
            } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            }*/
        }
        //clear map
        Query q = null;
        for (Iterator<PropertyModifier> itr = modifiersMap.values().iterator(); itr.hasNext();) {
            PropertyModifier pm = itr.next();
            pm.callback.setValue(pm.value);
            q = pm.query;
        }
        modifiersMap.clear();
        if (q != null) {
            model.updateParameters(q);
        }
    }

    private void filter(AbstractQueryImpl query) {
        FilterProcessor processor = new FilterProcessor();
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        Graph result = processor.process((AbstractQueryImpl) query, graphModel);
//        System.out.println("#Nodes: " + result.getNodeCount());
//        System.out.println("#Edges: " + result.getEdgeCount());
        if (running) {
            GraphView view = result.getView();
            graphModel.setVisibleView(view);
            if (model.getCurrentResult() != null) {
                graphModel.destroyView(model.getCurrentResult());
            }
            model.setCurrentResult(view);
        } else {
            //destroy view
            graphModel.destroyView(result.getView());
        }
    }

    private void select(AbstractQueryImpl query) {
        FilterProcessor processor = new FilterProcessor();
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        Graph result = processor.process((AbstractQueryImpl) query, graphModel);
//        System.out.println("#Nodes: " + result.getNodeCount());
//        System.out.println("#Edges: " + result.getEdgeCount());
        if (running) {
            VisualizationController visController = Lookup.getDefault().lookup(VisualizationController.class);
            if (visController != null) {
                visController.selectNodes(result.getNodes().toArray());
                visController.selectEdges(result.getEdges().toArray());
            }
            GraphView view = result.getView();
            model.setCurrentResult(view);
        } else {
            //destroy view
            graphModel.destroyView(result.getView());
        }
    }

    public void setRootQuery(AbstractQueryImpl rootQuery) {
        this.rootQuery.set(rootQuery);
        synchronized (this.lock) {
            lock.notify();
        }
    }

    public AbstractQueryImpl getRootQuery() {
        return rootQuery.get();
    }

    public void setRunning(boolean running) {
        this.running = running;
        synchronized (this.lock) {
            lock.notify();
        }
    }

    public void addModifier(PropertyModifier modifier) {
        modifiersMap.put(modifier.property.getName(), modifier);
    }

    protected static class PropertyModifier {

        protected final Object value;
        protected final Callback callback;
        protected final FilterProperty property;
        protected final Query query;

        public PropertyModifier(Query query, FilterProperty property, Object value, Callback callback) {
            this.query = query;
            this.property = property;
            this.value = value;
            this.callback = callback;
        }
    }
}
