/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.desktop.timeline;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;

import org.gephi.timeline.api.TimelineAnimator;
import org.gephi.timeline.api.TimelineAnimatorListener;

import org.gephi.timeline.api.TimelineInterval;
import org.gephi.timeline.api.TimelineIntervalListener;

import org.gephi.timeline.api.TimelineModel;
import org.gephi.timeline.api.TimelineModelListener;

import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author jbilcke
 */
@ServiceProvider(service = TimelineModel.class)
public class TimelineModelImpl
        implements
        TimelineModel,
        TimelineIntervalListener,
        TimelineAnimatorListener {

    private List<TimelineModelListener> listeners;

    private TimelineInterval interval;
    private TimelineAnimator animator;


    public TimelineModelImpl() {
        listeners = new ArrayList<TimelineModelListener>();
        interval = new TimelineIntervalImpl();
        animator = new TimelineAnimatorImpl();
        interval.addListener(this);
        animator.addListener(this);
    }

    public void fireChangeEvent() {
        ChangeEvent evt = new ChangeEvent(this);
        for (TimelineModelListener listener : listeners) {
            listener.timelineModelChanged(evt);
        }
    }

    public TimelineInterval getInterval() {
        return interval;
    }

    public TimelineAnimator getAnimator() {
        return animator;
    }

    public void addListener(TimelineModelListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(TimelineModelListener listener) {
        listeners.remove(listener);
    }

    public void timelineIntervalChanged(ChangeEvent event) {
    }

    public void timelineAnimatorChanged(ChangeEvent event) {
    }

}
