package org.gephi.viz.engine.spi;

import org.gephi.viz.engine.VizEngine;

/**
 *
 * @param <R>
 * @param <I>
 * @author Eduardo Ramos
 */
public interface VizEngineConfigurator<R extends RenderingTarget, I> {

    void configure(VizEngine<R, I> engine);
}
