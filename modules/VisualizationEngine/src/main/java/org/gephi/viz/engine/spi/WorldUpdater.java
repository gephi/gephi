package org.gephi.viz.engine.spi;

/**
 *
 * @param <R>
 * @author Eduardo Ramos
 */
public interface WorldUpdater<R extends RenderingTarget> extends PipelinedExecutor<R> {

    void updateWorld();
}
