package org.gephi.viz.engine.spi;

import org.gephi.viz.engine.VizEngineModel;

/**
 *
 * @param <R>
 * @author Eduardo Ramos
 */
public interface WorldUpdater<R extends RenderingTarget> extends PipelinedExecutor<R> {

    void updateWorld(VizEngineModel model);
}
