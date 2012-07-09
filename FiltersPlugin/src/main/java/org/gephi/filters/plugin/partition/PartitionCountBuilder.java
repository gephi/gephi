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
import javax.swing.JPanel;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.api.Range;
import org.gephi.filters.plugin.AbstractAttributeFilter;
import org.gephi.filters.plugin.AbstractAttributeFilterBuilder;
import org.gephi.filters.plugin.graph.RangeUI;
import org.gephi.filters.spi.*;
import org.gephi.graph.api.Attributable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.partition.api.*;
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

    private static class PartitionCountFilterBuilder extends AbstractAttributeFilterBuilder {

        private Partition partition;

        public PartitionCountFilterBuilder(AttributeColumn column, Partition partition) {
            super(column,
                    PARTITION_COUNT,
                    NbBundle.getMessage(PartitionCountBuilder.class, "PartitionCountBuilder.description"),
                    null);
            this.partition = partition;
        }

        public PartitionCountFilter getFilter() {
            return new PartitionCountFilter(partition);
        }

        public JPanel getPanel(Filter filter) {
            RangeUI ui = Lookup.getDefault().lookup(RangeUI.class);
            if (ui != null) {
                return ui.getPanel((PartitionCountFilter) filter);
            }
            return null;
        }
    }

    public static class PartitionCountFilter extends AbstractAttributeFilter implements RangeFilter {

        private Partition partition;
        private Range range;

        public PartitionCountFilter(Partition partition) {
            super(NbBundle.getMessage(PartitionCountBuilder.class, "PartitionCountBuilder.name"),
                    partition.getColumn());
            this.partition = partition;
            
            //Add property
            addProperty(Range.class, "range");
        }

        public boolean init(Graph graph) {
            HierarchicalGraph hg = (HierarchicalGraph) graph;
            this.partition = Lookup.getDefault().lookup(PartitionController.class).buildPartition(partition.getColumn(), hg);
            if (partition.getParts().length > 0) {
                return true;
            }
            return false;
        }

        public boolean evaluate(Graph graph, Attributable attributable) {
            Part p = partition.getPart(attributable);
            if (p != null) {
                int partCount = p.getObjects().length;
                return range.isInRange(partCount);
            }
            return false;
        }

        public void finish() {
        }

        public Number[] getValues(Graph graph) {
            if (partition.getPartsCount() == 0) {
                //build partition
                this.partition = Lookup.getDefault().lookup(PartitionController.class).buildPartition(partition.getColumn(), graph);
            }
            Integer[] values = new Integer[partition.getPartsCount()];
            Part[] parts = partition.getParts();
            for (int i = 0; i < parts.length; i++) {
                int partCount = parts[i].getObjects().length;
                values[i] = partCount;
            }
            return values;
        }

        public FilterProperty getRangeProperty() {
            return properties.get(1);
        }

        public Range getRange() {
            return range;
        }

        public void setRange(Range range) {
            this.range = range;
        }
    }
}
