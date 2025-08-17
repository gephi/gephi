package org.gephi.viz.engine.spi;

import org.gephi.viz.engine.VizEngine;

/**
 *
 * @author Eduardo Ramos
 */
public interface RenderingTarget {

    void setup(VizEngine engine);

    void start();

    void stop();

    default void frameStart() {
        //NOOP
    }

    default void frameEnd() {
        //NOOP
    }
}
