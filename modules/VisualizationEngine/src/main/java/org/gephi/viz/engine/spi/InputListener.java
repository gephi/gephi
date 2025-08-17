package org.gephi.viz.engine.spi;

/**
 *
 * @author Eduardo Ramos
 * @param <R>
 * @param <T> Event type
 */
public interface InputListener<R extends RenderingTarget, T> extends PipelinedExecutor<R> {

    default void frameStart() {

    }

    /**
     *
     * @param event Event
     * @return True if consumed
     */
    boolean processEvent(T event);

    default void frameEnd() {

    }
}
