package org.gephi.viz.engine.spi;

import com.jogamp.opengl.GL3ES3;
import org.gephi.viz.engine.VizEngine;

/**
 *
 * @author Eduardo Ramos
 */
public interface RenderingTarget {

    void setup(VizEngine engine);

    default void frameStart() {
        //NOOP
    }

    default void frameEnd() {
        //NOOP
    }

    int getFps();
}
