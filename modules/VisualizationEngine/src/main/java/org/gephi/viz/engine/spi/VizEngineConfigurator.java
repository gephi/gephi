package org.gephi.viz.engine.spi;

import org.gephi.viz.engine.VizEngine;

/**
 *
 * @author Eduardo Ramos
 * @param <R>
 * @param <I>
 */
public interface VizEngineConfigurator<R extends RenderingTarget, I> {

    void configure(VizEngine<R, I> engine);
}
