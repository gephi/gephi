package org.gephi.viz.engine.jogl.availability;

import com.jogamp.newt.event.NEWTEvent;
import com.jogamp.opengl.GLAutoDrawable;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;

/**
 *
 * @author Eduardo Ramos
 */
public class IndirectDraw {

    public static int getPreferenceInCategory() {
        return 100;
    }

    public static boolean isAvailable(VizEngine<JOGLRenderingTarget, NEWTEvent> engine, GLAutoDrawable drawable) {
        if (engine.getOpenGLOptions().isDisableIndirectDrawing()) {
            return false;
        }

        return drawable.getGLProfile().isGL4()
            && engine.getRenderingTarget().getGlCapabilitiesSummary().isIndirectDrawSupported();
    }

}
