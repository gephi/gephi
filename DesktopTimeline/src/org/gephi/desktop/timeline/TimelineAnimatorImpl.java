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
import org.gephi.timeline.api.TimelineAnimator;
import org.gephi.timeline.api.TimelineAnimatorListener;
import org.gephi.timeline.api.TimelinePlayMode;

/**
 *
 * @author jbilcke
 */
public class TimelineAnimatorImpl 
        implements TimelineAnimator, ActionListener {

    //Listeners
    private List<TimelineAnimatorListener> listeners;

    //
    private Timer timer;

    private Float relativeSelectionStart;
    private Float relativeSelectionEnd;


    private TimelinePlayMode playMode;
    private float stepByTick;
    private AtomicBoolean paused;

    public TimelineAnimatorImpl() {
        listeners = new ArrayList<TimelineAnimatorListener>();
        playMode = playMode.OLDEST;
        stepByTick = 0.01f;
        paused = new AtomicBoolean(true);

        relativeSelectionStart = 0.0f;
        relativeSelectionEnd = 1.0f;
    }

    public synchronized void setInterval(Float from, Float to) {
        setFrom(from);
        setTo(to);
    }

    public synchronized void setTo(Float to) {

        if (to > 1.0f) {
            relativeSelectionStart = 1.0f;
        }
        else if (to <= 0.0f) {
            relativeSelectionStart = 0.0f;
        } else {
        relativeSelectionStart = to;
        }
    }

    public synchronized void setFrom(Float from) {

        if (from > 1.0f) {
            relativeSelectionEnd = 1.0f;
        }
        else if (from <= 0.0f) {
            relativeSelectionEnd = 0.0f;
        } else {
        relativeSelectionEnd = from;
        }
    }
    public synchronized Float getFrom() {
        return relativeSelectionStart;
    }

    public synchronized Float getTo() {
        return relativeSelectionEnd;
    }

    public synchronized void play() {
        timer = new Timer(100, this); // 0.1
        timer.setInitialDelay(1900); // 1.9 sec
        paused.set(false);
        timer.start();
    }

    public synchronized void play(TimelinePlayMode playMode) {
        setTimelinePlayMode(playMode);
        play();
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

    public void addListener(TimelineAnimatorListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(TimelineAnimatorListener listener) {
        listeners.remove(listener);
    }

    public void fireChangeEvent() {
        ChangeEvent evt = new ChangeEvent(this);
        for (TimelineAnimatorListener listener : listeners) {
            listener.timelineAnimatorChanged(evt);
        }
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
        setInterval(f, t);
        fireChangeEvent();
    }

}
