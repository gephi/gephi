/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.desktop.filters.query;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import javax.swing.Action;
import org.gephi.desktop.filters.library.FilterBuilderNode;
import org.gephi.desktop.filters.library.SavedQueryNode;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.spi.FilterBuilder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author Mathieu Bastian
 */
public class RootNode extends AbstractNode {

    public RootNode(Children children) {
        super(children);
        setName(NbBundle.getMessage(RootNode.class, "RootNode.name"));
        setIconBaseWithExtension("org/gephi/desktop/filters/query/resources/queries.png");
    }

    @Override
    public PasteType getDropType(Transferable t, int action, int index) {
        final Node dropNode = NodeTransfer.node(t, DnDConstants.ACTION_COPY_OR_MOVE);

        if (t.isDataFlavorSupported(FilterBuilderNode.DATA_FLAVOR)) {
            try {
                final FilterBuilder fb = (FilterBuilder) t.getTransferData(FilterBuilderNode.DATA_FLAVOR);
                return new PasteType() {

                    @Override
                    public Transferable paste() throws IOException {
                        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
                        Query f = filterController.createQuery(fb.getFilter());
                        filterController.add(f);
                        return null;
                    }
                };
            } catch (UnsupportedFlavorException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else if (dropNode != null && dropNode instanceof SavedQueryNode) {
            return new PasteType() {

                @Override
                public Transferable paste() throws IOException {
                    SavedQueryNode node = (SavedQueryNode) dropNode;
                    FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
                    filterController.add(node.getQuery());
                    return null;
                }
            };
        } else if (dropNode != null && dropNode instanceof QueryNode && ((QueryNode) dropNode).getQuery().getParent() != null) {
            return new PasteType() {

                @Override
                public Transferable paste() throws IOException {
                    QueryNode queryNode = (QueryNode) dropNode;
                    FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
                    filterController.removeSubQuery(queryNode.getQuery(), queryNode.getQuery().getParent());
                    filterController.add(queryNode.getQuery());
                    return null;
                }
            };
        }
        return null;
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[0];
    }
}
