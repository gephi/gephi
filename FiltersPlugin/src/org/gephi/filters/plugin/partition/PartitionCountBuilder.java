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
import java.util.List;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeUtils;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.api.Range;
import org.gephi.filters.plugin.RangeFilter;
import org.gephi.filters.plugin.graph.RangeUI;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.CategoryBuilder;
import org.gephi.filters.spi.EdgeFilter;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.filters.spi.NodeFilter;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
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
 * @author Mathieu Bastian
 */
@ServiceProvider(service = CategoryBuilder.class)
public class PartitionCountBuilder implements CategoryBuilder {

    private final static Category PARTITION_COUNT = new Category(
            NbBundle.getMessage(PartitionBuilder.class, "PartitionCountBuilder.name"),
            null,
            FilterLibrary.ATTRIBUTES);

    public Category getCategory() {
        return PARTITION_COUNT;
    }

    public FilterBuilder[] getBuilders() {
        List<FilterBuilder> builders = new ArrayList<FilterBuilder>();
        PartitionController pc = Lookup.getDefault().lookup(PartitionController.class);
        if (pc.getModel() != null) {
            pc.refreshPartitions();
            NodePartition[] nodePartitions = pc.getModel().getNodePartitions();
            EdgePartition[] edgePartitions = pc.getModel().getEdgePartitions();
            for (NodePartition np : nodePartitions) {
                PartitionCountFilterBuilder builder = new PartitionCountFilterBuilder(np.getColumn(), np);
                builders.add(builder);
            }
            for (EdgePartition ep : edgePartitions) {
                PartitionCountFilterBuilder builder = new PartitionCountFilterBuilder(ep.getColumn(), ep);
                builders.add(builder);
            }
        }

        return builders.toArray(new FilterBuilder[0]);
    }

    private static class PartitionCountFilterBuilder implements FilterBuilder {

        private final AttributeColumn column;
        private Partition partition;

        public PartitionCountFilterBuilder(AttributeColumn column, NodePartition partition) {
            this.column = column;
            this.partition = partition;
        }

        public PartitionCountFilterBuilder(AttributeColumn column, EdgePartition partition) {
            this.column = column;
            this.partition = partition;
        }

        public Category getCategory() {
            return PARTITION_COUNT;
        }

        public String getName() {
            return column.getTitle() + " (" + (partition instanceof NodePartition ? "Node" : "Edge") + ")";
        }

        public Icon getIcon() {
            return null;
        }

        public String getDescription() {
            return null;
        }

        public PartitionCountFilter getFilter() {
            if (AttributeUtils.getDefault().isNodeColumn(column)) {
                return new NodePartitionCountFilter(partition);
            } else {
                return new EdgePartitionCountFilter(partition);
            }
        }

        public JPanel getPanel(Filter filter) {
            RangeUI ui = Lookup.getDefault().lookup(RangeUI.class);
            if (ui != null) {
                return ui.getPanel((PartitionCountFilter) filter);
            }
            return null;
        }
    }

    public static class NodePartitionCountFilter extends PartitionCountFilter implements NodeFilter {

        public NodePartitionCountFilter(Partition partition) {
            super(partition);
        }
    }

    public static class EdgePartitionCountFilter extends PartitionCountFilter implements EdgeFilter {

        public EdgePartitionCountFilter(Partition partition) {
            super(partition);
        }
    }

    public static class PartitionCountFilter implements Filter, RangeFilter {

        private Partition partition;
        private FilterProperty[] filterProperties;
        private Integer min = 0;
        private Integer max = 0;
        private Range range = new Range(0, 0);
        //States
        private Integer[] values;

        public PartitionCountFilter(Partition partition) {
            this.partition = partition;
        }

        private void refreshRange() {
            Integer lowerBound = range.getLowerInteger();
            Integer upperBound = range.getUpperInteger();
            if ((Integer) min > lowerBound || (Integer) max < lowerBound || lowerBound.equals(upperBound)) {
                lowerBound = (Integer) min;
            }
            if ((Integer) min > upperBound || (Integer) max < upperBound || lowerBound.equals(upperBound)) {
                upperBound = (Integer) max;
            }
            range = new Range(lowerBound, upperBound);
        }

        public boolean init(Graph graph) {
            this.partition = Lookup.getDefault().lookup(PartitionController.class).buildPartition(partition.getColumn(), graph);
            return true;
        }

        public boolean evaluate(Graph graph, Node node) {
            Part p = partition.getPart(node);
            if (p != null) {
                int partCount = p.getObjects().length;
                return range.isInRange(partCount);
            }
            return false;
        }

        public boolean evaluate(Graph graph, Edge edge) {
            Part p = partition.getPart(edge);
            if (p != null) {
                int partCount = p.getObjects().length;
                return range.isInRange(partCount);
            }
            return false;
        }

        public void finish() {
            refreshRange();
        }

        public String getName() {
            return NbBundle.getMessage(PartitionBuilder.class, "PartitionCountBuilder.name");
        }

        public FilterProperty[] getProperties() {
            if (filterProperties == null) {
                filterProperties = new FilterProperty[0];
                try {
                    filterProperties = new FilterProperty[]{
                                FilterProperty.createProperty(this, AttributeColumn.class, "column"),
                                FilterProperty.createProperty(this, Range.class, "range")};
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return filterProperties;
        }

        public FilterProperty getRangeProperty() {
            return filterProperties[1];
        }

        public Object[] getValues() {
            if (values == null) {
                min = Integer.MAX_VALUE;
                max = Integer.MIN_VALUE;
                if (partition.getPartsCount() == 0) {
                    //build partition
                    GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
                    this.partition = Lookup.getDefault().lookup(PartitionController.class).buildPartition(partition.getColumn(), graphModel.getGraph());
                }
                values = new Integer[partition.getPartsCount()];
                Part[] parts = partition.getParts();
                for (int i = 0; i < parts.length; i++) {
                    int partCount = parts[i].getObjects().length;
                    min = Math.min(min, partCount);
                    max = Math.max(max, partCount);
                    values[i] = partCount;
                }
                refreshRange();
            }
            return values;
        }

        public Object getMinimum() {
            return min;
        }

        public Object getMaximum() {
            return max;
        }

        public Range getRange() {
            return range;
        }

        public void setRange(Range range) {
            this.range = range;
        }

        public AttributeColumn getColumn() {
            return partition.getColumn();
        }

        public void setColumn(AttributeColumn column) {
        }
    }
}
