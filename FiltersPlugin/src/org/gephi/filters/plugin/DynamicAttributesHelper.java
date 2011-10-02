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

package org.gephi.filters.plugin;

import org.gephi.data.attributes.api.Estimator;
import org.gephi.data.attributes.type.DynamicType;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.FilterModel;
import org.gephi.filters.api.Query;
import org.gephi.filters.plugin.dynamic.DynamicRangeBuilder.DynamicRangeFilter;
import org.gephi.filters.spi.Filter;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class DynamicAttributesHelper {

    private final FilterModel filterModel;
    private final DynamicModel dynamicModel;
    private final boolean dynamic;

    public DynamicAttributesHelper(Filter filter, Graph graph) {
        if (graph != null) {
            Workspace workspace = graph.getGraphModel().getWorkspace();
            FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
            filterModel = filterController.getModel(workspace);

            DynamicController dynamicController = Lookup.getDefault().lookup(DynamicController.class);
            dynamicModel = dynamicController.getModel(workspace);
            dynamic = isDynamic(filter);
        } else {
            filterModel = null;
            dynamicModel = null;
            dynamic = false;
        }
    }

    private boolean isDynamic(Filter filter) {
        if (filterModel.getCurrentQuery() == null) {
            return false;
        }
        Query filterQuery = null;
        for (Query q : filterModel.getCurrentQuery().getQueries(filter.getClass())) {
            if (q.getFilter() == filter) {
                filterQuery = q;
                break;
            }
        }
        if (filterQuery != null) {
            for (Query query : filterQuery.getDescendantsAndSelf()) {
                if (query.getFilter().getClass().equals(DynamicRangeFilter.class)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Object getDynamicValue(Object attributeValue) {
        if (attributeValue != null && attributeValue instanceof DynamicType) {
            DynamicType dynamicValue = (DynamicType) attributeValue;
            Estimator estimator = dynamicModel == null ? Estimator.FIRST : dynamicModel.getEstimator();
            if (Number.class.isAssignableFrom(dynamicValue.getUnderlyingType())) {
                estimator = dynamicModel == null ? Estimator.AVERAGE : dynamicModel.getNumberEstimator();
            }
            TimeInterval timeInterval = new TimeInterval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            if (dynamic) {
                timeInterval = dynamicModel.getVisibleInterval();
            }
            return dynamicValue.getValue(timeInterval.getLow(), timeInterval.getHigh(), estimator);
        }
        return attributeValue;
    }

    public float getEdgeWeight(Edge edge) {
        if (dynamic) {
            TimeInterval timeInterval = dynamicModel.getVisibleInterval();
            return edge.getWeight(timeInterval.getLow(), timeInterval.getHigh());
        }
        return edge.getWeight();
    }
}
