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
package org.gephi.graph.dhns.core;

import java.util.HashMap;
import java.util.Map;
import org.gephi.data.attributes.api.Estimator;
import org.gephi.graph.api.GraphEvent.EventType;
import org.gephi.graph.api.GraphSettings;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.dhns.edge.AverageMetaEdgeBuilder;
import org.gephi.graph.dhns.edge.SumMetaEdgeBuilder;
import org.gephi.graph.dhns.event.GeneralEvent;
import org.gephi.graph.spi.MetaEdgeBuilder;

/**
 *
 * @author Mathieu Bastian
 */
public class SettingsManager implements GraphSettings {

    private Dhns dhns;
    //Settings
    private Boolean autoMetaEdgeCreation;
    private MetaEdgeBuilder metaEdgeBuilder;
    private Float metaEdgeBuilderNonDeepDivisor;
    private Estimator defaultWeightEstimator;

    public SettingsManager(Dhns dhns) {
        this.dhns = dhns;
        defaultSettings();
    }

    private void defaultSettings() {
        autoMetaEdgeCreation = Boolean.TRUE;
        metaEdgeBuilderNonDeepDivisor = Float.valueOf(10f);
        metaEdgeBuilder = new SumMetaEdgeBuilder(metaEdgeBuilderNonDeepDivisor);
        defaultWeightEstimator = Estimator.AVERAGE;
    }

    public boolean isAutoMetaEdgeCreation() {
        return autoMetaEdgeCreation;
    }

    public MetaEdgeBuilder getMetaEdgeBuilder() {
        return metaEdgeBuilder;
    }

    public Estimator getDefaultWeightEstimator() {
        return defaultWeightEstimator;
    }

    public void setMetaEdgeBuilder(MetaEdgeBuilder metaEdgeBuilder) {
        putClientProperty(GraphSettings.METAEDGE_BUILDER, metaEdgeBuilder);
    }

    public void putClientProperty(String key, Object value) {
        if (key.equals(GraphSettings.AUTO_META_EDGES)) {
            autoMetaEdgeCreation = (Boolean) value;
            fireUpdate();
        } else if (key.equals(GraphSettings.METAEDGE_BUILDER)) {
            if (value instanceof MetaEdgeBuilder) {
                metaEdgeBuilder = (MetaEdgeBuilder) value;
            }
            if (value.equals("average")) {
                metaEdgeBuilder = new AverageMetaEdgeBuilder(metaEdgeBuilderNonDeepDivisor);
            } else if (value.equals("sum")) {
                metaEdgeBuilder = new SumMetaEdgeBuilder(metaEdgeBuilderNonDeepDivisor);
            }
            fireUpdate();
        } else if (key.equals(GraphSettings.METAEDGE_BUILDER_NONDEEP_DIVISOR)) {
            metaEdgeBuilderNonDeepDivisor = (Float) value;
            if (metaEdgeBuilder instanceof SumMetaEdgeBuilder) {
                metaEdgeBuilder = new SumMetaEdgeBuilder(metaEdgeBuilderNonDeepDivisor);
            } else if (metaEdgeBuilder instanceof AverageMetaEdgeBuilder) {
                metaEdgeBuilder = new AverageMetaEdgeBuilder(metaEdgeBuilderNonDeepDivisor);
            }
            fireUpdate();
        } else if(key.equals(GraphSettings.DEFAULT_WEIGHT_ESTIMATOR)) {
            defaultWeightEstimator = (Estimator)value;
            fireUpdate();
        }
    }

    public Object getClientProperty(String key) {
        if (key.equals(GraphSettings.AUTO_META_EDGES)) {
            return autoMetaEdgeCreation;
        } else if (key.equals(GraphSettings.METAEDGE_BUILDER)) {
            if (metaEdgeBuilder instanceof SumMetaEdgeBuilder) {
                return "sum";
            } else if (metaEdgeBuilder instanceof AverageMetaEdgeBuilder) {
                return "average";
            } else {
                return metaEdgeBuilder.getClass().getName();
            }
        } else if (key.equals(GraphSettings.METAEDGE_BUILDER_NONDEEP_DIVISOR)) {
            return metaEdgeBuilderNonDeepDivisor;
        } else if (key.equals(GraphSettings.DEFAULT_WEIGHT_ESTIMATOR)) {
            return defaultWeightEstimator;
        }
        return null;
    }

    private void fireUpdate() {
        dhns.getGraphVersion().incEdgeVersion();
        for (GraphView view : dhns.getGraphStructure().getViews()) {
            dhns.getEventManager().fireEvent(new GeneralEvent(EventType.META_EDGES_UPDATE, view));
        }
    }

    public Map<String, Object> getClientProperties() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(GraphSettings.AUTO_META_EDGES, getClientProperty(GraphSettings.AUTO_META_EDGES));
        map.put(GraphSettings.METAEDGE_BUILDER, getClientProperty(GraphSettings.METAEDGE_BUILDER));
        map.put(GraphSettings.METAEDGE_BUILDER_NONDEEP_DIVISOR, getClientProperty(GraphSettings.METAEDGE_BUILDER_NONDEEP_DIVISOR));
        map.put(GraphSettings.DEFAULT_WEIGHT_ESTIMATOR, getClientProperty(GraphSettings.DEFAULT_WEIGHT_ESTIMATOR));
        return map;
    }
}
