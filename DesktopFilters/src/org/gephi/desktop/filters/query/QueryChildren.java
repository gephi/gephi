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

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.Action;
import org.gephi.desktop.filters.library.FilterBuilderNode;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.spi.FilterBuilder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author Mathieu Bastian
 */
public class QueryChildren extends Children.Array {

    private Query query;
    private Query[] topQuery;

    public QueryChildren(Query query) {
        this.query = query;
    }

    public QueryChildren(Query[] topQuery) {   //Only for root node
        if (topQuery.length > 0) {
            this.topQuery = topQuery;
        }
    }

    @Override
    protected Collection<Node> initCollection() {
        Collection<Node> nodesChildren = new ArrayList<Node>();
        if (query == null && topQuery == null) {
            nodesChildren.add(new HelpNode());
        } else {
            Query[] children = topQuery != null ? topQuery : query.getChildren();
            boolean hasParameters = query == null ? false : query.getParametersCount() > 0;
            int slots = topQuery != null ? topQuery.length : query.getChildrenSlotsCount();

            if (slots == Integer.MAX_VALUE) {
                slots = children != null ? children.length + 1 : 1;
            }

            if (hasParameters) {
                nodesChildren.add(new ParameterNode(query));
            }
            for (int i = 0; i < slots; i++) {
                if (children != null && i < children.length) {
                    nodesChildren.add(new QueryNode(children[i]));
                } else {
                    nodesChildren.add(new SlotNode(query));
                }
            }
        }
        return nodesChildren;
    }

    private static class HelpNode extends AbstractNode {

        public HelpNode() {
            super(Children.LEAF);
            setIconBaseWithExtension("filtersui/desktop/query/resources/drop.png");
        }

        @Override
        public String getHtmlDisplayName() {
            return NbBundle.getMessage(QueryChildren.class, "HelpNode.name");
        }

        @Override
        public PasteType getDropType(Transferable t, int action, int index) {
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
            }
            return null;
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[0];
        }
    }
}
