package org.gephi.viz.engine.jogl.availability;

import com.jogamp.newt.event.NEWTEvent;
import com.jogamp.opengl.GLAutoDrawable;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;

/**
 *
 * @author Eduardo Ramos
 */
public class InstancedDraw {

    public static int getPreferenceInCategory() {
        return 50;
    }

    public static boolean isAvailable(VizEngine<JOGLRenderingTarget, NEWTEvent> engine, GLAutoDrawable drawable) {
        if (engine.getOpenGLOptions().isDisableIndirectDrawing()) {
            return false;
        }

        return drawable.getGLProfile().isGL2ES3()
            && engine.getRenderingTarget().getGlCapabilitiesSummary().isInstancingSupported();
    }
}
