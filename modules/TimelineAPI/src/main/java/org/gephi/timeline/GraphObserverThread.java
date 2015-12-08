/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.timeline;

import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Interval;

/**
 *
 * @author mbastian
 */
public class GraphObserverThread extends Thread {

    private final TimelineControllerImpl timelineController;
    private final TimelineModelImpl timelineModel;
    private boolean stop;
    private Interval interval;

    public GraphObserverThread(TimelineControllerImpl controller, TimelineModelImpl model) {
        this.timelineModel = model;
        this.timelineController = controller;
        this.interval = model.getGraphModel().getTimeBounds();
    }

    @Override
    public void run() {
        while (!stop) {
            GraphModel graphModel = timelineModel.getGraphModel();
            Interval bounds = graphModel.getTimeBounds();
            if(!bounds.equals(interval)) {
                interval = bounds;
                timelineController.setMinMax(interval.getLow(), interval.getHigh());
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {

            }
        }
    }

    public void stopThread() {
        stop = true;
    }

}
