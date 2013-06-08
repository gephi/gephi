package org.gephi.visualization.scheduler;

/**
 *
 * @author mbastian
 */
public class BasicFPSAnimator extends Thread {

    //Runnable
    protected final Runnable runnable;
    //Fps
    protected long startTime;
    protected long delay;
    //Flag
    protected boolean animating = true;
    //Lock
    protected final Object worldLock;
    protected final Object lock = new Object();

    public BasicFPSAnimator(Runnable runnable, Object worldLock, String name, float fps) {
        super(name);
        this.worldLock = worldLock;
        this.runnable = runnable;
        setDaemon(true);
        setFps(fps);
    }

    @Override
    public void run() {
        while (animating) {
            startTime = System.currentTimeMillis();
            //Execute
            synchronized (worldLock) {
                runnable.run();
            }
            //End
            long timeout;
            while ((timeout = delay - System.currentTimeMillis() + startTime) > 0) {
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
