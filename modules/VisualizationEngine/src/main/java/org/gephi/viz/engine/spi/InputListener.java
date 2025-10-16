package org.gephi.viz.engine.spi;

import java.util.List;
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
     * Process a batch of events.
     * 
     * @param events List of events to process
     * @return List of events that were NOT consumed (remaining for next listener in pipeline)
     */
    List<T> processEvents(List<T> events);

    default void frameEnd(VizEngineModel model) {

    }
}
