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
import java.util.Arrays;
import javax.swing.Action;
import org.gephi.desktop.filters.library.FilterBuilderNode;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.spi.FilterBuilder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author Mathieu Bastian
 */
public class SlotNode extends AbstractNode {

    private Query parent;

    public SlotNode(Query parent) {
        super(Children.LEAF);
        this.parent = parent;
        setIconBaseWithExtension("org/gephi/desktop/filters/query/resources/drop.png");
    }

    @Override
    public String getHtmlDisplayName() {
        return NbBundle.getMessage(SlotNode.class, "SlotNode.name");
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[0];
    }

    @Override
    public PasteType getDropType(final Transferable t, int action, int index) {
        final Node dropNode = NodeTransfer.node(t, DnDConstants.ACTION_COPY_OR_MOVE);
        if (dropNode != null && dropNode instanceof QueryNode) {
            Query q = ((QueryNode) dropNode).getQuery();
            if (!Arrays.asList(q.getDescendantsAndSelf()).contains(parent)) { //Check if not parent
                return new PasteType() {

                    @Override
                    public Transferable paste() throws IOException {
                        QueryNode queryNode = (QueryNode) dropNode;
                        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
                        filterController.setSubQuery(parent, queryNode.getQuery());
                        return null;
                    }
                };
            }
        } else if (t.isDataFlavorSupported(FilterBuilderNode.DATA_FLAVOR)) {
            return new PasteType() {

                @Override
                public Transferable paste() throws IOException {
                    try {
                        FilterBuilder builder = (FilterBuilder) t.getTransferData(FilterBuilderNode.DATA_FLAVOR);
                        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
                        Query query = filterController.createQuery(builder.getFilter());
                        filterController.setSubQuery(parent, query);
                    } catch (UnsupportedFlavorException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                    return null;
                }
            };
        }
        return null;
    }
}
