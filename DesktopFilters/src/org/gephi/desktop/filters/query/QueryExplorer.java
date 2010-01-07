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
package org.gephi.desktop.filters.query;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreeSelectionModel;
import org.gephi.desktop.filters.FilterUIModel;
import org.gephi.filters.api.FilterModel;
import org.gephi.filters.api.Query;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;

/**
 *
 * @author Mathieu Bastian
 */
public class QueryExplorer extends BeanTreeView {

    private ExplorerManager manager;
    private FilterModel model;
    private FilterUIModel uiModel;

    public QueryExplorer() {
        setRootVisible(true);
        setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

    }

    public void setup(ExplorerManager manager, FilterModel model, FilterUIModel filterUIModel) {
        this.manager = manager;
        this.model = model;
        this.uiModel = filterUIModel;
        manager.setRootContext(new RootNode(new QueryChildren(model.getQueries())));

        model.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                System.out.println("model updated");
                saveExpandStatus(QueryExplorer.this.manager.getRootContext());
                QueryExplorer.this.manager.setRootContext(new RootNode(new QueryChildren(QueryExplorer.this.model.getQueries())));
                loadExpandStatus(QueryExplorer.this.manager.getRootContext());
            }
        });

        manager.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
                    Node[] nodeArray = (Node[]) evt.getNewValue();
                    if (nodeArray.length > 0) {
                        Node node = ((Node[]) evt.getNewValue())[0];
                        if (node instanceof RootNode) {
                            uiModel.setSelectedFilter(null);
                            return;
                        }
                        while (!(node instanceof QueryNode)) {
                            node = node.getParentNode();
                            if (node.getParentNode() == null) {
                                uiModel.setSelectedFilter(null);
                                return;
                            }
                        }
                        QueryNode functionNode = (QueryNode) node;
                        Query func = functionNode.qetQuery();
                        uiModel.setSelectedFilter(func.getFilter());
                    }
                }
            }
        });
    }

    private void loadExpandStatus(Node node) {
        if (node instanceof RootNode) {
            RootNode rootNode = (RootNode) node;
            for (Node n : rootNode.getChildren().getNodes()) {
                loadExpandStatus(n);
            }
        } else if (node instanceof QueryNode) {
            QueryNode queryNode = (QueryNode) node;
            if (uiModel.isExpanded(queryNode.qetQuery())) {
                expandNode(queryNode);
            }
            Node firstChild = queryNode.getChildren().getNodeAt(0);
            if (firstChild != null && firstChild instanceof ParameterNode) {
                if (uiModel.isParametersExpanded(queryNode.qetQuery())) {
                    expandNode(firstChild);
                }
            }
            for (Node n : queryNode.getChildren().getNodes()) {
                loadExpandStatus(n);
            }
        }
    }

    private void saveExpandStatus(Node node) {
        if (node instanceof RootNode) {
            RootNode rootNode = (RootNode) node;
            for (Node n : rootNode.getChildren().getNodes()) {
                saveExpandStatus(n);
            }
        } else if (node instanceof QueryNode) {
            QueryNode queryNode = (QueryNode) node;
            Node firstChild = queryNode.getChildren().getNodeAt(0);
            boolean parameterExpanded = false;
            if (firstChild != null && firstChild instanceof ParameterNode) {
                parameterExpanded = isExpanded(firstChild);
            }
            uiModel.setExpand(queryNode.qetQuery(), isExpanded(queryNode), parameterExpanded);
            for (Node n : queryNode.getChildren().getNodes()) {
                saveExpandStatus(n);
            }
        }
    }
}
