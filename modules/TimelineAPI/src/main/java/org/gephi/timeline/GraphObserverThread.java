/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.timeline;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.Interval;

/**
 * @author mbastian
 */
public class GraphObserverThread extends Thread {

    private static final Logger LOGGER = Logger.getLogger(GraphObserverThread.class.getName());
    private final TimelineControllerImpl timelineController;
    private final TimelineModelImpl timelineModel;
    private volatile boolean stop;
    private Interval interval;

    public GraphObserverThread(TimelineControllerImpl controller, TimelineModelImpl model) {
        this.timelineModel = model;
        this.timelineController = controller;
        this.interval = model.getTimeBounds();
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                poll();
            } catch (RuntimeException ex) {
                //Keep polling, this thread is what keeps the bounds cache fresh
                LOGGER.log(Level.WARNING, "Failed to observe the time bounds", ex);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {

            }
        }
    }

    void poll() {
        Interval bounds = timelineModel.getGraphModel().getTimeBounds();
        if (stop) {
            //Stopped while waiting for the graph lock, do not publish a stale result
            return;
        }
        //Cached before the events are fired so the listeners always read the new bounds
        timelineModel.setTimeBounds(bounds);
        if (!bounds.equals(interval)) {
            interval = bounds;
            timelineController.setMinMax(this, interval.getLow(), interval.getHigh());
        }
    }

    public void stopThread() {
        stop = true;
        interrupt();
    }

    TimelineModelImpl getTimelineModel() {
        return timelineModel;
    }

}
