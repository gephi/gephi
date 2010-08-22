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
import java.util.Arrays;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeUtils;
import org.gephi.filters.api.FilterLibrary;
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
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = CategoryBuilder.class)
public class PartitionBuilder implements CategoryBuilder {

    private final static Category PARTITION = new Category(
            NbBundle.getMessage(PartitionBuilder.class, "PartitionBuilder.name"),
            null,
            FilterLibrary.ATTRIBUTES);

    public Category getCategory() {
        return PARTITION;
    }

    public FilterBuilder[] getBuilders() {
        List<FilterBuilder> builders = new ArrayList<FilterBuilder>();
        PartitionController pc = Lookup.getDefault().lookup(PartitionController.class);
        if (pc.getModel() != null) {
            pc.refreshPartitions();
            NodePartition[] nodePartitions = pc.getModel().getNodePartitions();
            EdgePartition[] edgePartitions = pc.getModel().getEdgePartitions();
            for (NodePartition np : nodePartitions) {
                PartitionFilterBuilder builder = new PartitionFilterBuilder(np.getColumn(), np);
                builders.add(builder);
            }
            for (EdgePartition ep : edgePartitions) {
                PartitionFilterBuilder builder = new PartitionFilterBuilder(ep.getColumn(), ep);
                builders.add(builder);
            }
        }

        return builders.toArray(new FilterBuilder[0]);
    }

    private static class PartitionFilterBuilder implements FilterBuilder {

        private final AttributeColumn column;
        private Partition partition;

        public PartitionFilterBuilder(AttributeColumn column, NodePartition partition) {
            this.column = column;
            this.partition = partition;
        }

        public PartitionFilterBuilder(AttributeColumn column, EdgePartition partition) {
            this.column = column;
            this.partition = partition;
        }

        public Category getCategory() {
            return PARTITION;
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

        public PartitionFilter getFilter() {
            if (AttributeUtils.getDefault().isNodeColumn(column)) {
                return new NodePartitionFilter(partition);
            } else {
                return new EdgePartitionFilter(partition);
            }
        }

        public JPanel getPanel(Filter filter) {
            PartitionUI ui = Lookup.getDefault().lookup(PartitionUI.class);
            if (ui != null) {
                return ui.getPanel((PartitionFilter) filter);
            }
            return null;
        }
    }

    public static class NodePartitionFilter extends PartitionFilter implements NodeFilter {

        public NodePartitionFilter(Partition partition) {
            super(partition);
        }
    }

    public static class EdgePartitionFilter extends PartitionFilter implements EdgeFilter {

        public EdgePartitionFilter(Partition partition) {
            super(partition);
        }
    }

    public static abstract class PartitionFilter implements Filter {

        private Partition partition;
        private FilterProperty[] filterProperties;
        private List<Part> parts;

        public PartitionFilter(Partition partition) {
            this.partition = partition;
            parts = new ArrayList<Part>();
        }

        public String getName() {
            return NbBundle.getMessage(PartitionBuilder.class, "PartitionBuilder.name");
        }

        public boolean init(Graph graph) {
            this.partition = Lookup.getDefault().lookup(PartitionController.class).buildPartition(partition.getColumn(), graph);
            return true;
        }

        public boolean evaluate(Graph graph, Node node) {
            Object value = node.getNodeData().getAttributes().getValue(partition.getColumn().getIndex());
            if (value != null) {
                int size = parts.size();
                for (int i = 0; i < size; i++) {
                    Object obj = parts.get(i).getValue();
                    if (obj == null && value == null) {
                        return true;
                    } else if (obj != null && obj.equals(value)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean evaluate(Graph graph, Edge edge) {
            Object value = edge.getEdgeData().getAttributes().getValue(partition.getColumn().getIndex());
            if (value != null) {
                int size = parts.size();
                for (int i = 0; i < size; i++) {
                    Object obj = parts.get(i).getValue();
                    if (obj == null && value == null) {
                        return true;
                    } else if (obj != null && obj.equals(value)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public void finish() {
        }

        public void addPart(Part part) {
            if (!parts.contains(part)) {
                List<Part> newParts = new ArrayList<Part>(parts.size() + 1);
                newParts.addAll(parts);
                newParts.add(part);
                getProperties()[1].setValue(newParts);
            }
        }

        public void removePart(Part part) {
            List<Part> newParts = new ArrayList<Part>(parts);
            if (newParts.remove(part)) {
                getProperties()[1].setValue(newParts);
            }
        }

        public void unselectAll() {
            getProperties()[1].setValue(new ArrayList<Part>());
        }

        public void selectAll() {
            getProperties()[1].setValue(Arrays.asList(partition.getParts()));
        }

        public FilterProperty[] getProperties() {
            if (filterProperties == null) {
                filterProperties = new FilterProperty[0];
                try {
                    filterProperties = new FilterProperty[]{
                                FilterProperty.createProperty(this, AttributeColumn.class, "column"),
                                FilterProperty.createProperty(this, List.class, "parts")};
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return filterProperties;
        }

        public Partition getCurrentPartition() {
            if (partition.getPartsCount() == 0) {
                //build partition
                GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
                this.partition = Lookup.getDefault().lookup(PartitionController.class).buildPartition(partition.getColumn(), graphModel.getGraph());
            }
            return partition;
        }

        public Partition getPartition() {
            return partition;
        }

        public List<Part> getParts() {
            return parts;
        }

        public AttributeColumn getColumn() {
            return partition.getColumn();
        }

        public void setColumn(AttributeColumn column) {
        }

        public void setParts(List<Part> parts) {
            this.parts = parts;
        }

        public void setPartition(Partition partition) {
            this.partition = partition;
        }
    }
}
