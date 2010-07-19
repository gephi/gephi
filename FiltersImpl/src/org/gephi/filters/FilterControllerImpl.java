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
package org.gephi.filters;

import java.beans.PropertyEditorManager;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.FilterModel;
import org.gephi.filters.api.PropertyExecutor;
import org.gephi.filters.api.Query;
import org.gephi.filters.api.Range;
import org.gephi.filters.FilterThread.PropertyModifier;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.filters.spi.Operator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.utils.progress.ProgressTicketProvider;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceInformation;
import org.gephi.project.api.WorkspaceListener;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProviders({
    @ServiceProvider(service = FilterController.class),
    @ServiceProvider(service = PropertyExecutor.class)})
public class FilterControllerImpl implements FilterController, PropertyExecutor {

    private FilterModelImpl model;

    public FilterControllerImpl() {
        //Register range editor
        PropertyEditorManager.registerEditor(Range.class, RangePropertyEditor.class);
        PropertyEditorManager.registerEditor(AttributeColumn.class, AttributeColumnPropertyEditor.class);

        //Model management
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
                workspace.add(new FilterModelImpl());
            }

            public void select(Workspace workspace) {
                model = (FilterModelImpl) workspace.getLookup().lookup(FilterModel.class);
                if (model == null) {
                    model = new FilterModelImpl();
                    workspace.add(model);
                }
            }

            public void unselect(Workspace workspace) {
            }

            public void close(Workspace workspace) {
            }

            public void disable() {
                GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
                if (model.getCurrentResult() != null && graphModel != null) {
                    graphModel.destroyView(model.getCurrentResult());
                    model.setCurrentResult(null);
                }
                model = null;
            }
        });
        if (pc.getCurrentWorkspace() != null) {
            Workspace workspace = pc.getCurrentWorkspace();
            model = (FilterModelImpl) workspace.getLookup().lookup(FilterModel.class);
            if (model == null) {
                model = new FilterModelImpl();
                workspace.add(model);
            }
        }
    }

    public Query createQuery(Filter filter) {
        if (filter instanceof Operator) {
            return new OperatorQueryImpl((Operator) filter);
        }
        return new FilterQueryImpl(filter);
    }

    public void add(Query query) {
        query = ((AbstractQueryImpl) query).getRoot();
        if (!model.hasQuery(query)) {
            model.addFirst(query);
        }
    }

    public void remove(Query query) {
        query = ((AbstractQueryImpl) query).getRoot();
        model.remove(query);
    }

    public void rename(Query query, String name) {
        model.rename(query, name);
    }

    public void setSubQuery(Query query, Query subQuery) {
        model.setSubQuery(query, subQuery);
    }

    public void removeSubQuery(Query query, Query parent) {
        model.removeSubQuery(query, parent);
    }

    public void filterVisible(Query query) {
        if (query != null && model.getCurrentQuery() == query && model.isFiltering()) {
            return;
        }
        model.setFiltering(true);
        model.setCurrentQuery(query);

        if (model.getFilterThread() != null) {
            model.getFilterThread().setRunning(false);
            model.setFilterThread(null);
        }
        if (query != null) {
            FilterThread filterThread = new FilterThread(model);
            model.setFilterThread(filterThread);
            filterThread.setRootQuery((AbstractQueryImpl) query);
            filterThread.start();
        } else {
            GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
            graphModel.setVisibleView(null);
            if (model.getCurrentResult() != null) {
                graphModel.destroyView(model.getCurrentResult());
                model.setCurrentResult(null);
            }
        }
    }

    public GraphView filter(Query query) {
        FilterProcessor processor = new FilterProcessor();
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        Graph result = processor.process((AbstractQueryImpl) query, graphModel);
        return result.getView();
    }

    public void selectVisible(Query query) {
        if (query != null && model.getCurrentQuery() == query && !model.isFiltering()) {
            return;
        }
        model.setFiltering(false);
        model.setCurrentQuery(query);

        if (model.getFilterThread() != null) {
            model.getFilterThread().setRunning(false);
            model.setFilterThread(null);
        }

        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        graphModel.setVisibleView(null);
        if (model.getCurrentResult() != null) {
            graphModel.destroyView(model.getCurrentResult());
            model.setCurrentResult(null);
        }

        if (query != null) {
            FilterThread filterThread = new FilterThread(model);
            model.setFilterThread(filterThread);
            filterThread.setRootQuery((AbstractQueryImpl) query);
            filterThread.start();
        } else {
        }
    }

    public void exportToColumn(String title, Query query) {
        Graph result;
        if (model.getCurrentQuery() == query) {
            GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
            result = graphModel.getGraphVisible();
        } else {
            FilterProcessor processor = new FilterProcessor();
            GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
            result = processor.process((AbstractQueryImpl) query, graphModel);
        }
        AttributeModel am = Lookup.getDefault().lookup(AttributeController.class).getModel();
        AttributeColumn nodeCol = am.getNodeTable().getColumn("filter_" + title);
        if (nodeCol == null) {
            nodeCol = am.getNodeTable().addColumn("filter_" + title, title, AttributeType.BOOLEAN, AttributeOrigin.COMPUTED, Boolean.FALSE);
        }
        AttributeColumn edgeCol = am.getEdgeTable().getColumn("filter_" + title);
        if (edgeCol == null) {
            edgeCol = am.getEdgeTable().addColumn("filter_" + title, title, AttributeType.BOOLEAN, AttributeOrigin.COMPUTED, Boolean.FALSE);
        }
        result.readLock();
        for (Node n : result.getNodes()) {
            n.getNodeData().getAttributes().setValue(nodeCol.getIndex(), Boolean.TRUE);
        }
        for (Edge e : result.getEdges()) {
            e.getEdgeData().getAttributes().setValue(edgeCol.getIndex(), Boolean.TRUE);
        }
        result.readUnlock();
        //StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(FilterControllerImpl.class, "FilterController.exportToColumn.status", title));
    }

    public void exportToNewWorkspace(Query query) {
        Graph result;
        if (model.getCurrentQuery() == query) {
            GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
            result = graphModel.getGraphVisible();
        } else {
            FilterProcessor processor = new FilterProcessor();
            GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
            result = processor.process((AbstractQueryImpl) query, graphModel);
        }

        final Graph graphView = result;
        new Thread(new Runnable() {

            public void run() {
                ProgressTicketProvider progressProvider = Lookup.getDefault().lookup(ProgressTicketProvider.class);
                ProgressTicket ticket = null;
                if (progressProvider != null) {
                    ticket = progressProvider.createTicket("Export to workspace", null);
                }
                Progress.start(ticket);
                ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
                Workspace newWorkspace = pc.duplicateWorkspace(pc.getCurrentWorkspace());
                GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel(newWorkspace);
                graphModel.clear();
                graphModel.pushFrom(graphView);
                Progress.finish(ticket);
                String workspaceName = newWorkspace.getLookup().lookup(WorkspaceInformation.class).getName();
                //StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(FilterControllerImpl.class, "FilterController.exportToNewWorkspace.status", workspaceName));
            }
        }, "Export filter to workspace").start();
    }

    public FilterModel getModel() {
        return model;
    }

    public void setValue(FilterProperty property, Object value, Callback callback) {
        if (model != null) {
            Query query = model.getQuery(property.getFilter());
            if (query == null) {
                callback.setValue(value);
                return;
            }
            AbstractQueryImpl rootQuery = ((AbstractQueryImpl) query).getRoot();
            FilterThread filterThread = null;
            if ((filterThread = model.getFilterThread()) != null && model.getCurrentQuery() == rootQuery) {
                //The query is currently being filtered by the thread, or finished to do it
                filterThread.addModifier(new PropertyModifier(query, property, value, callback));
                filterThread.setRootQuery(rootQuery);
            } else {
                //Update normally
                callback.setValue(value);
                model.updateParameters(query);
            }
        } else {
            callback.setValue(value);
        }
    }
}
