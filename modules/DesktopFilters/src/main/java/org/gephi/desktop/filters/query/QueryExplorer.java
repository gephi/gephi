/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Sébastien Heymann <sebastien.heymann@gephi.org>
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
package org.gephi.desktop.filters.query;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreeSelectionModel;
import org.gephi.desktop.filters.FilterUIModel;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.FilterModel;
import org.gephi.filters.api.Query;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class QueryExplorer extends BeanTreeView implements PropertyChangeListener, ChangeListener {

    private ExplorerManager manager;
    private FilterModel model;
    private FilterUIModel uiModel;
    private FilterController filterController;
    //state
    private boolean listenSelectedNodes = false;

    public QueryExplorer() {
        setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    }

    public void unsetup() {
        if (model != null) {
            model.removeChangeListener(this);
            model = null;
        }
    }

    public void setup(final ExplorerManager manager, final FilterModel model, FilterUIModel uiModel) {
        this.manager = manager;
        this.model = model;
        this.uiModel = uiModel;
        this.filterController = Lookup.getDefault().lookup(FilterController.class);

        if (model != null) {
            model.addChangeListener(this);
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    manager.setRootContext(new RootNode(new QueryChildren(model.getQueries())));
                }
            });
        } else {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    manager.setRootContext(new AbstractNode(Children.LEAF) {

                        @Override
                        public Action[] getActions(boolean context) {
                            return new Action[0];
                        }
                    });
                }
            });
        }
        updateEnabled(model != null);

        if (!listenSelectedNodes) {
            manager.addPropertyChangeListener(this);
            listenSelectedNodes = true;
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
            if (uiModel == null) {
                return;
            }
            Node[] nodeArray = (Node[]) evt.getNewValue();
            if (nodeArray.length > 0) {
                Node node = ((Node[]) evt.getNewValue())[0];
                if (node instanceof RootNode) {
                    uiModel.setSelectedQuery(null);
                    filterController.setCurrentQuery(null);
                    return;
                }
                while (!(node instanceof QueryNode)) {
                    node = node.getParentNode();
                    if (node.getParentNode() == null) {
                        uiModel.setSelectedQuery(null);
                        filterController.setCurrentQuery(null);
                        return;
                    }
                }
                QueryNode queryNode = (QueryNode) node;
                final Query query = queryNode.getQuery();
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        uiModel.setSelectedQuery(query);
                        model.removeChangeListener(QueryExplorer.this);
                        filterController.setCurrentQuery(uiModel.getSelectedRoot());
                        model.addChangeListener(QueryExplorer.this);
                    }
                }).start();
            }
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        //System.out.println("model updated");
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                //uiModel.setSelectedQuery(model.getCurrentQuery());
                saveExpandStatus(QueryExplorer.this.manager.getRootContext());
                QueryExplorer.this.manager.setRootContext(new RootNode(new QueryChildren(QueryExplorer.this.model.getQueries())));
                loadExpandStatus(QueryExplorer.this.manager.getRootContext());
            }
        });
    }

    private void updateEnabled(final boolean enabled) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                setRootVisible(enabled);
                setEnabled(enabled);
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
            if (uiModel.isExpanded(queryNode.getQuery())) {
                expandNode(queryNode);
            }
            Node firstChild = queryNode.getChildren().getNodeAt(0);
            if (firstChild != null && firstChild instanceof ParameterNode) {
                if (uiModel.isParametersExpanded(queryNode.getQuery())) {
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
            uiModel.setExpand(queryNode.getQuery(), isExpanded(queryNode), parameterExpanded);
            for (Node n : queryNode.getChildren().getNodes()) {
                saveExpandStatus(n);
            }
        }
    }
}
