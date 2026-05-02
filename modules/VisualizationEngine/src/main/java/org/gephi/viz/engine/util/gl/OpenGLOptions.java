package org.gephi.viz.engine.util.gl;

import org.gephi.viz.engine.jogl.util.gl.capabilities.GLCapabilitiesSummary;

/**
 *
 * @author Eduardo Ramos
 */
public class OpenGLOptions {

    private boolean disableIndirectDrawing = false;
    private boolean disableInstancedDrawing = false;
    private boolean disableVertexArrayDrawing = false;
    private boolean disableVAOS = false;
    private boolean debug = false;
    private GLCapabilitiesSummary glCapabilitiesSummary = null;

    public OpenGLOptions() {
    }

    public boolean isDisableIndirectDrawing() {
        return disableIndirectDrawing;
    }

    public void setDisableIndirectDrawing(boolean disableIndirectDrawing) {
        this.disableIndirectDrawing = disableIndirectDrawing;
    }

    public boolean isDisableInstancedDrawing() {
        return disableInstancedDrawing;
    }

    public void setDisableInstancedDrawing(boolean disableInstancedDrawing) {
        this.disableInstancedDrawing = disableInstancedDrawing;
    }

    public boolean isDisableVertexArrayDrawing() {
        return disableVertexArrayDrawing;
    }

    public void setDisableVertexArrayDrawing(boolean disableVertexArrayDrawing) {
        this.disableVertexArrayDrawing = disableVertexArrayDrawing;
    }

    public boolean isDisableVAOS() {
        return disableVAOS;
    }

    public void setDisableVAOS(boolean disableVAOS) {
        this.disableVAOS = disableVAOS;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setGlCapabilitiesSummary(GLCapabilitiesSummary glCapabilitiesSummary) {
        this.glCapabilitiesSummary = glCapabilitiesSummary;
    }

    public boolean isVAOSupported() {
        return glCapabilitiesSummary.isVAOSupported() && !this.isDisableVAOS();
    }

    public boolean isInstancingSupported() {
        return glCapabilitiesSummary.isInstancingSupported();
    }

    public boolean isIndirectDrawSupported() {
        return glCapabilitiesSummary.isIndirectDrawSupported();
    }

    public boolean isVendorIntel() {
        return glCapabilitiesSummary.isVendorIntel();
    }

    public GLCapabilitiesSummary getGlCapabilitiesSummary() {
        return glCapabilitiesSummary;
    }

    @Override
    public String toString() {
        return "OpenGLOptions{" + "disableIndirectDrawing=" + disableIndirectDrawing + ", disableInstancedDrawing=" +
            disableInstancedDrawing + ", disableVertexArrayDrawing=" + disableVertexArrayDrawing + ", disableVAOS=" +
            disableVAOS + ", debug=" + debug + '}';
    }

}
