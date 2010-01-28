/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.desktop.timeline;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.timeline.api.TimelineModel;
import org.gephi.timeline.api.TimelinePlayMode;
import org.gephi.timeline.spi.Timeline;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author jbilcke
 */
@ServiceProvider(service = TimelineModel.class)
public class TimelineModelImpl implements TimelineModel, ActionListener {
    //Model

    private List<Timeline> TimelineList;
    //Listeners
    private List<ChangeListener> listeners;
    
    //
    private Timer timer;
    private Float from;
    private Float to;

    public TimelineModelImpl() {
        listeners = new ArrayList<ChangeListener>();
        TimelineList = new ArrayList<Timeline>();
    }

    public Timeline[] getTimeline() {
        return TimelineList.toArray(new Timeline[0]);
    }

    public void addTimeline(Timeline Timeline) {
        TimelineList.add(Timeline);
        fireChangeEvent();
    }



    public void addChangeListener(ChangeListener changeListener) {
        if (!listeners.contains(changeListener)) {
            listeners.add(changeListener);
        }
    }

    public void removeChangeListener(ChangeListener changeListener) {
        listeners.remove(changeListener);
    }

    public void fireChangeEvent() {
        ChangeEvent evt = new ChangeEvent(this);
        for (ChangeListener listener : listeners) {
            listener.stateChanged(evt);
        }
    }


    public List<Float> getOverviewSample(int resolution) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<Float> getZoomSample(int resolution) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void selectInterval(Float from, Float to) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void selectTo(Float to) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void selectFrom(Float from) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Float getSelectionFrom() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Float getSelectionTo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Comparable getFirstComparable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Comparable getLastComparable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Comparable getSelectionFromAsComparable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Comparable getSelectionToAsComparable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void play() {
        timer = new Timer(100, this); // 0.1
        timer.setInitialDelay(1900); // 1.9 sec
        timer.start();

        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void play(TimelinePlayMode playMode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void pause() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isPlaying() {
        return timer.isRunning();
    }

    public void setTimelinePlayMode(TimelinePlayMode playMode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void actionPerformed(ActionEvent ae) {


        // animator.repaint();
        // if end reached
        timer.stop();
    }
}
