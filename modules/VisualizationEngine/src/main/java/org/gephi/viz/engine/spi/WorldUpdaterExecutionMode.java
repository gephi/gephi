package org.gephi.viz.engine.spi;

/**
 *
 * @author Eduardo Ramos
 */
public enum WorldUpdaterExecutionMode {
    /**
     * Run each world update in the render loop thread.
     */
    SINGLE_THREAD,
    /**
     * Run each world update in a concurrent thread and wait for termination in the render loop thread before rendering.
     */
    CONCURRENT_SYNCHRONOUS,
    /**
     * <p>
     * Run each world update in a concurrent thread but don't wait for termination in the render loop thread before rendering.
     * </p>
     * <p>
     * Maximizes FPS and responsiveness to input events but can cause flicker when zooming out.
     * </p>
     */
    CONCURRENT_ASYNCHRONOUS;

    public boolean isConcurrent() {
        return this == CONCURRENT_SYNCHRONOUS || this == CONCURRENT_ASYNCHRONOUS;
    }

    public boolean isSynchronous() {
        return this == CONCURRENT_SYNCHRONOUS || this == SINGLE_THREAD;
    }
}
