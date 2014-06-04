package org.gephi.visualization.scheduler;

import java.util.concurrent.Semaphore;

/**
 *
 * @author mbastian
 */
public abstract class AbstractAnimator extends Thread {

    //Runnable
    protected final Runnable runnable;
    //Flag
    protected boolean animating = true;
    //Lock
    protected final Semaphore semaphore;

    public AbstractAnimator(Runnable runnable, Semaphore semaphore, String name) {
        super(name);
        this.semaphore = semaphore;
        this.runnable = runnable;
        setDaemon(true);
    }

    @Override
    public void run() {
        while (animating) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            runnable.run();
            semaphore.release();
        }
    }

    public final void shutdown() {
        animating = false;
        synchronized (this) {
            notify();
        }
    }

    public final boolean isAnimating() {
        return animating;
    }
}
