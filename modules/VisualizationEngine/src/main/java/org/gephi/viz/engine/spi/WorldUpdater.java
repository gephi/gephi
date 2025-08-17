package org.gephi.viz.engine.spi;

/**
 *
 * @author Eduardo Ramos
 * @param <R>
 */
public interface WorldUpdater<R extends RenderingTarget> extends PipelinedExecutor<R> {

    void updateWorld();
}
