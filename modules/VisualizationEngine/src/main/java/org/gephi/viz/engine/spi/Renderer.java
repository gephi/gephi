package org.gephi.viz.engine.spi;

import java.util.EnumSet;
import org.gephi.viz.engine.pipeline.RenderingLayer;

/**
 *
 * @param <R>
 * @author Eduardo Ramos
 */
public interface Renderer<R extends RenderingTarget> extends PipelinedExecutor<R> {

    void worldUpdated(R target);

    void render(R target, RenderingLayer layer);

    EnumSet<RenderingLayer> getLayers();
}
