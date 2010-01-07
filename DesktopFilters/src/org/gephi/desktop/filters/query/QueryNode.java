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

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.AbstractNode;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class QueryNode extends AbstractNode {

    private Query query;

    public QueryNode(Query query) {
        super(new QueryChildren(query));
        this.query = query;
        setName(query.getName());
        setIconBaseWithExtension("org/gephi/desktop/filters/query/resources/query.png");
    }

    /*@Override
    public PasteType getDropType(final Transferable t, int action, int index) {
    final Node dropNode = NodeTransfer.node(t, DnDConstants.ACTION_COPY_OR_MOVE);
    if (dropNode != null && dropNode instanceof QueryNode) {
    return new PasteType() {

    @Override
    public Transferable paste() throws IOException {
    return null;
    }
    };
    } else if (t.isDataFlavorSupported(FilterBuilderNode.DATA_FLAVOR)) {
    return new PasteType() {

    @Override
    public Transferable paste() throws IOException {
    try {
    FilterBuilder builder = (FilterBuilder) t.getTransferData(FilterBuilderNode.DATA_FLAVOR);
    FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
    } catch (UnsupportedFlavorException ex) {
    Exceptions.printStackTrace(ex);
    }

    return null;
    }
    };
    }
    return null;

    }*/
    @Override
    public Action[] getActions(boolean context) {
        System.out.println("getActions " + context);
        return new Action[]{new RemoveAction(), new RenameAction()};
    }

    public Query qetQuery() {
        return query;
    }

    private class RemoveAction extends AbstractAction {

        public RemoveAction() {
            super(NbBundle.getMessage(QueryNode.class, "QueryNode.actions.remove"));
        }

        public void actionPerformed(ActionEvent e) {
            FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
            if (query.getParent() == null) {
                filterController.remove(query);
            } else {
                filterController.removeSubQuery(query, query.getParent());
            }
        }
    }

    private class RenameAction extends AbstractAction {

        public RenameAction() {
            super(NbBundle.getMessage(QueryNode.class, "QueryNode.actions.rename"));
        }

        public void actionPerformed(ActionEvent e) {
            FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
            NotifyDescriptor.InputLine question = new NotifyDescriptor.InputLine(
                    NbBundle.getMessage(QueryNode.class, "QueryNode.actions.rename.text"),
                    NbBundle.getMessage(QueryNode.class, "QueryNode.actions.rename.title"));
            question.setInputText(query.getName());
            if (DialogDisplayer.getDefault().notify(question) == NotifyDescriptor.OK_OPTION) {
                String input = question.getInputText();
                if (input != null && !input.isEmpty()) {
                    filterController.rename(query, input);
                }
            }
        }
    }
}
