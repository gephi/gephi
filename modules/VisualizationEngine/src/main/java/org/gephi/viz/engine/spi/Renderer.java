package org.gephi.viz.engine.spi;

import java.util.EnumSet;
import org.gephi.viz.engine.VizEngineModel;
import org.gephi.viz.engine.pipeline.RenderingLayer;

/**
 *
 * @param <R>
 * @author Eduardo Ramos
 */
public interface Renderer<R extends RenderingTarget, D extends WorldData> extends PipelinedExecutor<R> {

    D worldUpdated(VizEngineModel model, R target, float[] mvpFloats);

    void render(D data, R target, RenderingLayer layer, float[] mvpFloats);

    EnumSet<RenderingLayer> getLayers();
}
