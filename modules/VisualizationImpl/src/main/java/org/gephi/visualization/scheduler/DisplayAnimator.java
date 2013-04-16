package org.gephi.visualization.scheduler;

import org.gephi.visualization.swing.GraphDrawableImpl;

/**
 *
 * @author mbastian
 */
public class DisplayAnimator implements Runnable {

    private final Object worldLock;
    private final GraphDrawableImpl drawable;
    //Animator
    private FPSAnimator animator;
    private float displayFpsLimit = 30f;

    public DisplayAnimator(GraphDrawableImpl drawable, Object worldLock) {
        this.drawable = drawable;
        this.worldLock = worldLock;
    }

    public synchronized void start() {
        animator = new FPSAnimator(this, worldLock, "DisplayAnimator", displayFpsLimit);
        animator.start();
    }

    public synchronized void stop() {
        if (animator == null) {
            return;
        }
        if (animator.isAnimating()) {
            animator.shutdown();
        }
    }

    public boolean isAnimating() {
        if (animator != null && animator.isAnimating()) {
            return true;
        }
        return false;
    }

    public void setFps(float maxFps) {
        this.displayFpsLimit = maxFps;
        if (animator != null) {
            animator.setFps(maxFps);
        }
    }

    @Override
    public void run() {
        drawable.display();
    }
}
