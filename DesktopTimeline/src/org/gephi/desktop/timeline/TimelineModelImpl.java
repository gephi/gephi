/*
Copyright 2008-2010 Gephi
Authors : Julian Bilcke <julian.bilcke@gephi.org>
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

        unit = dynamicModel.getTimeFormat().equals(DynamicModel.TimeFormat.DATE)?DateTime.class:null;
        customMin = Double.NEGATIVE_INFINITY;
        customMax = Double.POSITIVE_INFINITY;
        modelMin = Double.NEGATIVE_INFINITY;
        modelMax = Double.POSITIVE_INFINITY;
        setModelMin(dynamicModel.getMin());
        setModelMax(dynamicModel.getMax());
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
            switch (event.getEventType()) {
                case VISIBLE_INTERVAL:
                    System.out.println("get back visible interval " + event.getData());
                    fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.VISIBLE_INTERVAL, this, event.getData()));
                    break;
                case MIN_CHANGED:
                    setModelMin((Double)event.getData());
                    break;
                case MAX_CHANGED:
                    setModelMax((Double)event.getData());
                    break;
            }
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

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private void fireTimelineModelEvent(TimelineModelEvent event) {
        controller.fireTimelineModelEvent(event);
    }
}
