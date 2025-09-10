package org.gephi.viz.engine.spi;

import java.util.EnumSet;
import org.gephi.viz.engine.VizEngineModel;
import org.gephi.viz.engine.pipeline.RenderingLayer;

/**
 *
 * @param <R>
 * @author Eduardo Ramos
 */
public interface Renderer<R extends RenderingTarget> extends PipelinedExecutor<R> {

    void worldUpdated(VizEngineModel model, R target);

    void render(VizEngineModel model, R target, RenderingLayer layer);

    EnumSet<RenderingLayer> getLayers();
}
