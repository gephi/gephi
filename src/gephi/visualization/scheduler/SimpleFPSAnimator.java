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
package gephi.visualization.scheduler;

import gephi.visualization.swing.GraphDrawable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Mathieu
 */
public class SimpleFPSAnimator extends Thread {

    private GraphDrawable drawable;
    private long delay;
    private AtomicBoolean animating;
    private final Object lock = new Object();

    public SimpleFPSAnimator(GraphDrawable drawble, int fps) {
        super("SimpleFPSAnimator");
        this.drawable = drawble;
        this.animating = new AtomicBoolean();

        setFps(fps);
    }

    @Override
    public void run() {
        animating.set(true);

        try {
            while (animating.get()) {
                long startTime = System.currentTimeMillis();
                drawable.display();
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

    private void setFps(int fps) {
        delay = (long) (1000.0f / (float) fps);
    }
}
