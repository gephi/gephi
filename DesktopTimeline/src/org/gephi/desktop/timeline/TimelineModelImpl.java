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
import org.gephi.filters.api.Range;

import org.gephi.timeline.api.TimelineModel;
import org.gephi.timeline.api.TimelineModelEvent;
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
    
    //Architecture
    private final TimelineControllerImpl controller;
    private final DynamicController dynamicController;
    private double minValue = Double.POSITIVE_INFINITY;
    private double maxValue = Double.NEGATIVE_INFINITY;
    private DynamicModel dynamicModel;
    private Class unit = null;

    public TimelineModelImpl(TimelineControllerImpl controller) {
        this.controller = controller;
        dynamicController = Lookup.getDefault().lookup(DynamicController.class);
    }

    public void setup(DynamicModel dynamicModel) {
        this.dynamicModel = dynamicModel;
        dynamicController.addModelListener(this);
        fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.INIT, this, this));
    }

    public void unsetup() {
        dynamicModel = null;
        dynamicController.removeModelListener(this);
    }

    public void dynamicModelChanged(DynamicModelEvent event) {
        System.out.println("Dynamic model changed"+event.getEventType());
        if (event.getSource() == dynamicModel) {
            switch (event.getEventType()) {
                case IS_DYNAMIC:
                    Boolean isDynamic = (Boolean) event.getData();
                    break;
                case VISIBLE_INTERVAL:
                    System.out.println("get back visible interval " + event.getData());
                    fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.VISIBLE_INTERVAL, this, event.getData()));
                    break;
                case MIN_CHANGED:
                    setMinValue((Double)event.getData());
                    fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.MIN_CHANGED, this, event.getData()));
                    break;
                case MAX_CHANGED:
                    setMaxValue((Double)event.getData());
                    fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.MAX_CHANGED, this, event.getData()));
                    break;
            }
        }
    }

    private void setRange(Range range) {
        if (dynamicModel != null) {
            dynamicController.setVisibleInterval(range.getLowerDouble(), range.getUpperDouble());
        }
    }



    // Not used for the moment (will be used to generate charts)
    public String getFirstAttributeLabel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // Not used for the moment (will be used to generate charts)
    public String getLastAttributeLabel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // Not used for the moment (will be used to generate charts)
    public String getAttributeLabel(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // Not used for the moment (will be used to generate charts)
    public String getAttributeLabel(int from, int to) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // Not used for the moment (will be used to generate charts)
    public double getAttributeValue(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // Not used for the moment (will be used to generate charts)
    public double getAttributeValue(int from, int to) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public synchronized double getTotalSize() {
        return getMaxValue() - getMinValue();
    }

    public synchronized double getRangeSizeValue() {
        return getToValue() - getFromValue();
    }

    public synchronized double getRangeSizeFloat() {
        return getToFloat() - getFromFloat();
    }
    // set the range using real values

    public synchronized double getMinValue() {
        return minValue;
    }
    // set the range using real values

    public synchronized double getMaxValue() {
        return maxValue;
    }

    // set the range using real values
    public synchronized void setMinValue(double min) {
        //if (min >= maxValue) return;
        this.minValue = min;
        setFromValue(getMinValue() + getFromFloat() * getTotalSize());
    }
    // set the range using real values

    public synchronized void setMaxValue(double max) {
        //if (max <= minValue) return;
        this.maxValue = max;
        setFromValue(getMaxValue() + getToFloat() * getTotalSize());
    }

    public synchronized void setMinMax(double min, double max) {
        if (min >= max) {
            return;
        }
        this.minValue = min;
        setFromValue(getMinValue() + getFromFloat() * getTotalSize());
        this.maxValue = max;
        setFromValue(getMaxValue() + getToFloat() * getTotalSize());
    }

    // set the range using real values
    public synchronized void setRangeFromRealValues(double from, double to) {
        if (from >= to) {
            return;
        }
        fromValue = from;
        toValue = to;
        setRange(new Range(from, to));
    }

    public synchronized void setRangeFromFloat(double from, double to) {
        if (from >= to) {
            return;
        }
        fromFloat = from;
        fromValue = getMinValue() + from * getTotalSize();
        toFloat = to;
        toValue = getMinValue() + to * getTotalSize();
        setRange(new Range(getFromValue(), getToValue()));
    }

    public synchronized void setFromFloat(double from) {
        fromFloat = from;
        setFromValue(getMinValue() + getFromFloat() * getTotalSize());
        setRange(new Range(getFromValue(), getToValue()));
    }

    public synchronized void setToFloat(double to) {
        toFloat = to;
        setToValue(getMinValue() + getFromFloat() * getTotalSize());
        setRange(new Range(getFromValue(), getToValue()));
    }

    public synchronized double getFromFloat() {
        return fromFloat;
    }

    public synchronized double getToFloat() {
        return toFloat;
    }

    public synchronized void setFromValue(double from) {
        fromValue = from;
    }

    public synchronized void setToValue(double to) {
        toValue = to;
    }

    public synchronized double getFromValue() {
        return fromValue;
    }

    public synchronized double getToValue() {
        return toValue;
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

    private void fireTimelineModelEvent(TimelineModelEvent event) {
        controller.fireTimelineModelEvent(event);
    }
}
