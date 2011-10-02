/*
Copyright 2008-2010 Gephi
Authors : Julian Bilcke <julian.bilcke@gephi.org>
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
package org.gephi.desktop.timeline;

import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.dynamic.api.DynamicModelEvent;
import org.gephi.dynamic.api.DynamicModelListener;

import org.gephi.timeline.api.TimelineModel;
import org.gephi.timeline.api.TimelineModelEvent;
import org.joda.time.DateTime;
import org.openide.util.Lookup;

/**
 *
 * @author Julian Bilcke
 */
public class TimelineModelImpl implements TimelineModel, DynamicModelListener {

    //Variable
    private double fromFloat = 0.0f;
    private double toFloat = 1.0f;
    private double fromValue = 0.0f;
    private double toValue = 0.0f;
    private double customMin = Double.NEGATIVE_INFINITY;
    private double customMax = Double.POSITIVE_INFINITY;
    private double modelMin = Double.NEGATIVE_INFINITY;
    private double modelMax = Double.POSITIVE_INFINITY;
    private Class unit = null;
    private boolean enabled = false;
    //Architecture
    private final TimelineControllerImpl controller;
    private DynamicController dynamicController;
    private DynamicModel dynamicModel;

    public TimelineModelImpl(TimelineControllerImpl controller) {
        this.controller = controller;
    }

    public void setup(DynamicModel dynamicModel) {
        dynamicController = Lookup.getDefault().lookup(DynamicController.class);
        this.dynamicModel = dynamicModel;
        enabled = !Double.isInfinite(dynamicModel.getVisibleInterval().getLow()) && !Double.isInfinite(dynamicModel.getVisibleInterval().getHigh());
        dynamicController.addModelListener(this);

        unit = (dynamicModel.getTimeFormat().equals(DynamicModel.TimeFormat.DATE)
              ||dynamicModel.getTimeFormat().equals(DynamicModel.TimeFormat.DATETIME)) ? DateTime.class : Double.class;
        customMin = Double.NEGATIVE_INFINITY;
        customMax = Double.POSITIVE_INFINITY;
        modelMin = Double.NEGATIVE_INFINITY;
        modelMax = Double.POSITIVE_INFINITY;
        setModelMin(dynamicModel.getMin());
        setModelMax(dynamicModel.getMax());
        refreshEnabled();
    }

    public void unsetup() {
        dynamicModel = null;
        dynamicController.removeModelListener(this);
    }

    public void disable() {
        enabled = false;
        setModelMin(Double.NEGATIVE_INFINITY);
        setModelMax(Double.POSITIVE_INFINITY);
    }

    public void dynamicModelChanged(DynamicModelEvent event) {
        if (event.getSource() == dynamicModel) {
            
            // TODO: should be in its own event "UNIT_CHANGED"
             unit = (dynamicModel.getTimeFormat().equals(DynamicModel.TimeFormat.DATE)
              ||dynamicModel.getTimeFormat().equals(DynamicModel.TimeFormat.DATETIME)) ? DateTime.class : Double.class;
             
            switch (event.getEventType()) {
                case VISIBLE_INTERVAL:
                    fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.VISIBLE_INTERVAL, this, event.getData()));
                    break;
                case MIN_CHANGED:
                    setModelMin((Double) event.getData());
                    break;
                case MAX_CHANGED:
                    setModelMax((Double) event.getData());
                    break;
            }
        }
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            refreshEnabled();
        }
    }

    private void refreshEnabled() {
        if (this.enabled) {
            dynamicController.setVisibleInterval(modelMin, modelMax);
        } else {
            dynamicController.setVisibleInterval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        }
    }

    public synchronized double getTotalSize() {
        return getMaxValue() - getMinValue();
    }

    public synchronized double getMinValue() {
        if (!Double.isInfinite(customMin)) {
            return customMin;
        }
        return modelMin;
    }

    public synchronized double getMaxValue() {
        if (!Double.isInfinite(customMax)) {
            return customMax;
        }
        return modelMax;
    }

    public double getFromFloat() {
        return fromFloat;
    }

    public double getToFloat() {
        return toFloat;
    }

    public void setModelMin(double modelMin) {
        if (modelMin != this.modelMin) {
            this.modelMin = modelMin;
            fromValue = getMinValue() + fromFloat * getTotalSize();
            fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.MIN_CHANGED, this, modelMin));
        }
    }

    public void setModelMax(double modelMax) {
        if (modelMax != this.modelMax) {
            this.modelMax = modelMax;
            fromValue = getMaxValue() + toFloat * getTotalSize();
            fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.MAX_CHANGED, this, modelMax));
        }
    }

    public synchronized void setCustomMin(double min) {
        if (min != this.customMin && min != modelMin) {
            this.customMin = min;
            fromValue = getMinValue() + fromFloat * getTotalSize();
            fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.MIN_CHANGED, this, min));
        }
    }

    public synchronized void setCustomMax(double max) {
        if (max != this.customMax && max != modelMax) {
            this.customMax = max;
            fromValue = getMaxValue() + toFloat * getTotalSize();
            fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.MAX_CHANGED, this, max));
        }
    }

    public synchronized void setRangeFromFloat(double from, double to) {
        if (from >= to || !enabled) {
            return;
        }
        fromFloat = from;
        fromValue = getMinValue() + from * getTotalSize();
        toFloat = to;
        toValue = getMinValue() + to * getTotalSize();
        if (dynamicModel != null) {
            dynamicController.setVisibleInterval(fromValue, toValue);
        }
    }

    public synchronized double getValueFromFloat(double position) {
        return position * getTotalSize();
    }

    public void setUnit(Class cl) {
        this.unit = cl;
    }

    public Class getUnit() {
        return unit;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private void fireTimelineModelEvent(TimelineModelEvent event) {
        controller.fireTimelineModelEvent(event);
    }
}
