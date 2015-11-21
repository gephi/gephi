///*
// Copyright 2008-2010 Gephi
// Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
// Website : http://www.gephi.org
//
// This file is part of Gephi.
//
// DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
//
// Copyright 2011 Gephi Consortium. All rights reserved.
//
// The contents of this file are subject to the terms of either the GNU
// General Public License Version 3 only ("GPL") or the Common
// Development and Distribution License("CDDL") (collectively, the
// "License"). You may not use this file except in compliance with the
// License. You can obtain a copy of the License at
// http://gephi.org/about/legal/license-notice/
// or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
// specific language governing permissions and limitations under the
// License.  When distributing the software, include this License Header
// Notice in each file and include the License files at
// /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
// License Header, with the fields enclosed by brackets [] replaced by
// your own identifying information:
// "Portions Copyrighted [year] [name of copyright owner]"
//
// If you wish your version of this file to be governed by only the CDDL
// or only the GPL Version 3, indicate your decision by adding
// "[Contributor] elects to include this software in this distribution
// under the [CDDL or GPL Version 3] license." If you do not indicate a
// single choice of license, a recipient has the option to distribute
// your version of this file under either the CDDL, the GPL Version 3 or
// to extend the choice of license to its licensees as provided above.
// However, if you add GPL Version 3 code and therefore, elected the GPL
// Version 3 license, then the option applies only if the new code is
// made subject to such option by the copyright holder.
//
// Contributor(s):
//
// Portions Copyrighted 2011 Gephi Consortium.
// */
//package org.gephi.filters.plugin.partition;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//import javax.swing.Icon;
//import javax.swing.JPanel;
//import org.gephi.appearance.api.AppearanceController;
//import org.gephi.appearance.api.AppearanceModel;
//import org.gephi.appearance.api.Function;
//import org.gephi.appearance.api.Partition;
//import org.gephi.appearance.api.PartitionFunction;
//import org.gephi.filters.api.FilterLibrary;
//import org.gephi.filters.spi.Category;
//import org.gephi.filters.spi.CategoryBuilder;
//import org.gephi.filters.spi.EdgeFilter;
//import org.gephi.filters.spi.Filter;
//import org.gephi.filters.spi.FilterBuilder;
//import org.gephi.filters.spi.FilterProperty;
//import org.gephi.filters.spi.NodeFilter;
//import org.gephi.graph.api.AttributeUtils;
//import org.gephi.graph.api.Column;
//import org.gephi.graph.api.Edge;
//import org.gephi.graph.api.Graph;
//import org.gephi.graph.api.GraphController;
//import org.gephi.graph.api.GraphModel;
//import org.gephi.graph.api.Node;
//import org.openide.util.Exceptions;
//import org.openide.util.Lookup;
//import org.openide.util.NbBundle;
//import org.openide.util.lookup.ServiceProvider;
//
///**
// *
// * @author Mathieu Bastian
// */
//@ServiceProvider(service = CategoryBuilder.class)
//public class PartitionBuilder implements CategoryBuilder {
//
//    private final static Category PARTITION = new Category(
//            NbBundle.getMessage(PartitionBuilder.class, "PartitionBuilder.name"),
//            null,
//            FilterLibrary.ATTRIBUTES);
//
//    @Override
//    public Category getCategory() {
//        return PARTITION;
//    }
//
//    @Override
//    public FilterBuilder[] getBuilders() {
//        List<FilterBuilder> builders = new ArrayList<FilterBuilder>();
//        AppearanceModel am = Lookup.getDefault().lookup(AppearanceController.class).getModel();
//        for (Function f : am.getNodeFunctions()) {
//            if (f.isPartition()) {
//                Partition p = ((PartitionFunction) f).getPartition();
//                PartitionFilterBuilder builder = new PartitionFilterBuilder(p);
//                builders.add(builder);
//            }
//        }
//        for (Function f : am.getEdgeFunctions()) {
//            if (f.isPartition()) {
//                Partition p = ((PartitionFunction) f).getPartition();
//                PartitionFilterBuilder builder = new PartitionFilterBuilder(p);
//                builders.add(builder);
//            }
//        }
//
//        return builders.toArray(new FilterBuilder[0]);
//    }
//
//    private static class PartitionFilterBuilder implements FilterBuilder {
//
//        private final Partition partition;
//
//        public PartitionFilterBuilder(Column column, Partition partition) {
//            this.partition = partition;
//        }
//
//        @Override
//        public Category getCategory() {
//            return PARTITION;
//        }
//
//        @Override
//        public String getName() {
//            return partition.getColumn().getTitle() + " (" + (AttributeUtils.isNodeColumn(partition.getColumn())
//                    ? NbBundle.getMessage(PartitionFilterBuilder.class, "PartitionFilterBuilder.name.node")
//                    : NbBundle.getMessage(PartitionFilterBuilder.class, "PartitionFilterBuilder.name.edge")) + ")";
//        }
//
//        @Override
//        public Icon getIcon() {
//            return null;
//        }
//
//        @Override
//        public String getDescription() {
//            return NbBundle.getMessage(PartitionBuilder.class, "PartitionBuilder.description");
//        }
//
//        @Override
//        public PartitionFilter getFilter() {
//            if (AttributeUtils.isNodeColumn(partition.getColumn())) {
//                return new NodePartitionFilter(partition);
//            } else {
//                return new EdgePartitionFilter(partition);
//            }
//        }
//
//        @Override
//        public JPanel getPanel(Filter filter) {
//            PartitionUI ui = Lookup.getDefault().lookup(PartitionUI.class);
//            if (ui != null) {
//                return ui.getPanel((PartitionFilter) filter);
//            }
//            return null;
//        }
//
//        @Override
//        public void destroy(Filter filter) {
//        }
//    }
//
//    public static class NodePartitionFilter extends PartitionFilter implements NodeFilter {
//
//        public NodePartitionFilter(Partition partition) {
//            super(partition);
//        }
//    }
//
//    public static class EdgePartitionFilter extends PartitionFilter implements EdgeFilter {
//
//        public EdgePartitionFilter(Partition partition) {
//            super(partition);
//        }
//    }
//
//    public static abstract class PartitionFilter implements Filter {
//
//        protected Partition partition;
//        protected FilterProperty[] filterProperties;
//        protected Set<Object> parts;
//
//        public PartitionFilter(Partition partition) {
//            this.partition = partition;
//            parts = new HashSet<Object>();
//        }
//
//        @Override
//        public String getName() {
//            return NbBundle.getMessage(PartitionBuilder.class, "PartitionBuilder.name") + " (" + partition.getColumn().getTitle() + ")";
//        }
//
//        public boolean init(Graph graph) {
//            return true;
//        }
//
//        public boolean evaluate(Graph graph, Node node) {
//            Object value = node.getNodeData().getAttributes().getValue(partition.getColumn().getIndex());
//            int size = parts.size();
//            for (int i = 0; i < size; i++) {
//                Object obj = parts.get(i).getValue();
//                if (obj == null && value == null) {
//                    return true;
//                } else if (obj != null && value != null && obj.equals(value)) {
//                    return true;
//                }
//            }
//
//            return false;
//        }
//
//        public boolean evaluate(Graph graph, Edge edge) {
//            Object value = edge.getEdgeData().getAttributes().getValue(partition.getColumn().getIndex());
//            int size = parts.size();
//            for (int i = 0; i < size; i++) {
//                Object obj = parts.get(i).getValue();
//                if (obj == null && value == null) {
//                    return true;
//                } else if (obj != null && value != null && obj.equals(value)) {
//                    return true;
//                }
//            }
//
//            return false;
//        }
//
//        public void finish() {
//        }
//
//        public void addPart(Part part) {
//            if (!parts.contains(part)) {
//                List<Part> newParts = new ArrayList<Part>(parts.size() + 1);
//                newParts.addAll(parts);
//                newParts.add(part);
//                getProperties()[1].setValue(newParts);
//            }
//        }
//
//        public void removePart(Part part) {
//            List<Part> newParts = new ArrayList<Part>(parts);
//            if (newParts.remove(part)) {
//                getProperties()[1].setValue(newParts);
//            }
//        }
//
//        public void unselectAll() {
//            getProperties()[1].setValue(new ArrayList<Part>());
//        }
//
//        public void selectAll() {
//            getProperties()[1].setValue(Arrays.asList(partition.getParts()));
//        }
//
//        @Override
//        public FilterProperty[] getProperties() {
//            if (filterProperties == null) {
//                filterProperties = new FilterProperty[0];
//                try {
//                    filterProperties = new FilterProperty[]{
//                        FilterProperty.createProperty(this, AttributeColumn.class, "column"),
//                        FilterProperty.createProperty(this, List.class, "parts")};
//                } catch (Exception ex) {
//                    Exceptions.printStackTrace(ex);
//                }
//            }
//            return filterProperties;
//        }
//
//        public Partition getCurrentPartition() {
//            if (partition.getPartsCount() == 0) {
//                //build partition
//                GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
//                this.partition = Lookup.getDefault().lookup(PartitionController.class).buildPartition(partition.getColumn(), graphModel.getHierarchicalGraphVisible());
//            }
//            return partition;
//        }
//
//        public Partition getPartition() {
//            return partition;
//        }
//
//        public List<Part> getParts() {
//            return parts;
//        }
//
//        public AttributeColumn getColumn() {
//            return partition.getColumn();
//        }
//
//        public void setColumn(AttributeColumn column) {
//        }
//
//        public void setParts(List<Part> parts) {
//            this.parts = parts;
//        }
//
//        public void setPartition(Partition partition) {
//            this.partition = partition;
//        }
//    }
//}
