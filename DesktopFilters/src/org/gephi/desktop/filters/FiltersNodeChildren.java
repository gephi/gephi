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
package org.gephi.desktop.filters;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.gephi.filters.api.FilterBuilder;
import org.gephi.filters.api.FilterModel;
import org.gephi.project.api.ProjectController;
import org.gephi.workspace.api.Workspace;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class FiltersNodeChildren extends Children.Keys<FilterBuilder> {

    public FiltersNodeChildren(FilterBuilder[] filters) {
        setKeys(filters);
    }

    @Override
    protected Node[] createNodes(FilterBuilder filter) {
        return new Node[]{new FilterNode(filter)};
    }

    private static class FilterNode extends AbstractNode {

        private FilterBuilder filter;

        public FilterNode(FilterBuilder filter) {
            super(Children.LEAF);
            this.filter = filter;
        }

        @Override
        public String getDisplayName() {
            return filter.getName();
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[]{new AddFilter(filter)};
        }
    }

    private static class AddFilter extends AbstractAction {

        private FilterBuilder filter;

        public AddFilter(FilterBuilder filter) {
            this.filter = filter;
            putValue(Action.NAME, NbBundle.getMessage(FiltersLibraryExplorer.class, "FiltersLibraryExplorer_actions_AddFilter"));
        }

        public void actionPerformed(ActionEvent e) {
            Workspace workspace = Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace();
            if (workspace != null) {
                FilterModel filterModel = workspace.getWorkspaceData().getData(Lookup.getDefault().lookup(FiltersWorkspaceDataProvider.class).getWorkspaceDataKey());
                filterModel.addFilter(filter.getFilter());
            }
        }
    }
}
