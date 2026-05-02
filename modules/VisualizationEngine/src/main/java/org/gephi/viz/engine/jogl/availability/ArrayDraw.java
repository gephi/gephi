package org.gephi.viz.engine.jogl.availability;

import com.jogamp.opengl.GLAutoDrawable;
import org.gephi.viz.engine.VizEngine;

/**
 *
 * @author Eduardo Ramos
 */
public class ArrayDraw {

    public static int getPreferenceInCategory() {
        return 0;
    }

    public static boolean isAvailable(VizEngine engine, GLAutoDrawable drawable) {
        if (engine.getOpenGLOptions().isDisableVertexArrayDrawing()) {
            return false;
        }

        return drawable.getGLProfile().isGL2ES2();
    }

}
