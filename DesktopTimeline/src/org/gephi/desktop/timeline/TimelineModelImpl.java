/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.desktop.timeline;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Range;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;

import org.gephi.timeline.api.TimelineModel;
import org.gephi.timeline.api.TimelineModelListener;
import org.openide.util.Lookup;

import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author jbilcke
 */
@ServiceProvider(service = TimelineModel.class)
public class TimelineModelImpl
        implements
        TimelineModel {

    private List<TimelineModelListener> listeners;

    public TimelineModelImpl() {
        listeners = new ArrayList<TimelineModelListener>();
    }

    public void fireChangeEvent() {
        ChangeEvent evt = new ChangeEvent(this);
        for (TimelineModelListener listener : listeners) {
            listener.timelineModelChanged(evt);
        }
    }


    public void addListener(TimelineModelListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(TimelineModelListener listener) {
        listeners.remove(listener);
    }


        // for the future chart
    public String getFirstAttributeLabel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // for the future chart
    public String getLastAttributeLabel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // for the future chart
    public String getAttributeLabel(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // for the future chart
    public String getAttributeLabel(int from, int to) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // for the future chart
    public double getAttributeValue(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // for the future chart
    public double getAttributeValue(int from, int to) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    public int getLength() {
         ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
         GraphController gc = pc.getCurrentWorkspace().getLookup().lookup(GraphController.class);
         gc.getModel(pc.getCurrentWorkspace());
         GraphModel gm = gc.getModel();
         if (gm == null) return 0;

        Graph g = gm.getGraph();
         if (g == null) return 0;

        return g.getNodeCount();
        // TODO get data from the current graph
    }

    public void setInterval(double from, double to) {
          ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        FilterController fc = pc.getCurrentWorkspace().getLookup().lookup(FilterController.class);
        Range range = new Range(from, to);
        //
    }


}
