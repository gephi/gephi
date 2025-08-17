package org.gephi.viz.engine.spi;

import org.gephi.viz.engine.pipeline.RenderingLayer;

import java.util.EnumSet;

/**
 *
 * @author Eduardo Ramos
 * @param <R>
 */
public interface Renderer<R extends RenderingTarget> extends PipelinedExecutor<R> {

    void worldUpdated(R target);

    void render(R target, RenderingLayer layer);

    EnumSet<RenderingLayer> getLayers();
}
