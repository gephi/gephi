package org.gephi.viz.engine.util.gl;

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

    @Override
    public String toString() {
        return "OpenGLOptions{" + "disableIndirectDrawing=" + disableIndirectDrawing + ", disableInstancedDrawing=" + disableInstancedDrawing + ", disableVertexArrayDrawing=" + disableVertexArrayDrawing + ", disableVAOS=" + disableVAOS + ", debug=" + debug + '}';
    }

}
