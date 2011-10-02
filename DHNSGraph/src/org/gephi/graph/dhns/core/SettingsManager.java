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
