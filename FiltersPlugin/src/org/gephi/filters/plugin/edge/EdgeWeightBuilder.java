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
package org.gephi.filters.plugin.edge;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.api.Range;
import org.gephi.filters.plugin.AbstractFilter;
import org.gephi.filters.plugin.DynamicAttributesHelper;
import org.gephi.filters.plugin.graph.RangeUI;
import org.gephi.filters.spi.*;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.HierarchicalGraph;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = FilterBuilder.class)
public class EdgeWeightBuilder implements FilterBuilder {

    public Category getCategory() {
        return FilterLibrary.EDGE;
    }

    public String getName() {
        return NbBundle.getMessage(EdgeWeightBuilder.class, "EdgeWeightBuilder.name");
    }

    public Icon getIcon() {
        return null;
    }

    public String getDescription() {
        return NbBundle.getMessage(EdgeWeightBuilder.class, "EdgeWeightBuilder.description");
    }

    public Filter getFilter() {
        return new EdgeWeightFilter();
    }

    public JPanel getPanel(Filter filter) {
        RangeUI ui = Lookup.getDefault().lookup(RangeUI.class);
        if (ui != null) {
            return ui.getPanel((EdgeWeightFilter) filter);
        }
        return null;
    }

    public void destroy(Filter filter) {
    }

    public static class EdgeWeightFilter extends AbstractFilter implements RangeFilter, EdgeFilter {

        private Range range;
        private DynamicAttributesHelper dynamicHelper = new DynamicAttributesHelper(this, null);

        public EdgeWeightFilter() {
            super(NbBundle.getMessage(EdgeWeightBuilder.class, "EdgeWeightBuilder.name"));

            addProperty(Range.class, "range");
        }

        public boolean init(Graph graph) {
            HierarchicalGraph hgraph = (HierarchicalGraph) graph;
            if (hgraph.getTotalEdgeCount() == 0) {
                return false;
            }
            dynamicHelper = new DynamicAttributesHelper(this, hgraph);
            return true;
        }

        public boolean evaluate(Graph graph, Edge edge) {
            float weight = dynamicHelper.getEdgeWeight(edge);
            return range.isInRange(weight);
        }

        public void finish() {
        }

        public Number[] getValues(Graph graph) {
            HierarchicalGraph hgraph = (HierarchicalGraph) graph;
            List<Number> values = new ArrayList<Number>();
            for (Edge e : hgraph.getEdgesAndMetaEdges()) {
                float weight = dynamicHelper.getEdgeWeight(e);
                values.add(weight);
            }
            return values.toArray(new Number[0]);
        }

        public FilterProperty getRangeProperty() {
            return getProperties()[0];
        }

        public Range getRange() {
            return range;
        }

        public void setRange(Range range) {
            this.range = range;
        }
    }
}
