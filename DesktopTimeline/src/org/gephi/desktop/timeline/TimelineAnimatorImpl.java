/*
Copyright 2010 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke, Patrick J. McSweeney
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
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

    private double relativeSelectionStart;
    private double relativeSelectionEnd;


    private TimelinePlayMode playMode;
    private double stepByTick;
    private AtomicBoolean paused;

    public TimelineAnimatorImpl() {
        listeners = new ArrayList<TimelineAnimatorListener>();
        playMode = playMode.OLDEST;
        stepByTick = 0.01;
        paused = new AtomicBoolean(true);

        relativeSelectionStart = 0.0;
        relativeSelectionEnd = 1.0;
    }

    public synchronized void setInterval(double from, double to) {
        setFrom(from);
        setTo(to);
    }

    public synchronized void setTo(double to) {

        if (to > 1.0) {
            relativeSelectionStart = 1.0;
        }
        else if (to <= 0.0) {
            relativeSelectionStart = 0.0;
        } else {
        relativeSelectionStart = to;
        }
    }

    public synchronized void setFrom(double from) {

        if (from > 1.0) {
            relativeSelectionEnd = 1.0;
        }
        else if (from <= 0.0) {
            relativeSelectionEnd = 0.0;
        } else {
        relativeSelectionEnd = from;
        }
    }
    public synchronized double getFrom() {
        return relativeSelectionStart;
    }

    public synchronized double getTo() {
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


    
   public synchronized void setStepByTick(double s) {
        stepByTick = s;
    }
    public synchronized double getStepByTick() {
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

        double s = getStepByTick();

        double f = getFrom();
        double t = getTo();

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
