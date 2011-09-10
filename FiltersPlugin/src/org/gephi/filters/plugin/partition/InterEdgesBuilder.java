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
package org.gephi.filters.plugin.partition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.plugin.partition.PartitionBuilder.PartitionFilter;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.CategoryBuilder;
import org.gephi.filters.spi.EdgeFilter;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.partition.api.EdgePartition;
import org.gephi.partition.api.NodePartition;
import org.gephi.partition.api.Part;
import org.gephi.partition.api.Partition;
import org.gephi.partition.api.PartitionController;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mbastian
 */
@ServiceProvider(service = CategoryBuilder.class)
public class InterEdgesBuilder implements CategoryBuilder {

    public final static Category INTER_EDGES = new Category(
            NbBundle.getMessage(InterEdgesBuilder.class, "InterEdgesBuilder.name"),
            null,
            FilterLibrary.ATTRIBUTES);

    public Category getCategory() {
        return INTER_EDGES;
    }

    public FilterBuilder[] getBuilders() {
        List<FilterBuilder> builders = new ArrayList<FilterBuilder>();
        PartitionController pc = Lookup.getDefault().lookup(PartitionController.class);
        if (pc.getModel() != null) {
            pc.refreshPartitions();
            NodePartition[] nodePartitions = pc.getModel().getNodePartitions();
            for (NodePartition np : nodePartitions) {
                InterEdgesFilterBuilder builder = new InterEdgesFilterBuilder(np.getColumn(), np);
                builders.add(builder);
            }
        }

        return builders.toArray(new FilterBuilder[0]);
    }

    private static class InterEdgesFilterBuilder implements FilterBuilder {

        private final AttributeColumn column;
        private Partition partition;

        public InterEdgesFilterBuilder(AttributeColumn column, NodePartition partition) {
            this.column = column;
            this.partition = partition;
        }

        public Category getCategory() {
            return INTER_EDGES;
        }

        public String getName() {
            return column.getTitle();
        }

        public Icon getIcon() {
            return null;
        }

        public String getDescription() {
            return NbBundle.getMessage(InterEdgesBuilder.class, "InterEdgesBuilder.description");
        }

        public InterEdgesFilter getFilter() {
            return new InterEdgesFilter(partition);
        }

        public JPanel getPanel(Filter filter) {
            PartitionUI ui = Lookup.getDefault().lookup(PartitionUI.class);
            if (ui != null) {
                return ui.getPanel((PartitionFilter) filter);
            }
            return null;
        }

        public void destroy(Filter filter) {
        }
    }

    public static class InterEdgesFilter extends PartitionFilter implements EdgeFilter {

        private Set partsValue;

        public InterEdgesFilter(Partition partition) {
            super(partition);
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(InterEdgesBuilder.class, "InterEdgesBuilder.name");
        }

        @Override
        public boolean init(Graph graph) {
            boolean res = super.init(graph);
            partsValue = new HashSet();
            for (Part p : parts) {
                partsValue.add(p.getValue());
            }
            return res;
        }

        @Override
        public boolean evaluate(Graph graph, Edge edge) {
            Object srcValue = edge.getSource().getAttributes().getValue(partition.getColumn().getIndex());
            Object destValue = edge.getTarget().getAttributes().getValue(partition.getColumn().getIndex());
            if (partsValue.contains(srcValue) && partsValue.contains(destValue) && !srcValue.equals(destValue)) {
                return true;
            }
            return false;
        }

        @Override
        public void finish() {
            partsValue = null;
        }
    }
}
