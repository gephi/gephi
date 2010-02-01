/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.desktop.timeline;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
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

    private Float relativeSelectionStart;
    private Float relativeSelectionEnd;


    private TimelinePlayMode playMode;
    private float stepByTick;
    private AtomicBoolean paused;

    public TimelineModelImpl() {
        listeners = new ArrayList<ChangeListener>();
        TimelineList = new ArrayList<Timeline>();
        playMode = playMode.OLDEST;
        stepByTick = 0.01f;
        paused = new AtomicBoolean(true);

        relativeSelectionStart;
        relativeSelectionEnd;

        private
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

    public synchronized void setInterval(Float from, Float to) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public synchronized void setTo(Float to) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public synchronized void setFrom(Float from) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public synchronized Float getRelativeSelectionStartAsFloat() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public synchronized Float getRelativeSelectionEndAsFloat() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public synchronized Comparable getFirstAsFloat() {
        return 0.0f;
    }

    public synchronized Comparable getLastAsfloat() {
        return 1.0f;
    }

    public synchronized Comparable getFromAsComparable() {
        return getFromAsFloat();
    }

     public synchronized Comparable getToAsComparable() {
        return getToAsFloat();
    }

    public synchronized Comparable getFirstAsComparable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public synchronized Comparable getLastAsComparable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    public synchronized void play() {
        timer = new Timer(100, this); // 0.1
        timer.setInitialDelay(1900); // 1.9 sec
        paused.set(false);
        timer.start();
    }

    public synchronized void play(TimelinePlayMode playMode) {
        this.setTimelinePlayMode(playMode);
        this.play();
    }

    public synchronized boolean togglePause() {
        boolean p = !paused.get();
        paused.set(p);
        return p;
    }

    public synchronized void setPause(boolean p) {
        paused.set(p);
    }

    public synchronized void stop() {
         timer.stop();
    }
    
    public synchronized boolean isPaused() {
        return paused.get();
    }
     public synchronized boolean isStopped() {
        return !timer.isRunning();
    }

    public synchronized void setTimelinePlayMode(TimelinePlayMode playMode) {
        this.playMode = playMode;
    }
    public synchronized TimelinePlayMode getTimelinePlayMode() {
      return playMode;
    }

   public synchronized void setStepByTick(float s) {
        stepByTick = s;
    }
    public synchronized float getStepByTick() {
      return stepByTick;
    }

    public synchronized void actionPerformed(ActionEvent ae) {
        if (paused.get()) return;


        float s = getStepByTick();
        float f = getFrom();
        float t = getTo();

        switch(getTimelinePlayMode()) {
            case YOUNGEST:
                f += s;
                if (f >= 0.95) {
                    stop();
                    f = 0.95f;
                }
                break;
            case BOTH:
                f += s;
                t += s;
                if (t >= 1.0) {
                    stop();
                    t = 1.0f;
                    f -= s;
                }
                break;
            case OLDEST:
                t += s;
                if (t >= 1.0) {
                    stop();
                    t = 1.0f;
                 }
                break;
        }
    }

}
