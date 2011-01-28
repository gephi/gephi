/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
