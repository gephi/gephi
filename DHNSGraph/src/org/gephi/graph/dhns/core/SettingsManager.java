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
    private Boolean allowMultilevel;
    private Boolean autoMetaEdgeCreation;
    private MetaEdgeBuilder metaEdgeBuilder;
    private Float metaEdgeBuilderNonDeepDivisor;

    public SettingsManager(Dhns dhns) {
        this.dhns = dhns;
        defaultSettings();
    }

    private void defaultSettings() {
        allowMultilevel = Boolean.TRUE;
        autoMetaEdgeCreation = Boolean.TRUE;
        metaEdgeBuilderNonDeepDivisor = Float.valueOf(10f);
        metaEdgeBuilder = new SumMetaEdgeBuilder(metaEdgeBuilderNonDeepDivisor);
    }

    public boolean isAllowMultilevel() {
        return allowMultilevel;
    }

    public boolean isAutoMetaEdgeCreation() {
        return autoMetaEdgeCreation;
    }

    public MetaEdgeBuilder getMetaEdgeBuilder() {
        return metaEdgeBuilder;
    }

    public void setMetaEdgeBuilder(MetaEdgeBuilder metaEdgeBuilder) {
        putClientProperty("metaEdgeBuilder", metaEdgeBuilder);
    }

    public void putClientProperty(String key, Object value) {
        if (key.equals("allowMultilevel")) {
            allowMultilevel = (Boolean) value;
        } else if (key.equals("autoMetaEdgeCreation")) {
            autoMetaEdgeCreation = (Boolean) value;
            fireUpdate();
        } else if (key.equals("metaEdgeBuilder")) {
            if (value instanceof MetaEdgeBuilder) {
                metaEdgeBuilder = (MetaEdgeBuilder) value;
            }
            if (value.equals("average")) {
                metaEdgeBuilder = new AverageMetaEdgeBuilder(metaEdgeBuilderNonDeepDivisor);
            } else if (value.equals("sum")) {
                metaEdgeBuilder = new SumMetaEdgeBuilder(metaEdgeBuilderNonDeepDivisor);
            }
            fireUpdate();
        } else if (key.equals("metaEdgeBuilderNonDeepDivisor")) {
            metaEdgeBuilderNonDeepDivisor = (Float) value;
            if (metaEdgeBuilder instanceof SumMetaEdgeBuilder) {
                metaEdgeBuilder = new SumMetaEdgeBuilder(metaEdgeBuilderNonDeepDivisor);
            } else if (metaEdgeBuilder instanceof AverageMetaEdgeBuilder) {
                metaEdgeBuilder = new AverageMetaEdgeBuilder(metaEdgeBuilderNonDeepDivisor);
            }
            fireUpdate();
        }
    }

    public Object getClientProperty(String key) {
        if (key.equals("allowMultilevel")) {
            return allowMultilevel;
        } else if (key.equals("autoMetaEdgeCreation")) {
            return autoMetaEdgeCreation;
        } else if (key.equals("metaEdgeBuilder")) {
            if (metaEdgeBuilder instanceof SumMetaEdgeBuilder) {
                return "sum";
            } else if (metaEdgeBuilder instanceof AverageMetaEdgeBuilder) {
                return "average";
            } else {
                return metaEdgeBuilder.getClass().getName();
            }
        } else if (key.equals("metaEdgeBuilderNonDeepDivisor")) {
            return metaEdgeBuilderNonDeepDivisor;
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
        map.put("allowMultilevel", getClientProperty("allowMultilevel"));
        map.put("autoMetaEdgeCreation", getClientProperty("autoMetaEdgeCreation"));
        map.put("metaEdgeBuilder", getClientProperty("metaEdgeBuilder"));
        map.put("metaEdgeBuilderNonDeepDivisor", getClientProperty("metaEdgeBuilderNonDeepDivisor"));
        return map;
    }
}
