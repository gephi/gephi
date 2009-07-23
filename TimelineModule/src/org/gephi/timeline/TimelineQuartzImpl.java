/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
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
package org.gephi.timeline;

import java.util.ArrayList;
import java.util.List;
import org.gephi.timeline.api.TimelineQuartz;
import org.gephi.timeline.api.TimelineQuartzListener;
import org.openide.util.Exceptions;

/**
 *
 * @author Julian Bilcke
 */
public class TimelineQuartzImpl implements TimelineQuartz {

    private long delay; // quartz will awake every ms
    private List<TimelineQuartzListener> listeners;
    private QuartzRunnable runnable;
    private Thread thread;

    public TimelineQuartzImpl() {
        listeners = new ArrayList<TimelineQuartzListener>();
        delay = 500;
        runnable = new QuartzRunnable(this, delay);
        thread = new Thread(runnable);
    }

    public void setDelay(long delay) {
        runnable.setDelay(delay);
    }

    public long getDelay() {
        return delay;
    }

    public boolean isRunning() {
        return runnable.isRunning();
    }

    public void addTimelineQuartzListener(TimelineQuartzListener listener) {
        listeners.add(listener);
    }

    public void tick(long delay) {
        for (TimelineQuartzListener listener : listeners) {
            listener.quartzTick(delay);
        }
    }

    public void start() {
        runnable.start();
        thread.start();
    }

    public void delayedStop() {
        runnable.stop();
    }
    public void waitStop() {
        runnable.stop();
        
        try {
            thread.join();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}

class QuartzRunnable implements Runnable {

    private boolean running = false;
    private long delay = 1000;
    private TimelineQuartzImpl master;

    QuartzRunnable(TimelineQuartzImpl master, long delay) {
        this.master = master;
        this.delay = delay;
    }

    public synchronized void start() {
        running = true;
    }

    public void run() {
        while (isRunning()) {
            try {
                Thread.sleep(delay);
                master.tick(delay);
            } catch (InterruptedException e) {
                System.out.println("Interrupted Exception caught");
            }
        }

    }

    public void stop() {
        running = false;
    }

    public synchronized boolean isRunning() {
        return running;
    }

    void setDelay(long delay) {
        this.delay = delay;
    }
}
