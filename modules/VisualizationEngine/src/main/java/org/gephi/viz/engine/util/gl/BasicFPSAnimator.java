package org.gephi.viz.engine.util.gl;

import org.gephi.viz.engine.util.TimeUtils;

/**
 *
 * @author mbastian
 */
public class BasicFPSAnimator implements Runnable {

    //Runnable
    protected final Runnable runnable;
    //Fps
    protected long startTime;
    protected long delay;
    //Flag
    protected boolean animating = true;
    //Lock
    protected final Object lock = new Object();

    public BasicFPSAnimator(Runnable runnable, float fps) {
        this.runnable = runnable;
        setFps(fps);
    }

    @Override
    public void run() {
        while (animating) {
            startTime = TimeUtils.getTimeMillis();
            try {
                runnable.run();
            } catch (Throwable ex) {
                ex.printStackTrace();//TODO log if enabled
            }

            long timeout;
            while ((timeout = delay - TimeUtils.getTimeMillis() + startTime) > 0) {
                //Wait only if the time spent in display is inferior than delay
                //Otherwise the render loop acts as a 'as fast as you can' loop
                synchronized (this.lock) {
                    try {
                        this.lock.wait(timeout);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }
    }

    public final void setFps(float fps) {
        delay = (long) (1000.0f / fps);
        synchronized (this.lock) {
            startTime = 0;
            this.lock.notify();
        }
    }

    public final void shutdown() {
        animating = false;
    }

    public final boolean isAnimating() {
        return animating;
    }
}
