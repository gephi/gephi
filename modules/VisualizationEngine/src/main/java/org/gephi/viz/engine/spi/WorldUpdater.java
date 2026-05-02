package org.gephi.viz.engine.spi;

import org.gephi.viz.engine.VizEngineModel;

/**
 *
 * @param <R>
 * @author Eduardo Ramos
 */
public interface WorldUpdater<R extends RenderingTarget, T> extends PipelinedExecutor<R> {

    void updateWorld(VizEngineModel model);

    ElementsCallback<T> getElementsCallback();
}
