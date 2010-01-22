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
package org.gephi.filters.plugin.partition;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.CategoryBuilder;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
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
            NodePartition[] nodePartitions = pc.getModel().getNodePartitions();
            EdgePartition[] edgePartitions = pc.getModel().getEdgePartitions();
            if (nodePartitions == null || nodePartitions.length == 0) {
                pc.refreshPartitions();
                nodePartitions = pc.getModel().getNodePartitions();
                edgePartitions = pc.getModel().getEdgePartitions();
            }
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
            return new PartitionFilter(partition);
        }

        public JPanel getPanel(Filter filter) {
            PartitionUI ui = Lookup.getDefault().lookup(PartitionUI.class);
            if (ui != null) {
                return ui.getPanel((PartitionFilter) filter);
            }
            return null;
        }
    }

    public static class PartitionFilter implements Filter {

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

        public void addPart(Part part) {
            if (!parts.contains(part)) {
                parts.add(part);
            }
        }

        public void removePart(Part part) {
            parts.remove(part);
        }

        public FilterProperty[] getProperties() {
            if (filterProperties == null) {
                filterProperties = new FilterProperty[0];
                try {
                    filterProperties = new FilterProperty[]{
                                FilterProperty.createProperty(this, Partition.class, "partition"),
                                FilterProperty.createProperty(this, List.class, "parts")};
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return filterProperties;
        }

        public Partition getPartition() {
            return partition;
        }
    }
}
