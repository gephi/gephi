/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.graph.dhns.core;

import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.dhns.edge.AverageMetaEdgeBuilder;
import org.gephi.graph.dhns.edge.MetaEdgeBuilder;
import org.gephi.graph.dhns.edge.SumMetaEdgeBuilder;

/**
 *
 * @author Mathieu Bastian
 */
public class SettingsManager {

    private Dhns dhns;

    //Settings
    private Boolean allowMultilevel;
    private Boolean autoMetaEdgeCreation;
    private MetaEdgeBuilder metaEdgeBuilder;
    private Float metaEdgeBuilderMinimum;
    private Float metaEdgeBuilderLimit;
    private Float metaEdgeBuilderNonDeepDivisor;

    public SettingsManager(Dhns dhns) {
        this.dhns = dhns;
        defaultSettings();
    }

    private void defaultSettings() {
        allowMultilevel = Boolean.TRUE;
        autoMetaEdgeCreation = Boolean.TRUE;
        metaEdgeBuilderMinimum = Float.valueOf(0.1f);
        metaEdgeBuilderLimit = Float.valueOf(10f);
        metaEdgeBuilderNonDeepDivisor = Float.valueOf(10f);
        metaEdgeBuilder = new SumMetaEdgeBuilder(metaEdgeBuilderMinimum, metaEdgeBuilderLimit, metaEdgeBuilderNonDeepDivisor);
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

    public void putClientProperty(String key, Object value) {
        if (key.equals("allowMultilevel")) {
            allowMultilevel = (Boolean) value;
        } else if (key.equals("autoMetaEdgeCreation")) {
            autoMetaEdgeCreation = (Boolean) value;
        } else if (key.equals("metaEdgeBuilder")) {
            if (value.equals("average")) {
                metaEdgeBuilder = new AverageMetaEdgeBuilder(metaEdgeBuilderMinimum, metaEdgeBuilderLimit, metaEdgeBuilderNonDeepDivisor);
            } else if (value.equals("sum")) {
                metaEdgeBuilder = new SumMetaEdgeBuilder(metaEdgeBuilderMinimum, metaEdgeBuilderLimit, metaEdgeBuilderNonDeepDivisor);
            }
        } else if (key.equals("metaEdgeBuilderMinimum")) {
            metaEdgeBuilderMinimum = (Float) value;
            if (metaEdgeBuilder instanceof SumMetaEdgeBuilder) {
                metaEdgeBuilder = new SumMetaEdgeBuilder(metaEdgeBuilderMinimum, metaEdgeBuilderLimit, metaEdgeBuilderNonDeepDivisor);
            } else if (metaEdgeBuilder instanceof AverageMetaEdgeBuilder) {
                metaEdgeBuilder = new AverageMetaEdgeBuilder(metaEdgeBuilderMinimum, metaEdgeBuilderLimit, metaEdgeBuilderNonDeepDivisor);
            }
        } else if (key.equals("metaEdgeBuilderLimit")) {
            metaEdgeBuilderLimit = (Float) value;
            if (metaEdgeBuilder instanceof SumMetaEdgeBuilder) {
                metaEdgeBuilder = new SumMetaEdgeBuilder(metaEdgeBuilderMinimum, metaEdgeBuilderLimit, metaEdgeBuilderNonDeepDivisor);
            } else if (metaEdgeBuilder instanceof AverageMetaEdgeBuilder) {
                metaEdgeBuilder = new AverageMetaEdgeBuilder(metaEdgeBuilderMinimum, metaEdgeBuilderLimit, metaEdgeBuilderNonDeepDivisor);
            }
        } else if (key.equals("metaEdgeBuilderNonDeepDivisor")) {
            metaEdgeBuilderNonDeepDivisor = (Float) value;
            if (metaEdgeBuilder instanceof SumMetaEdgeBuilder) {
                metaEdgeBuilder = new SumMetaEdgeBuilder(metaEdgeBuilderMinimum, metaEdgeBuilderLimit, metaEdgeBuilderNonDeepDivisor);
            } else if (metaEdgeBuilder instanceof AverageMetaEdgeBuilder) {
                metaEdgeBuilder = new AverageMetaEdgeBuilder(metaEdgeBuilderMinimum, metaEdgeBuilderLimit, metaEdgeBuilderNonDeepDivisor);
            }
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
            }
        } else if (key.equals("metaEdgeBuilderMinimum")) {
            return metaEdgeBuilderMinimum;
        } else if (key.equals("metaEdgeBuilderLimit")) {
            return metaEdgeBuilderLimit;
        } else if (key.equals("metaEdgeBuilderNonDeepDivisor")) {
            return metaEdgeBuilderNonDeepDivisor;
        }
        return null;
    }

    public Map<String, Object> getClientProperties() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("allowMultilevel", getClientProperty("allowMultilevel"));
        map.put("autoMetaEdgeCreation", getClientProperty("autoMetaEdgeCreation"));
        map.put("metaEdgeBuilder", getClientProperty("metaEdgeBuilder"));
        map.put("metaEdgeBuilderMinimum", getClientProperty("metaEdgeBuilderMinimum"));
        map.put("metaEdgeBuilderLimit", getClientProperty("metaEdgeBuilderLimit"));
        map.put("metaEdgeBuilderNonDeepDivisor", getClientProperty("metaEdgeBuilderNonDeepDivisor"));
        return map;
    }
}
