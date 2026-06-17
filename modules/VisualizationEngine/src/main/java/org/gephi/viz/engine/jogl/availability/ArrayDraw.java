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

        // Data is now stored in RGBA32F textures fetched in the vertex shader via texelFetch
        // (GLSL 330), so the legacy GL2/GLES2 path is no longer supported.
        return drawable.getGLProfile().isGL2ES3();
    }

}
