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
package org.gephi.desktop.filters.query;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.api.FilterModel;
import org.gephi.filters.api.Query;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
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

    private final Query query;

    public QueryNode(Query query) {
        super(new QueryChildren(query));
        this.query = query;
        //setName(query.getName());
        setIconBaseWithExtension("org/gephi/desktop/filters/query/resources/query.png");
    }

    @Override
    public String getHtmlDisplayName() {
        return isSelected() ? "<b>" + (query.getName()) + "</b>" : query.getName();
    }

    private boolean isSelected() {
        FilterController fc = Lookup.getDefault().lookup(FilterController.class);
        FilterModel fm = fc.getModel();
        return (fm.isFiltering() || fm.isSelecting()) && fc.getModel().getCurrentQuery() == query;
        //return FiltersTopComponent.findInstance().getUiModel().getSelectedRoot() == query;
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
        //System.out.println("getActions " + context);
        return new Action[]{new RemoveAction(), new RenameAction(), new SaveAction(), new DuplicateAction()};
    }

    public Query getQuery() {
        return query;
    }

    private class RemoveAction extends AbstractAction {

        public RemoveAction() {
            super(NbBundle.getMessage(QueryNode.class, "QueryNode.actions.remove"));
        }

        @Override
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

        @Override
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

    private class SaveAction extends AbstractAction {

        public SaveAction() {
            super(NbBundle.getMessage(QueryNode.class, "QueryNode.actions.save"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
            FilterLibrary filterLibrary = filterController.getModel().getLibrary();
            if (query.getParent() == null) {
                filterLibrary.saveQuery(query);
            } else {
                filterLibrary.saveQuery(query.getParent());
            }
        }
    }

    private class DuplicateAction extends AbstractAction {

        public DuplicateAction() {
            super(NbBundle.getMessage(QueryNode.class, "QueryNode.actions.duplicate"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
            Query ancestor = query;
            while (ancestor.getParent() != null) {
                ancestor = ancestor.getParent();
            }
            duplicateQuery(filterController, null, ancestor);
        }

        private void duplicateQuery(FilterController filterController, Query parent, Query child) {
            Filter filter = child.getFilter();
            FilterBuilder builder = filterController.getModel().getLibrary().getBuilder(filter);

            Query childQuery = filterController.createQuery(builder);

            Filter filterCopy = childQuery.getFilter();
            FilterProperty[] filterProperties = filter.getProperties();
            FilterProperty[] filterCopyProperties = filterCopy.getProperties();
            if (filterProperties != null && filterCopyProperties != null) {
                for (int i = 0; i < filterProperties.length; i++) {
                    filterCopyProperties[i].setValue(filterProperties[i].getValue());
                }
            }

            if (parent == null) {
                filterController.add(childQuery);
            } else {
                filterController.setSubQuery(parent, childQuery);
            }
            if (child.getChildrenSlotsCount() > 0) {
                for (Query grandChild : child.getChildren()) {
                    duplicateQuery(filterController, childQuery, grandChild);
                }
            }
        }
    }
}
