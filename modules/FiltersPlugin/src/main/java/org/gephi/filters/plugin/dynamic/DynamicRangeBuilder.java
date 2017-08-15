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
package org.gephi.filters.plugin.dynamic;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.filters.api.Range;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.CategoryBuilder;
import org.gephi.filters.spi.ComplexFilter;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.graph.api.types.IntervalSet;
import org.gephi.graph.api.types.TimestampSet;
import org.gephi.project.api.Workspace;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = CategoryBuilder.class)
public class DynamicRangeBuilder implements CategoryBuilder {

    private final static Category DYNAMIC = new Category(
            NbBundle.getMessage(DynamicRangeBuilder.class, "DynamicRangeBuilder.category"),
            null,
            null);

    @Override
    public Category getCategory() {
        return DYNAMIC;
    }

    @Override
    public FilterBuilder[] getBuilders(Workspace workspace) {
        List<FilterBuilder> builders = new ArrayList<>();
        GraphModel am = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
        if (am.isDynamic()) {
            builders.add(new DynamicRangeFilterBuilder(am));
        }
        return builders.toArray(new FilterBuilder[0]);
    }

    private static class DynamicRangeFilterBuilder implements FilterBuilder {

        private final GraphModel graphModel;

        public DynamicRangeFilterBuilder(GraphModel graphModel) {
            this.graphModel = graphModel;
        }

        @Override
        public Category getCategory() {
            return DYNAMIC;
        }

        @Override
        public String getName() {
            return "Time Interval";
        }

        @Override
        public Icon getIcon() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public DynamicRangeFilter getFilter(Workspace workspace) {
            return new DynamicRangeFilter(graphModel);
        }

        @Override
        public JPanel getPanel(Filter filter) {
            final DynamicRangeFilter dynamicRangeFilter = (DynamicRangeFilter) filter;
            DynamicRangeUI ui = Lookup.getDefault().lookup(DynamicRangeUI.class);
            if (ui != null) {
                return ui.getPanel(dynamicRangeFilter);
            }
            return null;
        }

        @Override
        public void destroy(Filter filter) {
        }
    }

    public static class DynamicRangeFilter implements ComplexFilter {

        private final TimeRepresentation timeRepresentation;
        private FilterProperty[] filterProperties;
        private Interval visibleInterval;
        private Range range = new Range(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        private boolean keepNull = true;

        public DynamicRangeFilter(GraphModel graphModel) {
            this.timeRepresentation = graphModel.getConfiguration().getTimeRepresentation();
        }

        @Override
        public Graph filter(Graph graph) {
            visibleInterval = new Interval(range.getLowerDouble(), range.getUpperDouble());

            List<Node> toRemoveNodes = new ArrayList<>();
            for (Node n : graph.getNodes()) {
                if (!evaluateElement(n)) {
                    toRemoveNodes.add(n);
                }
            }
            graph.removeAllNodes(toRemoveNodes);

            List<Edge> toRemoveEdge = new ArrayList<>();
            for (Edge e : graph.getEdges()) {
                if (!evaluateElement(e)) {
                    toRemoveEdge.add(e);
                }
            }
            graph.removeAllEdges(toRemoveEdge);

            graph.getModel().setTimeInterval(graph.getView(), visibleInterval);

            return graph;
        }

        private boolean evaluateElement(Element element) {
            if (timeRepresentation.equals(TimeRepresentation.INTERVAL)) {
                IntervalSet timeSet = (IntervalSet) element.getAttribute("timeset");
                if (timeSet != null) {
                    for (Interval i : timeSet.toArray()) {
                        if (visibleInterval.compareTo(i) == 0) {
                            return true;
                        }
                    }
                } else if (keepNull) {
                    return true;
                }
            } else {
                TimestampSet timeSet = (TimestampSet) element.getAttribute("timeset");
                if (timeSet != null) {
                    for (double t : timeSet.toPrimitiveArray()) {
                        if (visibleInterval.compareTo(t) == 0) {
                            return true;
                        }
                    }
                } else if (keepNull) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(DynamicRangeBuilder.class, "DynamicRangeBuilder.name");
        }

        @Override
        public FilterProperty[] getProperties() {
            if (filterProperties == null) {
                filterProperties = new FilterProperty[0];
                try {
                    filterProperties = new FilterProperty[]{
                        FilterProperty.createProperty(this, Range.class, "range"),
                        FilterProperty.createProperty(this, Boolean.class, "keepNull")};
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return filterProperties;
        }

        public FilterProperty getRangeProperty() {
            return getProperties()[0];
        }

        public boolean isKeepNull() {
            return keepNull;
        }

        public void setKeepNull(boolean keepNull) {
            this.keepNull = keepNull;
        }

        public Range getRange() {
            return range;
        }

        public void setRange(Range range) {
            this.range = range;
        }

        public void destroy() {
        }
    }
}
