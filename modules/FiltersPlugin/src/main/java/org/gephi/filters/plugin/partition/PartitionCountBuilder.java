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
import java.util.Collection;
import java.util.List;
import javax.swing.JPanel;
import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Partition;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.api.Range;
import org.gephi.filters.plugin.AbstractAttributeFilter;
import org.gephi.filters.plugin.AbstractAttributeFilterBuilder;
import org.gephi.filters.plugin.graph.RangeUI;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.CategoryBuilder;
import org.gephi.filters.spi.EdgeFilter;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.filters.spi.NodeFilter;
import org.gephi.filters.spi.RangeFilter;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Mathieu Bastian
 */
@ServiceProvider(service = CategoryBuilder.class)
public class PartitionCountBuilder implements CategoryBuilder {

    private final static Category PARTITION_COUNT = new Category(
        NbBundle.getMessage(PartitionCountBuilder.class, "PartitionCountBuilder.name"),
        null,
        FilterLibrary.ATTRIBUTES);

    @Override
    public Category getCategory() {
        return PARTITION_COUNT;
    }

    @Override
    public FilterBuilder[] getBuilders(Workspace workspace) {
        List<FilterBuilder> builders = new ArrayList<>();
        GraphModel gm = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
        AppearanceModel am = Lookup.getDefault().lookup(AppearanceController.class).getModel(workspace);

        //Force refresh
        am.getNodeFunctions();
        am.getEdgeFunctions();

        for (Column nodeCol : gm.getNodeTable()) {
            if (!nodeCol.isProperty()) {
                Partition partition = am.getNodePartition(nodeCol);
                if (partition != null) {
                    PartitionCountFilterBuilder builder = new PartitionCountFilterBuilder(partition);
                    builders.add(builder);
                }
            }
        }

        for (Column edgeCol : gm.getEdgeTable()) {
            if (!edgeCol.isProperty()) {
                Partition partition = am.getEdgePartition(edgeCol);
                if (partition != null) {
                    PartitionCountFilterBuilder builder = new PartitionCountFilterBuilder(partition);
                    builders.add(builder);
                }
            }
        }

        return builders.toArray(new FilterBuilder[0]);
    }

    private static class PartitionCountFilterBuilder extends AbstractAttributeFilterBuilder {

        private final Partition partition;

        public PartitionCountFilterBuilder(Partition partition) {
            super(partition.getColumn(),
                PARTITION_COUNT,
                NbBundle.getMessage(PartitionCountBuilder.class, "PartitionCountBuilder.description"),
                null);
            this.partition = partition;
        }

        @Override
        public PartitionCountFilter getFilter(Workspace workspace) {
            if (AttributeUtils.isNodeColumn(column)) {
                return new PartitionCountFilter.Node(partition);
            } else {
                return new PartitionCountFilter.Edge(partition);
            }
        }

        @Override
        public JPanel getPanel(Filter filter) {
            RangeUI ui = Lookup.getDefault().lookup(RangeUI.class);
            if (ui != null) {
                return ui.getPanel((PartitionCountFilter) filter);
            }
            return null;
        }
    }

    public static abstract class PartitionCountFilter<K extends Element> extends AbstractAttributeFilter<K>
        implements RangeFilter {

        protected Partition partition;
        private Range range;

        public PartitionCountFilter(Partition partition) {
            super(NbBundle.getMessage(PartitionCountBuilder.class, "PartitionCountBuilder.name"),
                partition.getColumn());

            //Add property
            addProperty(Range.class, "range");
        }

        @Override
        public boolean evaluate(Graph graph, Element element) {
            Object p = partition.getValue(element, graph);
            int partCount = partition.count(p, graph);
            return range.isInRange(partCount);
        }

        @Override
        public void finish() {
        }

        @Override
        public Number[] getValues(Graph graph) {
            if (init(graph)) {
                Collection vals = partition.getValues(graph);
                Integer[] values = new Integer[vals.size()];
                int i = 0;
                for (Object v : vals) {
                    values[i++] = partition.count(v, graph);
                }
                return values;
            } else {
                return new Integer[0];
            }
        }

        @Override
        public FilterProperty getRangeProperty() {
            return getProperties()[1];
        }

        public Range getRange() {
            return range;
        }

        public void setRange(Range range) {
            this.range = range;
        }

        public static class Node extends PartitionCountFilter<org.gephi.graph.api.Node> implements NodeFilter {

            public Node(Partition partition) {
                super(partition);
            }

            @Override
            public boolean init(Graph graph) {
                return partition != null && partition.getColumn() != null;
            }
        }

        public static class Edge extends PartitionCountFilter<org.gephi.graph.api.Edge> implements EdgeFilter {

            public Edge(Partition partition) {
                super(partition);
            }

            @Override
            public boolean init(Graph graph) {
                return partition != null && partition.getColumn() != null;
            }
        }
    }
}
