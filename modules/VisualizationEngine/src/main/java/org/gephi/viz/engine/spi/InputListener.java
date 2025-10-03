package org.gephi.viz.engine.spi;

import org.gephi.viz.engine.VizEngineModel;

/**
 *
 * @param <R>
 * @param <T> Event type
 * @author Eduardo Ramos
 */
public interface InputListener<R extends RenderingTarget, T> extends PipelinedExecutor<R> {

    default void frameStart(VizEngineModel model) {

    }

    /**
     *
     * @param event Event
     * @return True if consumed
     */
    boolean processEvent(T event);

    default void frameEnd(VizEngineModel model) {

    }
}
