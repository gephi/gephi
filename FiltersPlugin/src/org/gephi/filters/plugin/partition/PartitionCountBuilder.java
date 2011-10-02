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
import org.gephi.graph.api.HierarchicalGraph;
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
            NbBundle.getMessage(PartitionCountBuilder.class, "PartitionCountBuilder.name"),
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
            return NbBundle.getMessage(PartitionCountBuilder.class, "PartitionCountBuilder.description");
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

        public void destroy(Filter filter) {
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
        private Range range = new Range(0, 0);
        //States
        private Integer[] values;

        public PartitionCountFilter(Partition partition) {
            this.partition = partition;
        }

        public boolean init(Graph graph) {
            HierarchicalGraph hg = (HierarchicalGraph) graph;
            this.partition = Lookup.getDefault().lookup(PartitionController.class).buildPartition(partition.getColumn(), hg);
            if (partition.getParts().length > 0) {
                refreshValues(graph);
                return true;
            }
            return false;
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
        }

        private void refreshValues(Graph graph) {
            Integer min = Integer.MAX_VALUE;
            Integer max = Integer.MIN_VALUE;
            if (partition.getPartsCount() == 0) {
                //build partition
                this.partition = Lookup.getDefault().lookup(PartitionController.class).buildPartition(partition.getColumn(), graph);
            }
            values = new Integer[partition.getPartsCount()];
            Part[] parts = partition.getParts();
            for (int i = 0; i < parts.length; i++) {
                int partCount = parts[i].getObjects().length;
                min = Math.min(min, partCount);
                max = Math.max(max, partCount);
                values[i] = partCount;
            }
            if (range == null) {
                range = new Range(min, max, min, max);
            } else {
                range.setMinMax(min, max);
            }
        }

        public String getName() {
            return NbBundle.getMessage(PartitionCountBuilder.class, "PartitionCountBuilder.name");
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
            return values;
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
