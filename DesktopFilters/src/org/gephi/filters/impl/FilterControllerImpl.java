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
package org.gephi.filters.impl;

import java.beans.PropertyEditorManager;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.FilterModel;
import org.gephi.filters.api.PropertyExecutor;
import org.gephi.filters.api.Query;
import org.gephi.filters.api.Range;
import org.gephi.filters.impl.FilterThread.PropertyModifier;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.filters.spi.Operator;
import org.gephi.project.api.ProjectController;
import org.gephi.workspace.api.Workspace;
import org.gephi.workspace.api.WorkspaceListener;
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
    private FilterThread filterThread;

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
        if (!model.hasQuery(query)) {
            model.addFirst(query);
        }
    }

    public void remove(Query query) {
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

    public void filter(Query query) {
        model.setFiltering(true);
        model.setCurrentQuery(query);

        if (filterThread != null) {
            filterThread.setRunning(false);
        }
        if (query != null) {
            filterThread = new FilterThread(model);
            filterThread.setRootQuery((AbstractQueryImpl) query);
            filterThread.start();
        }

        /*FilterProcessor processor = new FilterProcessor();
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        Graph result = processor.process((AbstractQueryImpl) query, graphModel);
        System.out.println("#Nodes: " + result.getNodeCount());
        System.out.println("#Edges: " + result.getEdgeCount());
        graphModel.setVisibleView(result.getView());*/
    }

    public void select(Query query) {
        model.setFiltering(false);
        model.setCurrentQuery(query);

        System.out.println("select " + (query != null ? query.getName() : "null"));
        /*FilterProcessor processor = new FilterProcessor();
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        Graph result = processor.process((AbstractQueryImpl) query, graphModel);
        System.out.println("#Nodes: " + result.getNodeCount());
        System.out.println("#Edges: " + result.getEdgeCount());

        VizController.getInstance().getSelectionManager().selectNodes(result.getNodes().toArray());
        VizController.getInstance().getSelectionManager().selectEdges(result.getEdges().toArray());*/
    }

    public FilterModel getModel() {
        return model;
    }

    public void setValue(FilterProperty property, Object value, Callback callback) {
        Query query = model.getQuery(property.getFilter());
        AbstractQueryImpl rootQuery = ((AbstractQueryImpl) query).getRoot();
        if (filterThread != null && model.getCurrentQuery() == rootQuery) {
            //The query is currently being filtered by the thread, or finished to do it
            filterThread.addModifier(new PropertyModifier(query, property, value, callback));
            filterThread.setRootQuery(rootQuery);
        } else {
            //Update normally
            callback.setValue(value);
            model.updateParameters(query);
        }
    }
}
