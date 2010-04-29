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
package org.gephi.visualization.scheduler;

import org.gephi.visualization.apiimpl.Scheduler;
import java.util.concurrent.atomic.AtomicBoolean;
import org.gephi.visualization.swing.GraphDrawableImpl;

/**
 *
 * @author Mathieu Bastian
 */
public class SimpleFPSAnimator extends Thread {

    private Scheduler scheduler;
    private GraphDrawableImpl drawable;
    private long delay;
    private AtomicBoolean animating;
    private final Object lock = new Object();
    private boolean displayCall = false;
    private long startTime;

    public SimpleFPSAnimator(Scheduler scheduler, GraphDrawableImpl drawble, float fps) {
        super("SimpleFPSAnimator");
        this.drawable = drawble;
        this.scheduler = scheduler;
        this.animating = new AtomicBoolean();

        setFps(fps);
        animating.set(true);
    }

    @Override
    public void run() {

        try {
            while (animating.get()) {
                startTime = System.currentTimeMillis();
                scheduler.updateWorld();
                scheduler.updatePosition();
                displayCall = true;
                drawable.display();
                displayCall = false;
                long timeout;
                while ((timeout = delay - System.currentTimeMillis() + startTime) > 0) {
                    //Wait only if the time spent in display is inferior than delay
                    //Otherwise the render loop acts as a 'as fast as you can' loop
                    synchronized (this.lock) {
                        this.lock.wait(timeout);
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        animating.set(false);
    }

    public boolean isAnimating() {
        return animating.get();
    }

    public void setFps(float fps) {
        delay = (long) (1000.0f / fps);
        synchronized (this.lock) {
            startTime = 0;
            this.lock.notify();
        }
    }

    public boolean isDisplayCall() {
        return displayCall;
    }
}
